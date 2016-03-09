/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.market.sensitivity;

import static com.opengamma.strata.basics.currency.Currency.GBP;
import static com.opengamma.strata.basics.currency.Currency.USD;
import static com.opengamma.strata.basics.index.IborIndices.GBP_LIBOR_3M;
import static com.opengamma.strata.collect.TestHelper.assertSerialization;
import static com.opengamma.strata.collect.TestHelper.coverBeanEquals;
import static com.opengamma.strata.collect.TestHelper.coverImmutableBean;
import static com.opengamma.strata.collect.TestHelper.date;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import java.time.LocalDate;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.CurrencyPair;
import com.opengamma.strata.basics.currency.FxMatrix;
import com.opengamma.strata.basics.index.IborIndexObservation;
import com.opengamma.strata.basics.market.ReferenceData;
import com.opengamma.strata.market.value.BondGroup;

/**
 * Test {@link RepoCurveZeroRateSensitivity}.
 */
@Test
public class RepoCurveZeroRateSensitivityTest {

  private static final ReferenceData REF_DATA = ReferenceData.standard();
  private static final LocalDate DATE = date(2015, 8, 27);
  private static final LocalDate DATE2 = date(2015, 9, 27);
  private static final double VALUE = 32d;
  private static final Currency CURRENCY = USD;
  private static final BondGroup GROUP = BondGroup.of("ISSUER1 BND 10Y");

  //-------------------------------------------------------------------------
  public void test_of_withSensitivityCurrency() {
    Currency sensiCurrency = GBP;
    RepoCurveZeroRateSensitivity test = RepoCurveZeroRateSensitivity.of(CURRENCY, DATE, sensiCurrency, GROUP, VALUE);
    assertEquals(test.getBondGroup(), GROUP);
    assertEquals(test.getCurveCurrency(), CURRENCY);
    assertEquals(test.getCurrency(), sensiCurrency);
    assertEquals(test.getDate(), DATE);
    assertEquals(test.getSensitivity(), VALUE);
  }

  public void test_of_withoutSensitivityCurrency() {
    RepoCurveZeroRateSensitivity test = RepoCurveZeroRateSensitivity.of(CURRENCY, DATE, GROUP, VALUE);
    assertEquals(test.getBondGroup(), GROUP);
    assertEquals(test.getCurveCurrency(), CURRENCY);
    assertEquals(test.getCurrency(), CURRENCY);
    assertEquals(test.getDate(), DATE);
    assertEquals(test.getSensitivity(), VALUE);
  }

  public void test_of_zeroRateSensitivity() {
    Currency sensiCurrency = GBP;
    ZeroRateSensitivity zeroSensi = ZeroRateSensitivity.of(CURRENCY, DATE, sensiCurrency, VALUE);
    RepoCurveZeroRateSensitivity test = RepoCurveZeroRateSensitivity.of(zeroSensi, GROUP);
    assertEquals(test.getBondGroup(), GROUP);
    assertEquals(test.getCurveCurrency(), CURRENCY);
    assertEquals(test.getCurrency(), sensiCurrency);
    assertEquals(test.getDate(), DATE);
    assertEquals(test.getSensitivity(), VALUE);
  }

  //-------------------------------------------------------------------------
  public void test_withCurrency() {
    RepoCurveZeroRateSensitivity base = RepoCurveZeroRateSensitivity.of(CURRENCY, DATE, GROUP, VALUE);
    RepoCurveZeroRateSensitivity test = base.withCurrency(GBP);
    assertEquals(test.getBondGroup(), GROUP);
    assertEquals(test.getCurveCurrency(), CURRENCY);
    assertEquals(test.getCurrency(), GBP);
    assertEquals(test.getDate(), DATE);
    assertEquals(test.getSensitivity(), VALUE);
  }

  public void test_withSensitivity() {
    RepoCurveZeroRateSensitivity base = RepoCurveZeroRateSensitivity.of(CURRENCY, DATE, GROUP, VALUE);
    double newValue = 53d;
    RepoCurveZeroRateSensitivity test = base.withSensitivity(newValue);
    assertEquals(test.getBondGroup(), GROUP);
    assertEquals(test.getCurveCurrency(), CURRENCY);
    assertEquals(test.getCurrency(), CURRENCY);
    assertEquals(test.getDate(), DATE);
    assertEquals(test.getSensitivity(), newValue);
  }

