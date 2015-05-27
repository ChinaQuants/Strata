/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.strata.basics.index;

import static com.opengamma.strata.basics.currency.Currency.EUR;
import static com.opengamma.strata.basics.currency.Currency.GBP;
import static com.opengamma.strata.basics.date.HolidayCalendars.GBLO;
import static com.opengamma.strata.basics.index.StandardFxIndices.ECB_EUR_CHF;
import static com.opengamma.strata.collect.TestHelper.assertJodaConvert;
import static com.opengamma.strata.collect.TestHelper.assertSerialization;
import static com.opengamma.strata.collect.TestHelper.assertThrowsIllegalArg;
import static com.opengamma.strata.collect.TestHelper.coverImmutableBean;
import static com.opengamma.strata.collect.TestHelper.coverPrivateConstructor;
import static com.opengamma.strata.collect.TestHelper.date;
import static org.testng.Assert.assertEquals;

import org.joda.beans.ImmutableBean;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.opengamma.strata.basics.currency.CurrencyPair;
import com.opengamma.strata.basics.date.DaysAdjustment;
import com.opengamma.strata.basics.date.HolidayCalendars;

/**
 * Test {@link FxIndex}.
 */
@Test
public class FxIndexTest {

  //-------------------------------------------------------------------------
  @DataProvider(name = "name")
  static Object[][] data_name() {
    return new Object[][] {
        {FxIndices.ECB_EUR_CHF, "ECB-EUR-CHF"},
        {FxIndices.ECB_EUR_GBP, "ECB-EUR-GBP"},
        {FxIndices.ECB_EUR_JPY, "ECB-EUR-JPY"},
        {FxIndices.ECB_EUR_USD, "ECB-EUR-USD"},
        {FxIndices.WM_USD_CHF, "WM-USD-CHF"},
        {FxIndices.WM_EUR_USD, "WM-EUR-USD"},
        {FxIndices.WM_GBP_USD, "WM-GBP-USD"},
        {FxIndices.WM_USD_JPY, "WM-USD-JPY"},
    };
  }

  @Test(dataProvider = "name")
  public void test_name(FxIndex convention, String name) {
    assertEquals(convention.getName(), name);
  }

  @Test(dataProvider = "name")
  public void test_toString(FxIndex convention, String name) {
    assertEquals(convention.toString(), name);
  }

  @Test(dataProvider = "name")
  public void test_of_lookup(FxIndex convention, String name) {
    assertEquals(FxIndex.of(name), convention);
  }

  @Test(dataProvider = "name")
  public void test_extendedEnum(FxIndex convention, String name) {
    ImmutableMap<String, FxIndex> map = FxIndex.extendedEnum().lookupAll();
    assertEquals(map.get(name), convention);
  }

  public void test_of_lookup_notFound() {
    assertThrowsIllegalArg(() -> FxIndex.of("Rubbish"));
  }

  public void test_of_lookup_null() {
    assertThrowsIllegalArg(() -> FxIndex.of((String) null));
  }

  //-------------------------------------------------------------------------
  public void test_ecb_eur_gbp_dates() {
    FxIndex test = FxIndices.ECB_EUR_GBP;
    assertEquals(test.calculateMaturityFromFixing(date(2014, 10, 13)), date(2014, 10, 15));
    assertEquals(test.calculateFixingFromMaturity(date(2014, 10, 15)), date(2014, 10, 13));
    // weekend
    assertEquals(test.calculateMaturityFromFixing(date(2014, 10, 16)), date(2014, 10, 20));
    assertEquals(test.calculateFixingFromMaturity(date(2014, 10, 20)), date(2014, 10, 16));
    assertEquals(test.calculateMaturityFromFixing(date(2014, 10, 17)), date(2014, 10, 21));
    assertEquals(test.calculateFixingFromMaturity(date(2014, 10, 21)), date(2014, 10, 17));
    // input date is Sunday
    assertEquals(test.calculateMaturityFromFixing(date(2014, 10, 19)), date(2014, 10, 22));
    assertEquals(test.calculateFixingFromMaturity(date(2014, 10, 19)), date(2014, 10, 16));
    // skip maturity over EUR (1st May) and GBP (5th May) holiday
    assertEquals(test.calculateMaturityFromFixing(date(2014, 4, 30)), date(2014, 5, 6));
    assertEquals(test.calculateFixingFromMaturity(date(2014, 5, 6)), date(2014, 4, 30));
  }

  public void test_dates() {
    FxIndex test = ImmutableFxIndex.builder()
        .name("Test")
        .currencyPair(CurrencyPair.of(EUR, GBP))
        .fixingCalendar(HolidayCalendars.NO_HOLIDAYS)
        .maturityDateOffset(DaysAdjustment.ofCalendarDays(2))
        .build();
    assertEquals(test.calculateMaturityFromFixing(date(2014, 10, 13)), date(2014, 10, 15));
    assertEquals(test.calculateFixingFromMaturity(date(2014, 10, 15)), date(2014, 10, 13));
    // weekend
    assertEquals(test.calculateMaturityFromFixing(date(2014, 10, 16)), date(2014, 10, 18));
    assertEquals(test.calculateFixingFromMaturity(date(2014, 10, 18)), date(2014, 10, 16));
    assertEquals(test.calculateMaturityFromFixing(date(2014, 10, 17)), date(2014, 10, 19));
    assertEquals(test.calculateFixingFromMaturity(date(2014, 10, 19)), date(2014, 10, 17));
    // input date is Sunday
    assertEquals(test.calculateMaturityFromFixing(date(2014, 10, 19)), date(2014, 10, 21));
    assertEquals(test.calculateFixingFromMaturity(date(2014, 10, 19)), date(2014, 10, 17));
  }

  //-------------------------------------------------------------------------
  public void test_equals() {
    ImmutableFxIndex a = ImmutableFxIndex.builder()
        .name("GBP-EUR")
        .currencyPair(CurrencyPair.of(GBP, EUR))
        .fixingCalendar(GBLO)
        .maturityDateOffset(DaysAdjustment.ofBusinessDays(2, GBLO))
        .build();
    ImmutableFxIndex b = a.toBuilder().name("EUR-GBP").build();
    assertEquals(a.equals(b), false);
  }

  //-------------------------------------------------------------------------
  public void coverage() {
    coverPrivateConstructor(FxIndices.class);
    coverPrivateConstructor(StandardFxIndices.class);
    coverImmutableBean((ImmutableBean) ECB_EUR_CHF);
  }

  public void test_jodaConvert() {
    assertJodaConvert(FxIndex.class, ECB_EUR_CHF);
  }

  public void test_serialization() {
    assertSerialization(ECB_EUR_CHF);
  }

}
