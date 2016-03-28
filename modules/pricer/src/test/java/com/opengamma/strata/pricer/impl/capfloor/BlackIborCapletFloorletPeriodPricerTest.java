/**
 * Copyright (C) 2016 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.pricer.impl.capfloor;

import static com.opengamma.strata.basics.currency.Currency.EUR;
import static com.opengamma.strata.basics.index.IborIndices.EUR_EURIBOR_3M;
import static com.opengamma.strata.collect.TestHelper.assertThrowsIllegalArg;
import static com.opengamma.strata.collect.TestHelper.dateUtc;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.function.Function;

import org.testng.annotations.Test;

import com.opengamma.strata.basics.currency.CurrencyAmount;
import com.opengamma.strata.basics.market.ReferenceData;
import com.opengamma.strata.collect.DoubleArrayMath;
import com.opengamma.strata.collect.array.DoubleArray;
import com.opengamma.strata.collect.timeseries.LocalDateDoubleTimeSeries;
import com.opengamma.strata.market.curve.CurveCurrencyParameterSensitivities;
import com.opengamma.strata.market.sensitivity.PointSensitivityBuilder;
import com.opengamma.strata.market.surface.InterpolatedNodalSurface;
import com.opengamma.strata.market.surface.SurfaceCurrencyParameterSensitivity;
import com.opengamma.strata.market.view.IborCapletFloorletVolatilities;
import com.opengamma.strata.pricer.capfloor.BlackIborCapletFloorletExpiryStrikeVolatilities;
import com.opengamma.strata.pricer.capfloor.NormalIborCapletFloorletExpiryStrikeVolatilities;
import com.opengamma.strata.pricer.impl.option.BlackFormulaRepository;
import com.opengamma.strata.pricer.impl.swap.DiscountingRatePaymentPeriodPricer;
import com.opengamma.strata.pricer.rate.ImmutableRatesProvider;
import com.opengamma.strata.pricer.sensitivity.RatesFiniteDifferenceSensitivityCalculator;
import com.opengamma.strata.product.capfloor.IborCapletFloorletPeriod;
import com.opengamma.strata.product.rate.FixedRateObservation;
import com.opengamma.strata.product.rate.IborRateObservation;
import com.opengamma.strata.product.swap.RateAccrualPeriod;
import com.opengamma.strata.product.swap.RatePaymentPeriod;

/**
 * Test {@link BlackIborCapletFloorletPeriodPricer}.
 */
@Test
public class BlackIborCapletFloorletPeriodPricerTest {