  public void test_compareKey() {
    RepoCurveZeroRateSensitivity a1 = RepoCurveZeroRateSensitivity.of(CURRENCY, DATE, GROUP, VALUE);
    RepoCurveZeroRateSensitivity a2 = RepoCurveZeroRateSensitivity.of(CURRENCY, DATE, GROUP, VALUE);
    RepoCurveZeroRateSensitivity b = RepoCurveZeroRateSensitivity.of(GBP, DATE, GROUP, VALUE);
    RepoCurveZeroRateSensitivity c = RepoCurveZeroRateSensitivity.of(CURRENCY, DATE2, GROUP, VALUE);
    RepoCurveZeroRateSensitivity d =
        RepoCurveZeroRateSensitivity.of(CURRENCY, DATE, BondGroup.of("ISSUER1 BND 3Y"), VALUE);
    IborRateSensitivity other =
        IborRateSensitivity.of(IborIndexObservation.of(GBP_LIBOR_3M, DATE, REF_DATA), 32d);
    assertEquals(a1.compareKey(a2), 0);
    assertEquals(a1.compareKey(b) > 0, true);
    assertEquals(b.compareKey(a1) < 0, true);
    assertEquals(a1.compareKey(c) < 0, true);
    assertEquals(c.compareKey(a1) > 0, true);
    assertEquals(a1.compareKey(d) < 0, true);
    assertEquals(d.compareKey(a1) > 0, true);
    assertEquals(a1.compareKey(other) > 0, true);
    assertEquals(other.compareKey(a1) < 0, true);
  }

  public void test_convertedTo() {
    RepoCurveZeroRateSensitivity base = RepoCurveZeroRateSensitivity.of(CURRENCY, DATE, GROUP, VALUE);
    double rate = 1.5d;
    FxMatrix matrix = FxMatrix.of(CurrencyPair.of(GBP, USD), rate);
    RepoCurveZeroRateSensitivity test1 = base.convertedTo(USD, matrix);
    assertEquals(test1, base);
    RepoCurveZeroRateSensitivity test2 = base.convertedTo(GBP, matrix);
    RepoCurveZeroRateSensitivity expected = RepoCurveZeroRateSensitivity.of(CURRENCY, DATE, GBP, GROUP, VALUE / rate);
    assertEquals(test2, expected);
  }

  //-------------------------------------------------------------------------
  public void test_multipliedBy() {
    RepoCurveZeroRateSensitivity base = RepoCurveZeroRateSensitivity.of(CURRENCY, DATE, GROUP, VALUE);
    double rate = 2.4d;
    RepoCurveZeroRateSensitivity test = base.multipliedBy(rate);
    RepoCurveZeroRateSensitivity expected = RepoCurveZeroRateSensitivity.of(CURRENCY, DATE, GROUP, VALUE * rate);
    assertEquals(test, expected);
  }

  public void test_mapSensitivity() {
    RepoCurveZeroRateSensitivity base = RepoCurveZeroRateSensitivity.of(CURRENCY, DATE, GROUP, VALUE);
    RepoCurveZeroRateSensitivity expected = RepoCurveZeroRateSensitivity.of(CURRENCY, DATE, GROUP, 1d / VALUE);
    RepoCurveZeroRateSensitivity test = base.mapSensitivity(s -> 1d / s);
    assertEquals(test, expected);
  }

  public void test_normalize() {
    RepoCurveZeroRateSensitivity base = RepoCurveZeroRateSensitivity.of(CURRENCY, DATE, GROUP, VALUE);
    RepoCurveZeroRateSensitivity test = base.normalize();
    assertEquals(test, base);
  }

  public void test_buildInto() {
    RepoCurveZeroRateSensitivity base = RepoCurveZeroRateSensitivity.of(CURRENCY, DATE, GROUP, VALUE);
    MutablePointSensitivities combo = new MutablePointSensitivities();
    MutablePointSensitivities test = base.buildInto(combo);
    assertSame(test, combo);
    assertEquals(test.getSensitivities(), ImmutableList.of(base));
  }

  public void test_cloned() {
    RepoCurveZeroRateSensitivity base = RepoCurveZeroRateSensitivity.of(CURRENCY, DATE, GROUP, VALUE);
    RepoCurveZeroRateSensitivity test = base.cloned();
    assertEquals(test, base);
  }

  public void test_createZeroRateSensitivity() {
    RepoCurveZeroRateSensitivity base = RepoCurveZeroRateSensitivity.of(CURRENCY, DATE, GBP, GROUP, VALUE);
    ZeroRateSensitivity expected = ZeroRateSensitivity.of(CURRENCY, DATE, GBP, VALUE);
    ZeroRateSensitivity test = base.createZeroRateSensitivity();
    assertEquals(test, expected);
  }

  //-------------------------------------------------------------------------
  public void coverage() {
    RepoCurveZeroRateSensitivity test1 = RepoCurveZeroRateSensitivity.of(CURRENCY, DATE, GROUP, VALUE);
    coverImmutableBean(test1);
    RepoCurveZeroRateSensitivity test2 =
        RepoCurveZeroRateSensitivity.of(GBP, date(2014, 3, 24), BondGroup.of("ISSUER2 BND 5Y"), 12d);
    coverBeanEquals(test1, test2);
  }

  public void test_serialization() {
    RepoCurveZeroRateSensitivity test = RepoCurveZeroRateSensitivity.of(CURRENCY, DATE, GROUP, VALUE);
    assertSerialization(test);
  }

}
