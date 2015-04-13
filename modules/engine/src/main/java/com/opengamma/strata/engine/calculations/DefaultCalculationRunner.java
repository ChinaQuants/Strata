/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.engine.calculations;

import static com.opengamma.strata.collect.Guavate.toImmutableList;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import com.google.common.collect.ImmutableList;
import com.opengamma.strata.basics.CalculationTarget;
import com.opengamma.strata.collect.ArgChecker;
import com.opengamma.strata.collect.result.Result;
import com.opengamma.strata.engine.Column;
import com.opengamma.strata.engine.config.CalculationTaskConfig;
import com.opengamma.strata.engine.config.CalculationTasksConfig;
import com.opengamma.strata.engine.config.EngineFunctionConfig;
import com.opengamma.strata.engine.config.MarketDataRules;
import com.opengamma.strata.engine.config.PricingRules;
import com.opengamma.strata.engine.config.ReportingRules;
import com.opengamma.strata.engine.marketdata.BaseMarketData;
import com.opengamma.strata.engine.marketdata.ScenarioMarketData;
import com.opengamma.strata.engine.marketdata.SingleScenarioMarketData;
import com.opengamma.strata.engine.marketdata.mapping.MarketDataMappings;

/**
 * The default calculation runner implementation.
 */
public class DefaultCalculationRunner implements CalculationRunner {

  /** Executes the tasks that perform the individual calculations. */
  private final ExecutorService executor;

  /** Factory for consumers that wrap listeners to control threading and notify them when calculations are complete. */
  private final ConsumerFactory consumerFactory = ListenerWrapper::new;

  /**
   * @param executor  executes the tasks that perform the calculations
   */
  public DefaultCalculationRunner(ExecutorService executor) {
    this.executor = ArgChecker.notNull(executor, "executor");
  }

  @Override
  public CalculationTasksConfig createCalculationConfig(
      List<? extends CalculationTarget> targets,
      List<Column> columns,
      PricingRules pricingRules,
      MarketDataRules marketDataRules,
      ReportingRules reportingRules) {

    // Create columns with rules that are a combination of the column overrides and the defaults
    List<Column> effectiveColumns =
        columns.stream()
            .map(column -> column.withDefaultRules(pricingRules, marketDataRules, reportingRules))
            .collect(toImmutableList());

    ImmutableList.Builder<CalculationTaskConfig> configBuilder = ImmutableList.builder();

    for (int i = 0; i < targets.size(); i++) {
      for (int j = 0; j < columns.size(); j++) {
        configBuilder.add(createTaskConfig(i, j, targets.get(i), effectiveColumns.get(j)));
      }
    }
    List<CalculationTaskConfig> config = configBuilder.build();

    return CalculationTasksConfig.builder()
        .columns(columns)
        .taskConfigurations(config)
        .build();
  }

  @Override
  public CalculationTasks createCalculationTasks(CalculationTasksConfig config) {
    List<CalculationTask> tasks = config.getTaskConfigurations().stream().map(this::createTask).collect(toImmutableList());
    return new CalculationTasks(tasks, config.getColumns());
  }

  @Override
  public Results calculate(CalculationTasks tasks, BaseMarketData marketData) {
    return calculate(tasks, new SingleScenarioMarketData(marketData));
  }

  @Override
  public Results calculate(CalculationTasks tasks, ScenarioMarketData marketData) {
    Listener listener = new Listener(tasks.getColumns());
    calculateAsync(tasks, marketData, listener);
    return listener.result();
  }

  @Override
  public void calculateAsync(CalculationTasks tasks, BaseMarketData marketData, CalculationListener listener) {
    calculateAsync(tasks, new SingleScenarioMarketData(marketData), listener);
  }

  @Override
  public void calculateAsync(CalculationTasks tasks, ScenarioMarketData marketData, CalculationListener listener) {
    List<CalculationTask> taskList = tasks.getTasks();
    Consumer<CalculationResult> consumer = consumerFactory.create(listener, taskList.size());
    taskList.stream().forEach(task -> runTask(task, marketData, consumer));
  }