  private static final ReferenceData REF_DATA = ReferenceData.standard();
  private static final ZonedDateTime VALUATION = dateUtc(2008, 8, 18);
  private static final LocalDate FIXING = LocalDate.of(2011, 1, 3);
  private static final double NOTIONAL = 1000000; //1m
  private static final double STRIKE = 0.01;
  private static final IborRateObservation RATE_OBS = IborRateObservation.of(EUR_EURIBOR_3M, FIXING, REF_DATA);
  private static final IborCapletFloorletPeriod CAPLET_LONG = IborCapletFloorletPeriod.builder()
      .caplet(STRIKE)
      .startDate(RATE_OBS.getEffectiveDate())
      .endDate(RATE_OBS.getMaturityDate())
      .yearFraction(RATE_OBS.getYearFraction())
      .notional(NOTIONAL)
      .iborRate(RATE_OBS)
      .build();
  private static final IborCapletFloorletPeriod CAPLET_SHORT = IborCapletFloorletPeriod.builder()
      .caplet(STRIKE)
      .startDate(RATE_OBS.getEffectiveDate())
      .endDate(RATE_OBS.getMaturityDate())
      .yearFraction(RATE_OBS.getYearFraction())
      .notional(-NOTIONAL)
      .iborRate(RATE_OBS)
      .build();
  private static final IborCapletFloorletPeriod FLOORLET_LONG = IborCapletFloorletPeriod.builder()
      .floorlet(STRIKE)
      .startDate(RATE_OBS.getEffectiveDate())
      .endDate(RATE_OBS.getMaturityDate())
      .yearFraction(RATE_OBS.getYearFraction())
      .notional(NOTIONAL)
      .iborRate(RATE_OBS)
      .build();
  private static final IborCapletFloorletPeriod FLOORLET_SHORT = IborCapletFloorletPeriod.builder()
      .floorlet(STRIKE)
      .startDate(RATE_OBS.getEffectiveDate())
      .endDate(RATE_OBS.getMaturityDate())
      .yearFraction(RATE_OBS.getYearFraction())
      .notional(-NOTIONAL)
      .iborRate(RATE_OBS)
      .build();
  private static final RateAccrualPeriod IBOR_PERIOD = RateAccrualPeriod.builder()
      .startDate(CAPLET_LONG.getStartDate())
      .endDate(CAPLET_LONG.getEndDate())
      .yearFraction(CAPLET_LONG.getYearFraction())
      .rateObservation(RATE_OBS)
      .build();
  private static final RatePaymentPeriod IBOR_COUPON = RatePaymentPeriod.builder()
      .accrualPeriods(IBOR_PERIOD)
      .paymentDate(CAPLET_LONG.getPaymentDate())
      .dayCount(EUR_EURIBOR_3M.getDayCount())
      .notional(NOTIONAL)
      .currency(EUR)
      .build();
  private static final RateAccrualPeriod FIXED_PERIOD = RateAccrualPeriod.builder()
      .startDate(CAPLET_LONG.getStartDate())
      .endDate(CAPLET_LONG.getEndDate())
      .rateObservation(FixedRateObservation.of(STRIKE))
      .yearFraction(CAPLET_LONG.getYearFraction())
      .build();
  private static final RatePaymentPeriod FIXED_COUPON = RatePaymentPeriod.builder()
      .accrualPeriods(FIXED_PERIOD)
      .paymentDate(CAPLET_LONG.getPaymentDate())
      .dayCount(EUR_EURIBOR_3M.getDayCount())
      .notional(NOTIONAL)
      .currency(EUR)
      .build();
  private static final RateAccrualPeriod FIXED_PERIOD_UNIT = RateAccrualPeriod.builder()
      .startDate(CAPLET_LONG.getStartDate())
      .endDate(CAPLET_LONG.getEndDate())
      .rateObservation(FixedRateObservation.of(1d))
      .yearFraction(CAPLET_LONG.getYearFraction())
      .build();
  private static final RatePaymentPeriod FIXED_COUPON_UNIT = RatePaymentPeriod.builder()
      .accrualPeriods(FIXED_PERIOD_UNIT)
      .paymentDate(CAPLET_LONG.getPaymentDate())
      .dayCount(EUR_EURIBOR_3M.getDayCount())
      .notional(NOTIONAL)
      .currency(EUR)
      .build();
  // valuation date before fixing date
  private static final ImmutableRatesProvider RATES =
      IborCapletFloorletDataSet.createRatesProvider(VALUATION.toLocalDate());
  private static final BlackIborCapletFloorletExpiryStrikeVolatilities VOLS = IborCapletFloorletDataSet
      .createBlackVolatilitiesProvider(VALUATION, EUR_EURIBOR_3M);
  // valuation date equal to fixing date
  private static final double OBS_INDEX = 0.013;
  private static final LocalDateDoubleTimeSeries TIME_SERIES = LocalDateDoubleTimeSeries.of(FIXING, OBS_INDEX);
  private static final ImmutableRatesProvider RATES_ON_FIX =
      IborCapletFloorletDataSet.createRatesProvider(FIXING, EUR_EURIBOR_3M, TIME_SERIES);
  private static final BlackIborCapletFloorletExpiryStrikeVolatilities VOLS_ON_FIX = IborCapletFloorletDataSet
      .createBlackVolatilitiesProvider(FIXING.atStartOfDay(ZoneOffset.UTC), EUR_EURIBOR_3M);
  // valuation date after fixing date
  private static final ImmutableRatesProvider RATES_AFTER_FIX =
      IborCapletFloorletDataSet.createRatesProvider(FIXING.plusWeeks(1), EUR_EURIBOR_3M, TIME_SERIES);
  private static final BlackIborCapletFloorletExpiryStrikeVolatilities VOLS_AFTER_FIX = IborCapletFloorletDataSet
      .createBlackVolatilitiesProvider(FIXING.plusWeeks(1).atStartOfDay(ZoneOffset.UTC), EUR_EURIBOR_3M);
  // valuation date after payment date
  private static final LocalDate DATE_AFTER_PAY = LocalDate.of(2011, 5, 2);
  private static final ImmutableRatesProvider RATES_AFTER_PAY =
      IborCapletFloorletDataSet.createRatesProvider(DATE_AFTER_PAY, EUR_EURIBOR_3M, TIME_SERIES);
  private static final BlackIborCapletFloorletExpiryStrikeVolatilities VOLS_AFTER_PAY = IborCapletFloorletDataSet
      .createBlackVolatilitiesProvider(DATE_AFTER_PAY.plusWeeks(1).atStartOfDay(ZoneOffset.UTC), EUR_EURIBOR_3M);
  // normal vols
  private static final NormalIborCapletFloorletExpiryStrikeVolatilities VOLS_NORMAL = IborCapletFloorletDataSet
      .createNormalVolatilitiesProvider(VALUATION, EUR_EURIBOR_3M);

