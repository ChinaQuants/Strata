/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 * <p>
 * Please see distribution for license.
 */
package com.opengamma.strata.report.framework.expression;

import java.util.List;
import java.util.Set;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

/**
 * Evaluator that evaluates the first token in the expression.
 * <p>
 * The supported values for the first token are enumerated in {@link ValueRootType}.
 */
class RootEvaluator extends TokenEvaluator<ResultsRow> {

  /** The single shared instance of this class. */
  static final RootEvaluator INSTANCE = new RootEvaluator();

  private static final ImmutableSet<String> TOKENS = ImmutableSet.of(
      ValueRootType.MEASURES.token(),
      ValueRootType.TRADE.token(),
      ValueRootType.PRODUCT.token());

  @Override
  public Class<?> getTargetType() {
    // This isn't used because the root parser has special treatment
    return ResultsRow.class;
  }

  @Override
  public Set<String> tokens(ResultsRow target) {
    return TOKENS;
  }

  @Override
  public EvaluationResult evaluate(ResultsRow resultsRow, String firstToken, List<String> remainingTokens) {
    ValueRootType rootType = ValueRootType.parseToken(firstToken);
    switch (rootType) {
      case MEASURES:
        return evaluateMeasures(resultsRow, remainingTokens);
      case PRODUCT:
        return EvaluationResult.of(resultsRow.getProduct(), remainingTokens);
      case TRADE:
        return EvaluationResult.success(resultsRow.getTrade(), remainingTokens);
      default:
        throw new IllegalArgumentException("Unknown root token '" + rootType.token() + "'");
    }
  }

  // find the result starting from a measure
  private EvaluationResult evaluateMeasures(ResultsRow resultsRow, List<String> remainingTokens) {
    // if no measures, return list of valid measures
    if (remainingTokens.isEmpty() || Strings.nullToEmpty(remainingTokens.get(0)).trim().isEmpty()) {
      List<String> measureNames = ResultsRow.measureNames(resultsRow.getTrade());
      return EvaluationResult.failure("No measure specified. Use one of: {}", measureNames);
    }
    // evaluate the measure name
    String measureToken = remainingTokens.get(0);
    return EvaluationResult.of(
        resultsRow.getResult(measureToken), remainingTokens.subList(1, remainingTokens.size()));
  }

}