  private void runTask(CalculationTask task, ScenarioMarketData marketData, Consumer<CalculationResult> consumer) {
    // Submits a task to the executor to be run. The result of the task is passed to consumer.accept()
    CompletableFuture.supplyAsync(() -> task.execute(marketData), executor).thenAccept(consumer::accept);
  }

  /**
   * Calculation listener that receives the results of individual calculations and builds a set of {@link Results}.
   */
  private static final class Listener extends AggregatingCalculationListener<Results> {

    /** Comparator for sorting the results by row and then column. */
    private static final Comparator<CalculationResult> COMPARATOR =
        Comparator.comparingInt(CalculationResult::getRowIndex)
            .thenComparingInt(CalculationResult::getColumnIndex);

    /** List that is populated with the results as they arrive. */
    private final List<CalculationResult> results = new ArrayList<>();

    /** The columns that define what values are calculated. */
    private final List<Column> columns;

    private Listener(List<Column> columns) {
      this.columns = columns;
    }

    @Override
    public void resultReceived(CalculationResult result) {
      results.add(result);
    }

    @Override
    protected Results createAggregateResult() {
      results.sort(COMPARATOR);
      return buildResults(results, columns);
    }

    /**
     * Builds a set of results from the results of the individual calculations.
     *
     * @param calculationResults  the results of the individual calculations
     * @param columns  the columns that define what values are calculated
     * @return the results
     */
    private static Results buildResults(List<CalculationResult> calculationResults, List<Column> columns) {
      Map<Column, Integer> columnIndices =
          IntStream.range(0, columns.size())
              .mapToObj(Integer::valueOf)
              .collect(toMap(columns::get, i -> i));

      List<Result<?>> results =
          calculationResults.stream()
              .map(CalculationResult::getResult)
              .collect(toImmutableList());

      return Results.builder().columnIndices(columnIndices).values(results).build();
    }
  }

  /**
   * Creates configuration for calculating the value of a single measure for a target.
   *
   * @param rowIndex  the row index of the value in the results grid
   * @param columnIndex  the column index of the value in the results grid
   * @param target  the target for which the measure will be calculated
   * @param column  the column for which the value is calculated
   * @return configuration for calculating the value for the target
   */
  private static CalculationTaskConfig createTaskConfig(
      int rowIndex,
      int columnIndex,
      CalculationTarget target,
      Column column) {

    EngineFunctionConfig functionConfig =
        column.getPricingRules().functionConfig(target, column.getMeasure())
            .orElse(EngineFunctionConfig.DEFAULT);

    // Use the mappings from the market data rules, else create a set of mappings that cause a failure to
    // be returned in the market data with an error message saying the rules didn't match the target
    MarketDataMappings marketDataMappings =
        column.getMarketDataRules().mappings(target)
            .orElse(NoMatchingRuleMappings.INSTANCE);

    return CalculationTaskConfig.of(
        target,
        rowIndex,
        columnIndex,
        functionConfig,
        marketDataMappings,
        column.getReportingRules());
  }

  /**
   * Creates a task for performing a single calculation.
   *
   * @param config  configuration for the task
   * @return a task for performing a single calculation
   */
  private CalculationTask createTask(CalculationTaskConfig config) {
    return new CalculationTask(
        config.getTarget(),
        config.getRowIndex(),
        config.getColumnIndex(),
        config.getEngineFunctionConfig().createFunction(),
        config.getMarketDataMappings(),
        config.getReportingRules());
  }

  /**
   * Factory for consumers of calculation results.
   * <p>
   * The consumer wraps a {@link CalculationListener} to ensure it is only invoked
   * by a single thread at a time.
   * <p>
   * It is also responsible for keeping track of how many results have been received and invoking
   * {@link CalculationListener#calculationsComplete() calculationsComplete()}.
   */
  private interface ConsumerFactory {

    /**
     * Returns a consumer to deliver messages to the listener.
     *
     * @param listener  the listener to which the consumer will deliver messages
     * @param totalResultsCount  the total number of results expected
     * @return a consumer to deliver messages to the listener
     */
    public abstract Consumer<CalculationResult> create(CalculationListener listener, int totalResultsCount);
  }
}