  private static final double TOL = 1.0e-14;
  private static final double EPS_FD = 1.0e-6;
  private static final BlackIborCapletFloorletPeriodPricer PRICER = BlackIborCapletFloorletPeriodPricer.DEFAULT;
  private static final VolatilityIborCapletFloorletPeriodPricer PRICER_BASE = VolatilityIborCapletFloorletPeriodPricer.DEFAULT;
  private static final DiscountingRatePaymentPeriodPricer PRICER_COUPON = DiscountingRatePaymentPeriodPricer.DEFAULT;
  private static final RatesFiniteDifferenceSensitivityCalculator FD_CAL = new RatesFiniteDifferenceSensitivityCalculator(EPS_FD);

  //-------------------------------------------------------------------------
  public void test_presentValue_formula() {
    CurrencyAmount computedCaplet = PRICER.presentValue(CAPLET_LONG, RATES, VOLS);
    CurrencyAmount computedFloorlet = PRICER.presentValue(FLOORLET_SHORT, RATES, VOLS);
    double forward = RATES.iborIndexRates(EUR_EURIBOR_3M).rate(RATE_OBS.getObservation());
    double expiry = VOLS.relativeTime(CAPLET_LONG.getFixingDateTime());
    double volatility = VOLS.volatility(expiry, STRIKE, forward);
    double df = RATES.discountFactor(EUR, CAPLET_LONG.getPaymentDate());
    double expectedCaplet = NOTIONAL * df * CAPLET_LONG.getYearFraction() *
        BlackFormulaRepository.price(forward, STRIKE, expiry, volatility, true);
    double expectedFloorlet = -NOTIONAL * df * CAPLET_LONG.getYearFraction() *
        BlackFormulaRepository.price(forward, STRIKE, expiry, volatility, false);
    assertEquals(computedCaplet.getCurrency(), EUR);
    assertEquals(computedCaplet.getAmount(), expectedCaplet, NOTIONAL * TOL);
    assertEquals(computedFloorlet.getCurrency(), EUR);
    assertEquals(computedFloorlet.getAmount(), expectedFloorlet, NOTIONAL * TOL);
  }

  public void test_presentValue_parity() {
    double capletLong = PRICER.presentValue(CAPLET_LONG, RATES, VOLS).getAmount();
    double capletShort = PRICER.presentValue(CAPLET_SHORT, RATES, VOLS).getAmount();
    double floorletLong = PRICER.presentValue(FLOORLET_LONG, RATES, VOLS).getAmount();
    double floorletShort = PRICER.presentValue(FLOORLET_SHORT, RATES, VOLS).getAmount();
    double iborCoupon = PRICER_COUPON.presentValue(IBOR_COUPON, RATES);
    double fixedCoupon = PRICER_COUPON.presentValue(FIXED_COUPON, RATES);
    assertEquals(capletLong, -capletShort, NOTIONAL * TOL);
    assertEquals(floorletLong, -floorletShort, NOTIONAL * TOL);
    assertEquals(capletLong - floorletLong, iborCoupon - fixedCoupon, NOTIONAL * TOL);
    assertEquals(capletShort - floorletShort, -iborCoupon + fixedCoupon, NOTIONAL * TOL);
  }

  public void test_presentValue_onFix() {
    CurrencyAmount computedCaplet = PRICER.presentValue(CAPLET_LONG, RATES_ON_FIX, VOLS_ON_FIX);
    CurrencyAmount computedFloorlet = PRICER.presentValue(FLOORLET_SHORT, RATES_ON_FIX, VOLS_ON_FIX);
    double expectedCaplet = PRICER_COUPON.presentValue(FIXED_COUPON_UNIT, RATES_ON_FIX) * (OBS_INDEX - STRIKE);
    double expectedFloorlet = 0d;
    assertEquals(computedCaplet.getCurrency(), EUR);
    assertEquals(computedCaplet.getAmount(), expectedCaplet, NOTIONAL * TOL);
    assertEquals(computedFloorlet.getCurrency(), EUR);
    assertEquals(computedFloorlet.getAmount(), expectedFloorlet, NOTIONAL * TOL);
  }

  public void test_presentValue_afterFix() {
    CurrencyAmount computedCaplet = PRICER.presentValue(CAPLET_LONG, RATES_AFTER_FIX, VOLS_AFTER_FIX);
    CurrencyAmount computedFloorlet = PRICER.presentValue(FLOORLET_SHORT, RATES_AFTER_FIX, VOLS_AFTER_FIX);
    double payoff = (OBS_INDEX - STRIKE) * PRICER_COUPON.presentValue(FIXED_COUPON_UNIT, RATES_AFTER_FIX);
    assertEquals(computedCaplet.getCurrency(), EUR);
    assertEquals(computedCaplet.getAmount(), payoff, NOTIONAL * TOL);
    assertEquals(computedFloorlet.getCurrency(), EUR);
    assertEquals(computedFloorlet.getAmount(), 0d, NOTIONAL * TOL);
  }

