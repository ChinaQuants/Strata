/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.finance.rate.swap;

import static com.opengamma.strata.basics.currency.Currency.GBP;
import static com.opengamma.strata.basics.date.HolidayCalendars.EUTA;
import static com.opengamma.strata.basics.index.FxIndices.ECB_EUR_GBP;
import static com.opengamma.strata.basics.index.FxIndices.ECB_EUR_USD;
import static com.opengamma.strata.collect.TestHelper.assertSerialization;
import static com.opengamma.strata.collect.TestHelper.assertThrowsIllegalArg;
import static com.opengamma.strata.collect.TestHelper.coverBeanEquals;
import static com.opengamma.strata.collect.TestHelper.coverImmutableBean;
import static com.opengamma.strata.collect.TestHelper.date;
import static org.testng.Assert.assertEquals;

import java.time.LocalDate;

import org.testng.annotations.Test;

import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.date.DaysAdjustment;
import com.opengamma.strata.basics.schedule.SchedulePeriod;

/**
 * Test.
 */
@Test
public class FxResetCalculationTest {

  private static final DaysAdjustment MINUS_TWO_DAYS = DaysAdjustment.ofBusinessDays(-2, EUTA);
  private static final DaysAdjustment MINUS_THREE_DAYS = DaysAdjustment.ofBusinessDays(-3, EUTA);
  private static final LocalDate DATE_2014_03_31 = date(2014, 3, 31);
  private static final LocalDate DATE_2014_06_30 = date(2014, 6, 30);

  public void test_builder() {
    FxResetCalculation test = FxResetCalculation.builder()
        .index(ECB_EUR_GBP)
        .referenceCurrency(GBP)
        .fixingDateOffset(MINUS_TWO_DAYS)
        .fixingRelativeTo(FxResetFixingRelativeTo.PERIOD_START)
        .build();
    assertEquals(test.getIndex(), ECB_EUR_GBP);
    assertEquals(test.getReferenceCurrency(), GBP);
    assertEquals(test.getFixingDateOffset(), MINUS_TWO_DAYS);
    assertEquals(test.getFixingRelativeTo(), FxResetFixingRelativeTo.PERIOD_START);
  }

  public void test_builder_defaultFixingRelativeTo() {
    FxResetCalculation test = FxResetCalculation.builder()
        .index(ECB_EUR_GBP)
        .referenceCurrency(GBP)
        .fixingDateOffset(MINUS_TWO_DAYS)
        .build();
    assertEquals(test.getIndex(), ECB_EUR_GBP);
    assertEquals(test.getReferenceCurrency(), GBP);
    assertEquals(test.getFixingDateOffset(), MINUS_TWO_DAYS);
    assertEquals(test.getFixingRelativeTo(), FxResetFixingRelativeTo.PERIOD_START);
  }

  public void test_invalidCurrency() {
    assertThrowsIllegalArg(() -> FxResetCalculation.builder()
        .index(ECB_EUR_USD)
        .referenceCurrency(GBP)
        .fixingDateOffset(MINUS_TWO_DAYS)
        .build());
  }

  //-------------------------------------------------------------------------
  public void test_applyToPeriod_beforeStart_weekend() {
    FxResetCalculation base = FxResetCalculation.builder()
        .index(ECB_EUR_GBP)
        .referenceCurrency(GBP)
        .fixingDateOffset(MINUS_TWO_DAYS)
        .build();
    FxReset test = base.applyToPeriod(SchedulePeriod.of(DATE_2014_03_31, DATE_2014_06_30));
    assertEquals(test, FxReset.of(ECB_EUR_GBP, GBP, date(2014, 3, 27)));
  }

  public void test_applyToPeriod_beforeEnd_weekend() {
    FxResetCalculation base = FxResetCalculation.builder()
        .index(ECB_EUR_GBP)
        .referenceCurrency(GBP)
        .fixingDateOffset(MINUS_TWO_DAYS)
        .fixingRelativeTo(FxResetFixingRelativeTo.PERIOD_END)
        .build();
    FxReset test = base.applyToPeriod(SchedulePeriod.of(DATE_2014_03_31, DATE_2014_06_30));
    assertEquals(test, FxReset.of(ECB_EUR_GBP, GBP, date(2014, 6, 26)));
  }

  public void test_applyToPeriod_beforeStart_threeDays() {
    FxResetCalculation base = FxResetCalculation.builder()
        .index(ECB_EUR_GBP)
        .referenceCurrency(GBP)
        .fixingDateOffset(MINUS_THREE_DAYS)
        .build();
    FxReset test = base.applyToPeriod(SchedulePeriod.of(DATE_2014_03_31, DATE_2014_06_30));
    assertEquals(test, FxReset.of(ECB_EUR_GBP, GBP, date(2014, 3, 26)));
  }

  //-------------------------------------------------------------------------
  public void coverage() {
    FxResetCalculation test = FxResetCalculation.builder()
        .index(ECB_EUR_GBP)
        .referenceCurrency(GBP)
        .fixingDateOffset(MINUS_TWO_DAYS)
        .build();
    coverImmutableBean(test);
    FxResetCalculation test2 = FxResetCalculation.builder()
        .index(ECB_EUR_USD)
        .referenceCurrency(Currency.EUR)
        .fixingDateOffset(MINUS_THREE_DAYS)
        .fixingRelativeTo(FxResetFixingRelativeTo.PERIOD_END)
        .build();
    coverBeanEquals(test, test2);
  }

  public void test_serialization() {
    FxResetCalculation test = FxResetCalculation.builder()
        .index(ECB_EUR_GBP)
        .referenceCurrency(GBP)
        .fixingDateOffset(MINUS_TWO_DAYS)
        .build();
    assertSerialization(test);
  }

}