  public void test_presentValue_afterPay() {
    CurrencyAmount computedCaplet = PRICER.presentValue(CAPLET_LONG, RATES_AFTER_PAY, VOLS_AFTER_PAY);
    CurrencyAmount computedFloorlet = PRICER.presentValue(FLOORLET_SHORT, RATES_AFTER_PAY, VOLS_AFTER_PAY);
    assertEquals(computedCaplet.getCurrency(), EUR);
    assertEquals(computedCaplet.getAmount(), 0d, NOTIONAL * TOL);
    assertEquals(computedFloorlet.getCurrency(), EUR);
    assertEquals(computedFloorlet.getAmount(), 0d, NOTIONAL * TOL);
  }

  //-------------------------------------------------------------------------
  public void test_impliedVolatility() {
    double computed = PRICER.impliedVolatility(CAPLET_LONG, RATES, VOLS);
    double expiry = VOLS.relativeTime(CAPLET_LONG.getFixingDateTime());
    double expected = VOLS.getSurface().zValue(expiry, STRIKE);
    assertEquals(computed, expected, TOL);
  }

  public void test_impliedVolatility_onFix() {
    double computed = PRICER.impliedVolatility(CAPLET_LONG, RATES_ON_FIX, VOLS_ON_FIX);
    double expected = VOLS_ON_FIX.getSurface().zValue(0d, STRIKE);
    assertEquals(computed, expected, TOL);
  }

  public void test_impliedVolatility_afterFix() {
    assertThrowsIllegalArg(() -> PRICER.impliedVolatility(CAPLET_LONG, RATES_AFTER_FIX, VOLS_AFTER_FIX));
  }

  //-------------------------------------------------------------------------
  public void test_presentValueDelta_formula() {
    CurrencyAmount computedCaplet = PRICER.presentValueDelta(CAPLET_LONG, RATES, VOLS);
    CurrencyAmount computedFloorlet = PRICER.presentValueDelta(FLOORLET_SHORT, RATES, VOLS);
    double forward = RATES.iborIndexRates(EUR_EURIBOR_3M).rate(RATE_OBS.getObservation());
    double expiry = VOLS.relativeTime(CAPLET_LONG.getFixingDateTime());
    double volatility = VOLS.volatility(expiry, STRIKE, forward);
    double df = RATES.discountFactor(EUR, CAPLET_LONG.getPaymentDate());
    double expectedCaplet = NOTIONAL * df * CAPLET_LONG.getYearFraction() *
        BlackFormulaRepository.delta(forward, STRIKE, expiry, volatility, true);
    double expectedFloorlet = -NOTIONAL * df * CAPLET_LONG.getYearFraction() *
        BlackFormulaRepository.delta(forward, STRIKE, expiry, volatility, false);
    assertEquals(computedCaplet.getCurrency(), EUR);
    assertEquals(computedCaplet.getAmount(), expectedCaplet, NOTIONAL * TOL);
    assertEquals(computedFloorlet.getCurrency(), EUR);
    assertEquals(computedFloorlet.getAmount(), expectedFloorlet, NOTIONAL * TOL);
  }

  public void test_presentValueDelta_parity() {
    double capletLong = PRICER.presentValueDelta(CAPLET_LONG, RATES, VOLS).getAmount();
    double capletShort = PRICER.presentValueDelta(CAPLET_SHORT, RATES, VOLS).getAmount();
    double floorletLong = PRICER.presentValueDelta(FLOORLET_LONG, RATES, VOLS).getAmount();
    double floorletShort = PRICER.presentValueDelta(FLOORLET_SHORT, RATES, VOLS).getAmount();
    double unitCoupon = PRICER_COUPON.presentValue(FIXED_COUPON_UNIT, RATES);
    assertEquals(capletLong, -capletShort, NOTIONAL * TOL);
    assertEquals(floorletLong, -floorletShort, NOTIONAL * TOL);
    assertEquals(capletLong - floorletLong, unitCoupon, NOTIONAL * TOL);
    assertEquals(capletShort - floorletShort, -unitCoupon, NOTIONAL * TOL);
  }

  public void test_presentValueDelta_onFix() {
    CurrencyAmount computedCaplet = PRICER.presentValueDelta(CAPLET_LONG, RATES_ON_FIX, VOLS_ON_FIX);
    CurrencyAmount computedFloorlet = PRICER.presentValueDelta(FLOORLET_SHORT, RATES_ON_FIX, VOLS_ON_FIX);
    double expectedCaplet = PRICER_COUPON.presentValue(FIXED_COUPON_UNIT, RATES_ON_FIX);
    double expectedFloorlet = 0d;
    assertEquals(computedCaplet.getCurrency(), EUR);
    assertEquals(computedCaplet.getAmount(), expectedCaplet, TOL);
    assertEquals(computedFloorlet.getCurrency(), EUR);
    assertEquals(computedFloorlet.getAmount(), expectedFloorlet, TOL);
  }

  public void test_presentValueDelta_afterFix() {
    CurrencyAmount computedCaplet = PRICER.presentValueDelta(CAPLET_LONG, RATES_AFTER_FIX, VOLS_AFTER_FIX);
    CurrencyAmount computedFloorlet = PRICER.presentValueDelta(FLOORLET_SHORT, RATES_AFTER_FIX, VOLS_AFTER_FIX);
    assertEquals(computedCaplet.getCurrency(), EUR);
    assertEquals(computedCaplet.getAmount(), 0d, TOL);
    assertEquals(computedFloorlet.getCurrency(), EUR);
    assertEquals(computedFloorlet.getAmount(), 0d, TOL);
  }

  //-------------------------------------------------------------------------
  public void test_presentValueGamma_formula() {
    CurrencyAmount computedCaplet = PRICER.presentValueGamma(CAPLET_LONG, RATES, VOLS);
    CurrencyAmount computedFloorlet = PRICER.presentValueGamma(FLOORLET_SHORT, RATES, VOLS);
    double forward = RATES.iborIndexRates(EUR_EURIBOR_3M).rate(RATE_OBS.getObservation());
    double expiry = VOLS.relativeTime(CAPLET_LONG.getFixingDateTime());
    double volatility = VOLS.volatility(expiry, STRIKE, forward);
    double df = RATES.discountFactor(EUR, CAPLET_LONG.getPaymentDate());
    double expectedCaplet = NOTIONAL * df * CAPLET_LONG.getYearFraction() *
        BlackFormulaRepository.gamma(forward, STRIKE, expiry, volatility);
    double expectedFloorlet = -NOTIONAL * df * CAPLET_LONG.getYearFraction() *
        BlackFormulaRepository.gamma(forward, STRIKE, expiry, volatility);
    assertEquals(computedCaplet.getCurrency(), EUR);
    assertEquals(computedCaplet.getAmount(), expectedCaplet, NOTIONAL * TOL);
    assertEquals(computedFloorlet.getCurrency(), EUR);
    assertEquals(computedFloorlet.getAmount(), expectedFloorlet, NOTIONAL * TOL);
  }

  public void test_presentValueGamma_onFix() {
    CurrencyAmount computedCaplet = PRICER.presentValueGamma(CAPLET_LONG, RATES_ON_FIX, VOLS_ON_FIX);
    CurrencyAmount computedFloorlet = PRICER.presentValueGamma(FLOORLET_SHORT, RATES_ON_FIX, VOLS_ON_FIX);
    double expectedCaplet = 0d;
    double expectedFloorlet = 0d;
    assertEquals(computedCaplet.getCurrency(), EUR);
    assertEquals(computedCaplet.getAmount(), expectedCaplet, TOL);
    assertEquals(computedFloorlet.getCurrency(), EUR);
    assertEquals(computedFloorlet.getAmount(), expectedFloorlet, TOL);
  }

  public void test_presentValueGamma_afterFix() {
    CurrencyAmount computedCaplet = PRICER.presentValueGamma(CAPLET_LONG, RATES_AFTER_FIX, VOLS_AFTER_FIX);
    CurrencyAmount computedFloorlet = PRICER.presentValueGamma(FLOORLET_SHORT, RATES_AFTER_FIX, VOLS_AFTER_FIX);
    assertEquals(computedCaplet.getCurrency(), EUR);
    assertEquals(computedCaplet.getAmount(), 0d, TOL);
    assertEquals(computedFloorlet.getCurrency(), EUR);
    assertEquals(computedFloorlet.getAmount(), 0d, TOL);
  }

  //-------------------------------------------------------------------------
  public void test_presentValueTheta_formula() {
    CurrencyAmount computedCaplet = PRICER.presentValueTheta(CAPLET_LONG, RATES, VOLS);
    CurrencyAmount computedFloorlet = PRICER.presentValueTheta(FLOORLET_SHORT, RATES, VOLS);
    double forward = RATES.iborIndexRates(EUR_EURIBOR_3M).rate(RATE_OBS.getObservation());
    double expiry = VOLS.relativeTime(CAPLET_LONG.getFixingDateTime());
    double volatility = VOLS.volatility(expiry, STRIKE, forward);
    double df = RATES.discountFactor(EUR, CAPLET_LONG.getPaymentDate());
    double expectedCaplet = NOTIONAL * df * CAPLET_LONG.getYearFraction() *
        BlackFormulaRepository.driftlessTheta(forward, STRIKE, expiry, volatility);
    double expectedFloorlet = -NOTIONAL * df * CAPLET_LONG.getYearFraction() *
        BlackFormulaRepository.driftlessTheta(forward, STRIKE, expiry, volatility);
    assertEquals(computedCaplet.getCurrency(), EUR);
    assertEquals(computedCaplet.getAmount(), expectedCaplet, NOTIONAL * TOL);
    assertEquals(computedFloorlet.getCurrency(), EUR);
    assertEquals(computedFloorlet.getAmount(), expectedFloorlet, NOTIONAL * TOL);
  }

  public void test_presentValueTheta_parity() {
    double capletLong = PRICER.presentValueTheta(CAPLET_LONG, RATES, VOLS).getAmount();
    double capletShort = PRICER.presentValueTheta(CAPLET_SHORT, RATES, VOLS).getAmount();
    double floorletLong = PRICER.presentValueTheta(FLOORLET_LONG, RATES, VOLS).getAmount();
    double floorletShort = PRICER.presentValueTheta(FLOORLET_SHORT, RATES, VOLS).getAmount();
    assertEquals(capletLong, -capletShort, NOTIONAL * TOL);
    assertEquals(floorletLong, -floorletShort, NOTIONAL * TOL);
    assertEquals(capletLong, floorletLong, NOTIONAL * TOL);
    assertEquals(capletShort, floorletShort, NOTIONAL * TOL);
  }

  public void test_presentValueTheta_onFix() {
    CurrencyAmount computedCaplet = PRICER.presentValueTheta(CAPLET_LONG, RATES_ON_FIX, VOLS_ON_FIX);
    CurrencyAmount computedFloorlet = PRICER.presentValueTheta(FLOORLET_SHORT, RATES_ON_FIX, VOLS_ON_FIX);
    double expectedCaplet = 0d;
    double expectedFloorlet = 0d;
    assertEquals(computedCaplet.getCurrency(), EUR);
    assertEquals(computedCaplet.getAmount(), expectedCaplet, TOL);
    assertEquals(computedFloorlet.getCurrency(), EUR);
    assertEquals(computedFloorlet.getAmount(), expectedFloorlet, TOL);
  }

  public void test_presentValueTheta_afterFix() {
    CurrencyAmount computedCaplet = PRICER.presentValueTheta(CAPLET_LONG, RATES_AFTER_FIX, VOLS_AFTER_FIX);
    CurrencyAmount computedFloorlet = PRICER.presentValueTheta(FLOORLET_SHORT, RATES_AFTER_FIX, VOLS_AFTER_FIX);
    assertEquals(computedCaplet.getCurrency(), EUR);
    assertEquals(computedCaplet.getAmount(), 0d, TOL);
    assertEquals(computedFloorlet.getCurrency(), EUR);
    assertEquals(computedFloorlet.getAmount(), 0d, TOL);
  }

  //-------------------------------------------------------------------------
  public void test_presentValueSensitivity() {
    PointSensitivityBuilder pointCaplet = PRICER.presentValueSensitivity(CAPLET_LONG, RATES, VOLS);
    CurveCurrencyParameterSensitivities computedCaplet = RATES.curveParameterSensitivity(pointCaplet.build());
    PointSensitivityBuilder pointFloorlet = PRICER.presentValueSensitivity(FLOORLET_SHORT, RATES, VOLS);
    CurveCurrencyParameterSensitivities computedFloorlet = RATES.curveParameterSensitivity(pointFloorlet.build());
    CurveCurrencyParameterSensitivities expectedCaplet =
        FD_CAL.sensitivity(RATES, p -> PRICER_BASE.presentValue(CAPLET_LONG, p, VOLS));
    CurveCurrencyParameterSensitivities expectedFloorlet =
        FD_CAL.sensitivity(RATES, p -> PRICER_BASE.presentValue(FLOORLET_SHORT, p, VOLS));
    assertTrue(computedCaplet.equalWithTolerance(expectedCaplet, EPS_FD * NOTIONAL * 50d));
    assertTrue(computedFloorlet.equalWithTolerance(expectedFloorlet, EPS_FD * NOTIONAL * 50d));
  }

  public void test_presentValueSensitivity_onFix() {
    PointSensitivityBuilder pointCaplet = PRICER.presentValueSensitivity(CAPLET_LONG, RATES_ON_FIX, VOLS_ON_FIX);
    CurveCurrencyParameterSensitivities computedCaplet = RATES_ON_FIX.curveParameterSensitivity(pointCaplet.build());
    PointSensitivityBuilder pointFloorlet = PRICER.presentValueSensitivity(FLOORLET_SHORT, RATES_ON_FIX, VOLS_ON_FIX);
    CurveCurrencyParameterSensitivities computedFloorlet = RATES_ON_FIX.curveParameterSensitivity(pointFloorlet.build());
    CurveCurrencyParameterSensitivities expectedCaplet =
        FD_CAL.sensitivity(RATES_ON_FIX, p -> PRICER_BASE.presentValue(CAPLET_LONG, p, VOLS_ON_FIX));
    CurveCurrencyParameterSensitivities expectedFloorlet =
        FD_CAL.sensitivity(RATES_ON_FIX, p -> PRICER_BASE.presentValue(FLOORLET_SHORT, p, VOLS_ON_FIX));
    assertTrue(computedCaplet.equalWithTolerance(expectedCaplet, EPS_FD * NOTIONAL));
    assertTrue(computedFloorlet.equalWithTolerance(expectedFloorlet, EPS_FD * NOTIONAL));
  }

  public void test_presentValueSensitivity_afterFix() {
    PointSensitivityBuilder pointCaplet = PRICER.presentValueSensitivity(CAPLET_LONG, RATES_AFTER_FIX, VOLS_AFTER_FIX);
    CurveCurrencyParameterSensitivities computedCaplet = RATES_AFTER_FIX.curveParameterSensitivity(pointCaplet.build());
    PointSensitivityBuilder pointFloorlet =
        PRICER.presentValueSensitivity(FLOORLET_SHORT, RATES_AFTER_FIX, VOLS_AFTER_FIX);
    CurveCurrencyParameterSensitivities computedFloorlet =
        RATES_AFTER_FIX.curveParameterSensitivity(pointFloorlet.build());
    CurveCurrencyParameterSensitivities expectedCaplet =
        FD_CAL.sensitivity(RATES_AFTER_FIX, p -> PRICER_BASE.presentValue(CAPLET_LONG, p, VOLS_AFTER_FIX));
    CurveCurrencyParameterSensitivities expectedFloorlet =
        FD_CAL.sensitivity(RATES_AFTER_FIX, p -> PRICER_BASE.presentValue(FLOORLET_SHORT, p, VOLS_AFTER_FIX));
    assertTrue(computedCaplet.equalWithTolerance(expectedCaplet, EPS_FD * NOTIONAL));
    assertTrue(computedFloorlet.equalWithTolerance(expectedFloorlet, EPS_FD * NOTIONAL));
  }

  public void test_presentValueSensitivity_afterPay() {
    PointSensitivityBuilder computedCaplet =
        PRICER.presentValueSensitivity(CAPLET_LONG, RATES_AFTER_PAY, VOLS_AFTER_PAY);
    PointSensitivityBuilder computedFloorlet =
        PRICER.presentValueSensitivity(FLOORLET_SHORT, RATES_AFTER_PAY, VOLS_AFTER_PAY);
    assertEquals(computedCaplet, PointSensitivityBuilder.none());
    assertEquals(computedFloorlet, PointSensitivityBuilder.none());
  }

  //-------------------------------------------------------------------------
  public void test_presentValueSensitivityVolatility() {
    PointSensitivityBuilder pointCaplet = PRICER.presentValueSensitivityVolatility(CAPLET_LONG, RATES, VOLS);
    SurfaceCurrencyParameterSensitivity computedCaplet =
        VOLS.surfaceCurrencyParameterSensitivity(pointCaplet.build()).getSensitivities().get(0);
    PointSensitivityBuilder pointFloorlet = PRICER.presentValueSensitivityVolatility(FLOORLET_SHORT, RATES, VOLS);
    SurfaceCurrencyParameterSensitivity computedFloorlet =
        VOLS.surfaceCurrencyParameterSensitivity(pointFloorlet.build()).getSensitivities().get(0);
    testSurfaceSensitivity(computedCaplet, VOLS, v -> PRICER.presentValue(CAPLET_LONG, RATES, v));
    testSurfaceSensitivity(computedFloorlet, VOLS, v -> PRICER.presentValue(FLOORLET_SHORT, RATES, v));
  }

  private void testSurfaceSensitivity(
      SurfaceCurrencyParameterSensitivity computed,
      BlackIborCapletFloorletExpiryStrikeVolatilities vols,
      Function<IborCapletFloorletVolatilities, CurrencyAmount> valueFn) {
    double pvBase = valueFn.apply(vols).getAmount();
    InterpolatedNodalSurface surfaceBase = (InterpolatedNodalSurface) vols.getSurface();
    int nParams = surfaceBase.getParameterCount();
    for (int i = 0; i < nParams; i++) {
      DoubleArray zBumped = surfaceBase.getZValues().with(i, surfaceBase.getZValues().get(i) + EPS_FD);
      InterpolatedNodalSurface surfaceBumped = surfaceBase.withZValues(zBumped);
      BlackIborCapletFloorletExpiryStrikeVolatilities volsBumped = BlackIborCapletFloorletExpiryStrikeVolatilities
          .of(surfaceBumped, vols.getIndex(), vols.getValuationDateTime(), vols.getDayCount());
      double fd = (valueFn.apply(volsBumped).getAmount() - pvBase) / EPS_FD;
      assertEquals(computed.getSensitivity().get(i), fd, NOTIONAL * EPS_FD);
    }
  }

  public void test_presentValueSensitivityVolatility_onFix() {
    PointSensitivityBuilder computedCaplet =
        PRICER.presentValueSensitivityVolatility(CAPLET_LONG, RATES_ON_FIX, VOLS_ON_FIX);
    PointSensitivityBuilder computedFloorlet =
        PRICER.presentValueSensitivityVolatility(FLOORLET_SHORT, RATES_ON_FIX, VOLS_ON_FIX);
    assertEquals(computedCaplet, PointSensitivityBuilder.none());
    assertEquals(computedFloorlet, PointSensitivityBuilder.none());

  }

  public void test_presentValueSensitivityVolatility_afterFix() {
    PointSensitivityBuilder computedCaplet =
        PRICER.presentValueSensitivityVolatility(CAPLET_LONG, RATES_AFTER_FIX, VOLS_AFTER_FIX);
    PointSensitivityBuilder computedFloorlet =
        PRICER.presentValueSensitivityVolatility(FLOORLET_SHORT, RATES_AFTER_FIX, VOLS_AFTER_FIX);
    assertEquals(computedCaplet, PointSensitivityBuilder.none());
    assertEquals(computedFloorlet, PointSensitivityBuilder.none());
  }

  //-------------------------------------------------------------------------
  public void test_fail_normal() {
    assertThrowsIllegalArg(() -> PRICER.presentValue(CAPLET_LONG, RATES, VOLS_NORMAL));
    assertThrowsIllegalArg(() -> PRICER.impliedVolatility(CAPLET_LONG, RATES, VOLS_NORMAL));
    assertThrowsIllegalArg(() -> PRICER.presentValueDelta(CAPLET_LONG, RATES, VOLS_NORMAL));
    assertThrowsIllegalArg(() -> PRICER.presentValueGamma(CAPLET_LONG, RATES, VOLS_NORMAL));
    assertThrowsIllegalArg(() -> PRICER.presentValueTheta(CAPLET_LONG, RATES, VOLS_NORMAL));
    assertThrowsIllegalArg(() -> PRICER.presentValueSensitivity(CAPLET_LONG, RATES, VOLS_NORMAL));
    assertThrowsIllegalArg(() -> PRICER.presentValueSensitivityVolatility(CAPLET_LONG, RATES, VOLS_NORMAL));
  }

  //-------------------------------------------------------------------------
  private static final IborCapletFloorletPeriod CAPLET_REG = IborCapletFloorletPeriod.builder()
      .caplet(0.04)
      .startDate(RATE_OBS.getEffectiveDate())
      .endDate(RATE_OBS.getMaturityDate())
      .yearFraction(RATE_OBS.getYearFraction())
      .notional(NOTIONAL)
      .iborRate(RATE_OBS)
      .build();

  public void regression_pv() {
    CurrencyAmount pv = PRICER.presentValue(CAPLET_REG, RATES, VOLS);
    assertEquals(pv.getAmount(), 3.4403901240887094, TOL); // 2.x
  }

  public void regression_pvSensi() {
    PointSensitivityBuilder point = PRICER.presentValueSensitivity(CAPLET_REG, RATES, VOLS);
    CurveCurrencyParameterSensitivities sensi = RATES.curveParameterSensitivity(point.build());
    double[] sensiDsc = new double[] {0.0, 0.0, 0.0, -7.148360371957523, -1.8968344850148018, 0.0}; // 2.x
    double[] sensiFwd = new double[] {0.0, 0.0, 0.0, -3999.714444844649, 5987.977558683395, 0.0, 0.0, 0.0}; // 2.x
    assertTrue(DoubleArrayMath.fuzzyEquals(
        sensi.getSensitivity(IborCapletFloorletDataSet.DSC_NAME, EUR).getSensitivity().toArray(),
        sensiDsc, NOTIONAL * TOL));
    assertTrue(DoubleArrayMath.fuzzyEquals(
        sensi.getSensitivity(IborCapletFloorletDataSet.FWD3_NAME, EUR).getSensitivity().toArray(),
        sensiFwd, NOTIONAL * TOL));
  }

}
