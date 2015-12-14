/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.pricer.bond;

import static com.opengamma.strata.basics.currency.Currency.EUR;
import static com.opengamma.strata.basics.date.DayCounts.ACT_365F;
import static com.opengamma.strata.collect.TestHelper.date;
import static com.opengamma.strata.market.value.CompoundedRateType.CONTINUOUS;
import static com.opengamma.strata.market.value.CompoundedRateType.PERIODIC;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.time.LocalDate;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.CurrencyAmount;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.basics.currency.Payment;
import com.opengamma.strata.basics.date.BusinessDayAdjustment;
import com.opengamma.strata.basics.date.BusinessDayConventions;
import com.opengamma.strata.basics.date.DayCount;
import com.opengamma.strata.basics.date.DayCounts;
import com.opengamma.strata.basics.date.DaysAdjustment;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.date.HolidayCalendars;
import com.opengamma.strata.basics.schedule.Frequency;
import com.opengamma.strata.basics.schedule.PeriodicSchedule;
import com.opengamma.strata.basics.schedule.StubConvention;
import com.opengamma.strata.collect.array.DoubleArray;
import com.opengamma.strata.collect.id.StandardId;
import com.opengamma.strata.collect.tuple.Pair;
import com.opengamma.strata.market.curve.CurveCurrencyParameterSensitivities;
import com.opengamma.strata.market.curve.CurveMetadata;
import com.opengamma.strata.market.curve.CurveName;
import com.opengamma.strata.market.curve.Curves;
import com.opengamma.strata.market.curve.InterpolatedNodalCurve;
import com.opengamma.strata.market.interpolator.CurveInterpolator;
import com.opengamma.strata.market.interpolator.CurveInterpolators;
import com.opengamma.strata.market.sensitivity.PointSensitivities;
import com.opengamma.strata.market.sensitivity.PointSensitivityBuilder;
import com.opengamma.strata.market.value.BondGroup;
import com.opengamma.strata.market.value.DiscountFactors;
import com.opengamma.strata.market.value.LegalEntityGroup;
import com.opengamma.strata.market.value.ZeroRateDiscountFactors;
import com.opengamma.strata.pricer.DiscountingPaymentPricer;
import com.opengamma.strata.pricer.impl.bond.DiscountingFixedCouponBondPaymentPeriodPricer;
import com.opengamma.strata.pricer.rate.LegalEntityDiscountingProvider;
import com.opengamma.strata.pricer.sensitivity.RatesFiniteDifferenceSensitivityCalculator;
import com.opengamma.strata.product.Security;
import com.opengamma.strata.product.SecurityLink;
import com.opengamma.strata.product.TradeInfo;
import com.opengamma.strata.product.UnitSecurity;
import com.opengamma.strata.product.bond.ExpandedFixedCouponBond;
import com.opengamma.strata.product.bond.FixedCouponBond;
import com.opengamma.strata.product.bond.FixedCouponBondPaymentPeriod;
import com.opengamma.strata.product.bond.FixedCouponBondTrade;
import com.opengamma.strata.product.bond.YieldConvention;

/**
 * Test {@link DiscountingFixedCouponBondTradePricer}.
 */
@Test
public class DiscountingFixedCouponBondTradePricerTest {
  // fixed coupon bond
  private static final StandardId SECURITY_ID = StandardId.of("OG-Ticker", "GOVT1-BOND1");
  private static final StandardId ISSUER_ID = StandardId.of("OG-Ticker", "GOVT1");
  private static final LocalDate SETTLEMENT = date(2016, 4, 29); // after coupon date
  private static final LocalDate VAL_DATE = date(2016, 4, 25);
  private static final LocalDate TRADE_BEFORE = date(2016, 3, 18);
  private static final LocalDate SETTLE_BEFORE = date(2016, 3, 22); // before coupon date
  private static final LocalDate TRADE_ON_COUPON = date(2016, 4, 8);
  private static final LocalDate SETTLE_ON_COUPON = date(2016, 4, 12); // coupon date
  private static final LocalDate TRADE_BTWN_DETACHMENT_COUPON = date(2016, 4, 5);
  private static final LocalDate SETTLE_BTWN_DETACHMENT_COUPON = date(2016, 4, 8); // between detachment date and coupon date
  private static final LocalDate TRADE_ON_DETACHMENT = date(2016, 4, 4);
  private static final LocalDate SETTLE_ON_DETACHMENT = date(2016, 4, 7); // detachment date
  private static final TradeInfo TRADE_INFO = TradeInfo.builder()
      .tradeDate(VAL_DATE)
      .settlementDate(SETTLEMENT)
      .build();
  private static final TradeInfo TRADE_INFO_BEFORE = TradeInfo.builder()
      .tradeDate(TRADE_BEFORE)
      .settlementDate(SETTLE_BEFORE)
      .build();
  private static final TradeInfo TRADE_INFO_ON_COUPON = TradeInfo.builder()
      .tradeDate(TRADE_ON_COUPON)
      .settlementDate(SETTLE_ON_COUPON)
      .build();
  private static final TradeInfo TRADE_INFO_BTWN_DETACHMENT_COUPON = TradeInfo.builder()
      .tradeDate(TRADE_BTWN_DETACHMENT_COUPON)
      .settlementDate(SETTLE_BTWN_DETACHMENT_COUPON)
      .build();
  private static final TradeInfo TRADE_INFO_ON_DETACHMENT = TradeInfo.builder()
      .tradeDate(TRADE_ON_DETACHMENT)
      .settlementDate(SETTLE_ON_DETACHMENT)
      .build();
  private static final long QUANTITY = 15L;
  private static final YieldConvention YIELD_CONVENTION = YieldConvention.GERMAN_BONDS;
  private static final double NOTIONAL = 1.0e7;
  private static final double FIXED_RATE = 0.015;
  private static final HolidayCalendar EUR_CALENDAR = HolidayCalendars.EUTA;
  private static final DaysAdjustment DATE_OFFSET = DaysAdjustment.ofBusinessDays(3, EUR_CALENDAR);
  private static final DayCount DAY_COUNT = DayCounts.ACT_365F;
  private static final LocalDate START_DATE = LocalDate.of(2015, 4, 12);
  private static final LocalDate END_DATE = LocalDate.of(2025, 4, 12);
  private static final BusinessDayAdjustment BUSINESS_ADJUST =
      BusinessDayAdjustment.of(BusinessDayConventions.MODIFIED_FOLLOWING, EUR_CALENDAR);
  private static final PeriodicSchedule PERIOD_SCHEDULE = PeriodicSchedule.of(
      START_DATE, END_DATE, Frequency.P6M, BUSINESS_ADJUST, StubConvention.SHORT_INITIAL, false);
  private static final DaysAdjustment EX_COUPON = DaysAdjustment.ofCalendarDays(-5, BUSINESS_ADJUST);
  private static final FixedCouponBond PRODUCT = FixedCouponBond.builder()
      .dayCount(DAY_COUNT)
      .fixedRate(FIXED_RATE)
      .legalEntityId(ISSUER_ID)
      .currency(EUR)
      .notional(NOTIONAL)
      .periodicSchedule(PERIOD_SCHEDULE)
      .settlementDateOffset(DATE_OFFSET)
      .yieldConvention(YIELD_CONVENTION)
      .exCouponPeriod(EX_COUPON)
      .build();
  private static final Security<FixedCouponBond> BOND_SECURITY =
      UnitSecurity.builder(PRODUCT).standardId(SECURITY_ID).build();
  private static final SecurityLink<FixedCouponBond> SECURITY_LINK = SecurityLink.resolved(BOND_SECURITY);
  private static final Payment UPFRONT_PAYMENT = Payment.of(CurrencyAmount.of(EUR, -QUANTITY * NOTIONAL * 0.99), SETTLEMENT);
  private static final Payment UPFRONT_PAYMENT_ZERO = Payment.of(CurrencyAmount.of(EUR, 0d), SETTLE_BEFORE);
  /** nonzero ex-coupon period */
  private static final FixedCouponBondTrade TRADE = FixedCouponBondTrade.builder()
      .securityLink(SECURITY_LINK)
      .tradeInfo(TRADE_INFO)
      .quantity(QUANTITY)
      .payment(UPFRONT_PAYMENT)
      .build();
  private static final FixedCouponBond PRODUCT_NO_EXCOUPON = FixedCouponBond.builder()
      .dayCount(DAY_COUNT)
      .fixedRate(FIXED_RATE)
      .legalEntityId(ISSUER_ID)
      .currency(EUR)
      .notional(NOTIONAL)
      .periodicSchedule(PERIOD_SCHEDULE)
      .settlementDateOffset(DATE_OFFSET)
      .yieldConvention(YIELD_CONVENTION)
      .build();
  private static final Security<FixedCouponBond> BOND_SECURITY_NO_EXCOUPON =
      UnitSecurity.builder(PRODUCT_NO_EXCOUPON).standardId(SECURITY_ID).build();
  private static final SecurityLink<FixedCouponBond> SECURITY_LINK_NO_EXCOUPON =
      SecurityLink.resolved(BOND_SECURITY_NO_EXCOUPON);
  /** no ex-coupon period */
  private static final FixedCouponBondTrade TRADE_NO_EXCOUPON = FixedCouponBondTrade.builder()
      .securityLink(SECURITY_LINK_NO_EXCOUPON)
      .tradeInfo(TRADE_INFO)
      .quantity(QUANTITY)
      .payment(UPFRONT_PAYMENT)
      .build();

  // rates provider
  private static final CurveInterpolator INTERPOLATOR = CurveInterpolators.LINEAR;
  private static final CurveName NAME_REPO = CurveName.of("TestRepoCurve");
  private static final CurveMetadata METADATA_REPO = Curves.zeroRates(NAME_REPO, ACT_365F);
  private static final InterpolatedNodalCurve CURVE_REPO = InterpolatedNodalCurve.of(
      METADATA_REPO, DoubleArray.of(0.1, 2.0, 10.0), DoubleArray.of(0.05, 0.06, 0.09), INTERPOLATOR);
  private static final BondGroup GROUP_REPO = BondGroup.of("GOVT1 BOND1");
  private static final CurveName NAME_ISSUER = CurveName.of("TestIssuerCurve");
  private static final CurveMetadata METADATA_ISSUER = Curves.zeroRates(NAME_ISSUER, ACT_365F);
  private static final InterpolatedNodalCurve CURVE_ISSUER = InterpolatedNodalCurve.of(
      METADATA_ISSUER, DoubleArray.of(0.2, 9.0, 15.0), DoubleArray.of(0.03, 0.05, 0.13), INTERPOLATOR);
  private static final LegalEntityGroup GROUP_ISSUER = LegalEntityGroup.of("GOVT1");
  private static final LegalEntityDiscountingProvider PROVIDER = createRatesProvider(VAL_DATE);
  private static final LegalEntityDiscountingProvider PROVIDER_BEFORE = createRatesProvider(TRADE_BEFORE);

  private static final double Z_SPREAD = 0.035;
  private static final int PERIOD_PER_YEAR = 4;
  private static final double TOL = 1.0e-12;
  private static final double EPS = 1.0e-6;

  // pricers
  private static final DiscountingFixedCouponBondTradePricer TRADE_PRICER = DiscountingFixedCouponBondTradePricer.DEFAULT;
  private static final DiscountingFixedCouponBondProductPricer PRODUCT_PRICER =
      DiscountingFixedCouponBondProductPricer.DEFAULT;
  private static final DiscountingPaymentPricer PRICER_NOMINAL = DiscountingPaymentPricer.DEFAULT;
  private static final DiscountingFixedCouponBondPaymentPeriodPricer COUPON_PRICER =
      DiscountingFixedCouponBondPaymentPeriodPricer.DEFAULT;
  private static final RatesFiniteDifferenceSensitivityCalculator FD_CAL = new RatesFiniteDifferenceSensitivityCalculator(EPS);

  public void test_presentValue() {
    CurrencyAmount computedTrade = TRADE_PRICER.presentValue(TRADE, PROVIDER);
    CurrencyAmount computedProduct = PRODUCT_PRICER.presentValue(PRODUCT, PROVIDER, SETTLEMENT);
    CurrencyAmount pvPayment =
        PRICER_NOMINAL.presentValue(UPFRONT_PAYMENT, ZeroRateDiscountFactors.of(EUR, VAL_DATE, CURVE_REPO));
    assertEquals(computedTrade.getAmount(),
        computedProduct.multipliedBy(QUANTITY).plus(pvPayment).getAmount(), NOTIONAL * QUANTITY * TOL);
  }

  public void test_presentValueWithZSpread_continuous() {
    CurrencyAmount computedTrade = TRADE_PRICER.presentValueWithZSpread(
        TRADE, PROVIDER, Z_SPREAD, CONTINUOUS, 0);
    CurrencyAmount computedProduct =
        PRODUCT_PRICER.presentValueWithZSpread(PRODUCT, PROVIDER, Z_SPREAD, CONTINUOUS, 0, SETTLEMENT);
    CurrencyAmount pvPayment =
        PRICER_NOMINAL.presentValue(UPFRONT_PAYMENT, ZeroRateDiscountFactors.of(EUR, VAL_DATE, CURVE_REPO));
    assertEquals(computedTrade.getAmount(),
        computedProduct.multipliedBy(QUANTITY).plus(pvPayment).getAmount(), NOTIONAL * QUANTITY * TOL);
  }

  public void test_presentValueWithZSpread_periodic() {
    CurrencyAmount computedTrade =
        TRADE_PRICER.presentValueWithZSpread(TRADE, PROVIDER, Z_SPREAD, PERIODIC, PERIOD_PER_YEAR);
    CurrencyAmount computedProduct = PRODUCT_PRICER.presentValueWithZSpread(
        PRODUCT, PROVIDER, Z_SPREAD, PERIODIC, PERIOD_PER_YEAR, SETTLEMENT);
    CurrencyAmount pvPayment =
        PRICER_NOMINAL.presentValue(UPFRONT_PAYMENT, ZeroRateDiscountFactors.of(EUR, VAL_DATE, CURVE_REPO));
    assertEquals(computedTrade.getAmount(),
        computedProduct.multipliedBy(QUANTITY).plus(pvPayment).getAmount(), NOTIONAL * QUANTITY * TOL);
  }

  public void test_presentValue_noExcoupon() {
    CurrencyAmount computedTrade = TRADE_PRICER.presentValue(TRADE_NO_EXCOUPON, PROVIDER);
    CurrencyAmount computedProduct = PRODUCT_PRICER.presentValue(PRODUCT_NO_EXCOUPON, PROVIDER, SETTLEMENT);
    CurrencyAmount pvPayment =
        PRICER_NOMINAL.presentValue(UPFRONT_PAYMENT, ZeroRateDiscountFactors.of(EUR, VAL_DATE, CURVE_REPO));
    assertEquals(computedTrade.getAmount(),
        computedProduct.multipliedBy(QUANTITY).plus(pvPayment).getAmount(), NOTIONAL * QUANTITY * TOL);
  }

  public void test_presentValueWithZSpread_continuous_noExcoupon() {
    CurrencyAmount computedTrade =
        TRADE_PRICER.presentValueWithZSpread(TRADE_NO_EXCOUPON, PROVIDER, Z_SPREAD, CONTINUOUS, 0);
    CurrencyAmount computedProduct = PRODUCT_PRICER.presentValueWithZSpread(
        PRODUCT_NO_EXCOUPON, PROVIDER, Z_SPREAD, CONTINUOUS, 0, SETTLEMENT);
    CurrencyAmount pvPayment =
        PRICER_NOMINAL.presentValue(UPFRONT_PAYMENT, ZeroRateDiscountFactors.of(EUR, VAL_DATE, CURVE_REPO));
    assertEquals(computedTrade.getAmount(),
        computedProduct.multipliedBy(QUANTITY).plus(pvPayment).getAmount(), NOTIONAL * QUANTITY * TOL);
  }

  public void test_presentValueWithZSpread_periodic_noExcoupon() {
    CurrencyAmount computedTrade = TRADE_PRICER.presentValueWithZSpread(
        TRADE_NO_EXCOUPON, PROVIDER, Z_SPREAD, PERIODIC, PERIOD_PER_YEAR);
    CurrencyAmount computedProduct = PRODUCT_PRICER.presentValueWithZSpread(
        PRODUCT_NO_EXCOUPON, PROVIDER, Z_SPREAD, PERIODIC, PERIOD_PER_YEAR, SETTLEMENT);
    CurrencyAmount pvPayment =
        PRICER_NOMINAL.presentValue(UPFRONT_PAYMENT, ZeroRateDiscountFactors.of(EUR, VAL_DATE, CURVE_REPO));
    assertEquals(computedTrade.getAmount(),
        computedProduct.multipliedBy(QUANTITY).plus(pvPayment).getAmount(), NOTIONAL * QUANTITY * TOL);
  }

  //-------------------------------------------------------------------------
  public void test_presentValue_dateLogic() {
    FixedCouponBondTrade tradeAfter = FixedCouponBondTrade.builder()
        .securityLink(SECURITY_LINK)
        .tradeInfo(TRADE_INFO)
        .quantity(QUANTITY)
        .payment(UPFRONT_PAYMENT_ZERO)
        .build();
    CurrencyAmount computedTradeAfter = TRADE_PRICER.presentValue(tradeAfter, PROVIDER_BEFORE);
    // settle before detachment date
    FixedCouponBondTrade tradeBefore = FixedCouponBondTrade.builder()
        .securityLink(SECURITY_LINK)
        .tradeInfo(TRADE_INFO_BEFORE)
        .quantity(QUANTITY)
        .payment(UPFRONT_PAYMENT_ZERO)
        .build();
    CurrencyAmount computedTradeBefore = TRADE_PRICER.presentValue(tradeBefore, PROVIDER_BEFORE);
    FixedCouponBondPaymentPeriod periodExtra = findPeriod(PRODUCT.expand(), SETTLE_BEFORE, SETTLEMENT);
    double pvExtra = COUPON_PRICER.presentValue(periodExtra, PROVIDER_BEFORE.issuerCurveDiscountFactors(ISSUER_ID, EUR));
    assertEquals(computedTradeBefore.getAmount(), computedTradeAfter.plus(pvExtra * QUANTITY).getAmount(),
        NOTIONAL * QUANTITY * TOL);
    // settle on detachment date
    FixedCouponBondTrade tradeOnDetachment = FixedCouponBondTrade.builder()
        .securityLink(SECURITY_LINK)
        .tradeInfo(TRADE_INFO_ON_DETACHMENT)
        .quantity(QUANTITY)
        .payment(UPFRONT_PAYMENT_ZERO)
        .build();
    CurrencyAmount computedTradeOnDetachment = TRADE_PRICER.presentValue(tradeOnDetachment, PROVIDER_BEFORE);
    assertEquals(computedTradeOnDetachment.getAmount(), computedTradeAfter.getAmount(), NOTIONAL * QUANTITY * TOL);
    // settle between detachment date and coupon date
    FixedCouponBondTrade tradeBtwnDetachmentCoupon = FixedCouponBondTrade.builder()
        .securityLink(SECURITY_LINK)
        .tradeInfo(TRADE_INFO_BTWN_DETACHMENT_COUPON)
        .quantity(QUANTITY)
        .payment(UPFRONT_PAYMENT_ZERO)
        .build();
    CurrencyAmount computedTradeBtwnDetachmentCoupon =
        TRADE_PRICER.presentValue(tradeBtwnDetachmentCoupon, PROVIDER_BEFORE);
    assertEquals(computedTradeBtwnDetachmentCoupon.getAmount(), computedTradeAfter.getAmount(), NOTIONAL * QUANTITY * TOL);
  }

  public void test_presentValue_dateLogic_pastSettle() {
    FixedCouponBondTrade tradeAfter = FixedCouponBondTrade.builder()
        .securityLink(SECURITY_LINK)
        .tradeInfo(TRADE_INFO)
        .quantity(QUANTITY)
        .payment(UPFRONT_PAYMENT_ZERO)
        .build();
    CurrencyAmount computedTradeAfter = TRADE_PRICER.presentValue(tradeAfter, PROVIDER);
    // settle before detachment date
    FixedCouponBondTrade tradeBefore = FixedCouponBondTrade.builder()
        .securityLink(SECURITY_LINK)
        .tradeInfo(TRADE_INFO_BEFORE)
        .quantity(QUANTITY)
        .payment(UPFRONT_PAYMENT_ZERO)
        .build();
    CurrencyAmount computedTradeBefore = TRADE_PRICER.presentValue(tradeBefore, PROVIDER);
    assertEquals(computedTradeBefore.getAmount(), computedTradeAfter.getAmount(), NOTIONAL * QUANTITY * TOL);
    // settle on detachment date
    FixedCouponBondTrade tradeOnDetachment = FixedCouponBondTrade.builder()
        .securityLink(SECURITY_LINK)
        .tradeInfo(TRADE_INFO_ON_DETACHMENT)
        .quantity(QUANTITY)
        .payment(UPFRONT_PAYMENT_ZERO)
        .build();
    CurrencyAmount computedTradeOnDetachment = TRADE_PRICER.presentValue(tradeOnDetachment, PROVIDER);
    assertEquals(computedTradeOnDetachment.getAmount(), computedTradeAfter.getAmount(), NOTIONAL * QUANTITY * TOL);
    // settle between detachment date and coupon date
    FixedCouponBondTrade tradeBtwnDetachmentCoupon = FixedCouponBondTrade.builder()
        .securityLink(SECURITY_LINK)
        .tradeInfo(TRADE_INFO_BTWN_DETACHMENT_COUPON)
        .quantity(QUANTITY)
        .payment(UPFRONT_PAYMENT_ZERO)
        .build();
    CurrencyAmount computedTradeBtwnDetachmentCoupon =
        TRADE_PRICER.presentValue(tradeBtwnDetachmentCoupon, PROVIDER);
    assertEquals(computedTradeBtwnDetachmentCoupon.getAmount(), computedTradeAfter.getAmount(), NOTIONAL * QUANTITY * TOL);
  }

  public void test_presentValue_dateLogic_noExcoupon() {
    FixedCouponBondTrade tradeAfter = FixedCouponBondTrade.builder()
        .securityLink(SECURITY_LINK_NO_EXCOUPON)
        .tradeInfo(TRADE_INFO)
        .quantity(QUANTITY)
        .payment(UPFRONT_PAYMENT_ZERO)
        .build();
    CurrencyAmount computedTradeAfter = TRADE_PRICER.presentValue(tradeAfter, PROVIDER_BEFORE);
    // settle before coupon date
    FixedCouponBondTrade tradeBefore = FixedCouponBondTrade.builder()
        .securityLink(SECURITY_LINK_NO_EXCOUPON)
        .tradeInfo(TRADE_INFO_BEFORE)
        .quantity(QUANTITY)
        .payment(UPFRONT_PAYMENT_ZERO)
        .build();
    CurrencyAmount computedTradeBefore = TRADE_PRICER.presentValue(tradeBefore, PROVIDER_BEFORE);
    FixedCouponBondPaymentPeriod periodExtra = findPeriod(PRODUCT_NO_EXCOUPON.expand(), SETTLE_BEFORE, SETTLEMENT);
    double pvExtra = COUPON_PRICER.presentValue(periodExtra, PROVIDER_BEFORE.issuerCurveDiscountFactors(ISSUER_ID, EUR));
    assertEquals(computedTradeBefore.getAmount(), computedTradeAfter.plus(pvExtra * QUANTITY).getAmount(),
        NOTIONAL * QUANTITY * TOL);
    // settle on coupon date
    FixedCouponBondTrade tradeOnCoupon = FixedCouponBondTrade.builder()
        .securityLink(SECURITY_LINK_NO_EXCOUPON)
        .tradeInfo(TRADE_INFO_ON_COUPON)
        .quantity(QUANTITY)
        .payment(UPFRONT_PAYMENT_ZERO)
        .build();
    CurrencyAmount computedTradeOnCoupon = TRADE_PRICER.presentValue(tradeOnCoupon, PROVIDER_BEFORE);
    assertEquals(computedTradeOnCoupon.getAmount(), computedTradeAfter.getAmount(), NOTIONAL * QUANTITY * TOL);
  }

  public void test_presentValue_dateLogic_pastSettle_noExcoupon() {
    FixedCouponBondTrade tradeAfter = FixedCouponBondTrade.builder()
        .securityLink(SECURITY_LINK_NO_EXCOUPON)
        .tradeInfo(TRADE_INFO)
        .quantity(QUANTITY)
        .payment(UPFRONT_PAYMENT_ZERO)
        .build();
    CurrencyAmount computedTradeAfter = TRADE_PRICER.presentValue(tradeAfter, PROVIDER);
    // settle before coupon date
    FixedCouponBondTrade tradeBefore = FixedCouponBondTrade.builder()
        .securityLink(SECURITY_LINK_NO_EXCOUPON)
        .tradeInfo(TRADE_INFO_BEFORE)
        .quantity(QUANTITY)
        .payment(UPFRONT_PAYMENT_ZERO)
        .build();
    CurrencyAmount computedTradeBefore = TRADE_PRICER.presentValue(tradeBefore, PROVIDER);
    assertEquals(computedTradeBefore.getAmount(), computedTradeAfter.getAmount(),
        NOTIONAL * QUANTITY * TOL);
    // settle on coupon date
    FixedCouponBondTrade tradeOnCoupon = FixedCouponBondTrade.builder()
        .securityLink(SECURITY_LINK_NO_EXCOUPON)
        .tradeInfo(TRADE_INFO_ON_COUPON)
        .quantity(QUANTITY)
        .payment(UPFRONT_PAYMENT_ZERO)
        .build();
    CurrencyAmount computedTradeOnCoupon = TRADE_PRICER.presentValue(tradeOnCoupon, PROVIDER);
    assertEquals(computedTradeOnCoupon.getAmount(), computedTradeAfter.getAmount(), NOTIONAL * QUANTITY * TOL);
  }

  //-------------------------------------------------------------------------
  public void test_presentValueFromCleanPrice() {
    double cleanPrice = 0.985;
    CurrencyAmount computed = TRADE_PRICER.presentValueFromCleanPrice(TRADE, PROVIDER, cleanPrice);
    LocalDate standardSettlement = PRODUCT.getSettlementDateOffset().adjust(VAL_DATE);
    double df = ZeroRateDiscountFactors.of(EUR, VAL_DATE, CURVE_REPO).discountFactor(standardSettlement);
    double accruedInterest = PRODUCT_PRICER.accruedInterest(PRODUCT, standardSettlement);
    double pvPayment = PRICER_NOMINAL
        .presentValue(UPFRONT_PAYMENT, ZeroRateDiscountFactors.of(EUR, VAL_DATE, CURVE_REPO)).getAmount();
    double expected = QUANTITY * (cleanPrice * df * NOTIONAL + accruedInterest * df) + pvPayment;
    assertEquals(computed.getCurrency(), EUR);
    assertEquals(computed.getAmount(), expected, NOTIONAL * QUANTITY * TOL);
  }

  public void test_presentValueFromCleanPriceWithZSpread_continuous() {
    double cleanPrice = 0.985;
    CurrencyAmount computed = TRADE_PRICER.presentValueFromCleanPriceWithZSpread(
        TRADE, PROVIDER, cleanPrice, Z_SPREAD, CONTINUOUS, 0);
    LocalDate standardSettlement = PRODUCT.getSettlementDateOffset().adjust(VAL_DATE);
    double df = ZeroRateDiscountFactors.of(EUR, VAL_DATE, CURVE_REPO).discountFactor(standardSettlement);
    double accruedInterest = PRODUCT_PRICER.accruedInterest(PRODUCT, standardSettlement);
    double pvPayment = PRICER_NOMINAL
        .presentValue(UPFRONT_PAYMENT, ZeroRateDiscountFactors.of(EUR, VAL_DATE, CURVE_REPO)).getAmount();
    double expected = QUANTITY * (cleanPrice * df * NOTIONAL + accruedInterest * df) + pvPayment;
    assertEquals(computed.getCurrency(), EUR);
    assertEquals(computed.getAmount(), expected, NOTIONAL * QUANTITY * TOL);
  }

  public void test_presentValueFromCleanPriceWithZSpread_periodic() {
    double cleanPrice = 0.985;
    CurrencyAmount computed = TRADE_PRICER.presentValueFromCleanPriceWithZSpread(
        TRADE, PROVIDER, cleanPrice, Z_SPREAD, PERIODIC, PERIOD_PER_YEAR);
    LocalDate standardSettlement = PRODUCT.getSettlementDateOffset().adjust(VAL_DATE);
    double df = ZeroRateDiscountFactors.of(EUR, VAL_DATE, CURVE_REPO).discountFactor(standardSettlement);
    double accruedInterest = PRODUCT_PRICER.accruedInterest(PRODUCT, standardSettlement);
    double pvPayment = PRICER_NOMINAL
        .presentValue(UPFRONT_PAYMENT, ZeroRateDiscountFactors.of(EUR, VAL_DATE, CURVE_REPO)).getAmount();
    double expected = QUANTITY * (cleanPrice * df * NOTIONAL + accruedInterest * df) + pvPayment;
    assertEquals(computed.getCurrency(), EUR);
    assertEquals(computed.getAmount(), expected, NOTIONAL * QUANTITY * TOL);
  }

  public void test_presentValueFromCleanPrice_noExcoupon() {
    double cleanPrice = 0.985;
    CurrencyAmount computed = TRADE_PRICER.presentValueFromCleanPrice(TRADE_NO_EXCOUPON, PROVIDER, cleanPrice);
    LocalDate standardSettlement = PRODUCT_NO_EXCOUPON.getSettlementDateOffset().adjust(VAL_DATE);
    double df = ZeroRateDiscountFactors.of(EUR, VAL_DATE, CURVE_REPO).discountFactor(standardSettlement);
    double accruedInterest = PRODUCT_PRICER.accruedInterest(PRODUCT_NO_EXCOUPON, standardSettlement);
    double pvPayment = PRICER_NOMINAL
        .presentValue(UPFRONT_PAYMENT, ZeroRateDiscountFactors.of(EUR, VAL_DATE, CURVE_REPO)).getAmount();
    double expected = QUANTITY * (cleanPrice * df * NOTIONAL + accruedInterest * df) + pvPayment;
    assertEquals(computed.getCurrency(), EUR);
    assertEquals(computed.getAmount(), expected, NOTIONAL * QUANTITY * TOL);
  }

  public void test_presentValueFromCleanPriceWithZSpread_continuous_noExcoupon() {
    double cleanPrice = 0.985;
    CurrencyAmount computed = TRADE_PRICER.presentValueFromCleanPriceWithZSpread(
        TRADE_NO_EXCOUPON, PROVIDER, cleanPrice, Z_SPREAD, CONTINUOUS, 0);
    LocalDate standardSettlement = PRODUCT_NO_EXCOUPON.getSettlementDateOffset().adjust(VAL_DATE);
    double df = ZeroRateDiscountFactors.of(EUR, VAL_DATE, CURVE_REPO).discountFactor(standardSettlement);
    double accruedInterest = PRODUCT_PRICER.accruedInterest(PRODUCT_NO_EXCOUPON, standardSettlement);
    double pvPayment = PRICER_NOMINAL
        .presentValue(UPFRONT_PAYMENT, ZeroRateDiscountFactors.of(EUR, VAL_DATE, CURVE_REPO)).getAmount();
    double expected = QUANTITY * (cleanPrice * df * NOTIONAL + accruedInterest * df) + pvPayment;
    assertEquals(computed.getCurrency(), EUR);
    assertEquals(computed.getAmount(), expected, NOTIONAL * QUANTITY * TOL);
  }

  public void test_presentValueFromCleanPriceWithZSpread_periodic_noExcoupon() {
    double cleanPrice = 0.985;
    CurrencyAmount computed = TRADE_PRICER.presentValueFromCleanPriceWithZSpread(
        TRADE_NO_EXCOUPON, PROVIDER, cleanPrice, Z_SPREAD, PERIODIC, PERIOD_PER_YEAR);
    LocalDate standardSettlement = PRODUCT_NO_EXCOUPON.getSettlementDateOffset().adjust(VAL_DATE);
    double df = ZeroRateDiscountFactors.of(EUR, VAL_DATE, CURVE_REPO).discountFactor(standardSettlement);
    double accruedInterest = PRODUCT_PRICER.accruedInterest(PRODUCT_NO_EXCOUPON, standardSettlement);
    double pvPayment = PRICER_NOMINAL
        .presentValue(UPFRONT_PAYMENT, ZeroRateDiscountFactors.of(EUR, VAL_DATE, CURVE_REPO)).getAmount();
    double expected = QUANTITY * (cleanPrice * df * NOTIONAL + accruedInterest * df) + pvPayment;
    assertEquals(computed.getCurrency(), EUR);
    assertEquals(computed.getAmount(), expected, NOTIONAL * QUANTITY * TOL);
  }

  //-------------------------------------------------------------------------
  public void test_presentValueFromCleanPrice_dateLogic() {
    double cleanPrice = 0.985;
    FixedCouponBondPaymentPeriod periodExtra = findPeriod(PRODUCT.expand(), SETTLE_BEFORE, SETTLEMENT);
    // trade settlement < detachment date < standard settlement
    LocalDate valuation1 = SETTLE_ON_DETACHMENT.minusDays(1);
    TradeInfo tradeInfo1 = TradeInfo.builder()
        .tradeDate(valuation1)
        .settlementDate(valuation1)
        .build();
    FixedCouponBondTrade trade1 = FixedCouponBondTrade.builder()
        .securityLink(SECURITY_LINK)
        .tradeInfo(tradeInfo1)
        .quantity(QUANTITY)
        .payment(UPFRONT_PAYMENT_ZERO)
        .build();
    LegalEntityDiscountingProvider provider1 = createRatesProvider(valuation1);
    LocalDate standardSettlement1 = PRODUCT.getSettlementDateOffset().adjust(valuation1);
    double df1 = ZeroRateDiscountFactors.of(EUR, valuation1, CURVE_REPO).discountFactor(standardSettlement1);
    double accruedInterest1 = PRODUCT_PRICER.accruedInterest(PRODUCT, standardSettlement1);
    double basePv1 = cleanPrice * df1 * NOTIONAL + accruedInterest1 * df1;
    double pvExtra1 = COUPON_PRICER.presentValue(periodExtra, provider1.issuerCurveDiscountFactors(ISSUER_ID, EUR));
    double pvExtra1Continuous = COUPON_PRICER.presentValueWithSpread(
        periodExtra, provider1.issuerCurveDiscountFactors(ISSUER_ID, EUR), Z_SPREAD, CONTINUOUS, 0);
    double pvExtra1Periodic = COUPON_PRICER.presentValueWithSpread(
        periodExtra, provider1.issuerCurveDiscountFactors(ISSUER_ID, EUR), Z_SPREAD, PERIODIC, PERIOD_PER_YEAR);
    CurrencyAmount computed1 = TRADE_PRICER.presentValueFromCleanPrice(trade1, provider1, cleanPrice);
    CurrencyAmount computed1Continuous = TRADE_PRICER.presentValueFromCleanPriceWithZSpread(
        trade1, provider1, cleanPrice, Z_SPREAD, CONTINUOUS, 0);
    CurrencyAmount computed1Periodic = TRADE_PRICER.presentValueFromCleanPriceWithZSpread(
        trade1, provider1, cleanPrice, Z_SPREAD, PERIODIC, PERIOD_PER_YEAR);
    assertEquals(computed1.getAmount(), QUANTITY * (basePv1 + pvExtra1), NOTIONAL * QUANTITY * TOL);
    assertEquals(computed1Continuous.getAmount(), QUANTITY * (basePv1 + pvExtra1Continuous), NOTIONAL * QUANTITY * TOL);
    assertEquals(computed1Periodic.getAmount(), QUANTITY * (basePv1 + pvExtra1Periodic), NOTIONAL * QUANTITY * TOL);
    // detachment date < trade settlement < standard settlement
    TradeInfo tradeInfo2 = TradeInfo.builder()
        .tradeDate(valuation1)
        .settlementDate(SETTLE_ON_DETACHMENT.plusDays(2))
        .build();
    FixedCouponBondTrade trade2 = FixedCouponBondTrade.builder()
        .securityLink(SECURITY_LINK)
        .tradeInfo(tradeInfo2)
        .quantity(QUANTITY)
        .payment(UPFRONT_PAYMENT_ZERO)
        .build();
    CurrencyAmount computed2 = TRADE_PRICER.presentValueFromCleanPrice(trade2, provider1, cleanPrice);
    CurrencyAmount computed2Continuous = TRADE_PRICER.presentValueFromCleanPriceWithZSpread(
        trade2, provider1, cleanPrice, Z_SPREAD, CONTINUOUS, 0);
    CurrencyAmount computed2Periodic = TRADE_PRICER.presentValueFromCleanPriceWithZSpread(
        trade2, provider1, cleanPrice, Z_SPREAD, PERIODIC, PERIOD_PER_YEAR);
    assertEquals(computed2.getAmount(), QUANTITY * basePv1, NOTIONAL * QUANTITY * TOL);
    assertEquals(computed2Continuous.getAmount(), QUANTITY * basePv1, NOTIONAL * QUANTITY * TOL);
    assertEquals(computed2Periodic.getAmount(), QUANTITY * basePv1, NOTIONAL * QUANTITY * TOL);
    // detachment date < standard settlement < trade settlement
    TradeInfo tradeInfo3 = TradeInfo.builder()
        .tradeDate(valuation1)
        .settlementDate(SETTLE_ON_DETACHMENT.plusDays(7))
        .build();
    FixedCouponBondTrade trade3 = FixedCouponBondTrade.builder()
        .securityLink(SECURITY_LINK)
        .tradeInfo(tradeInfo3)
        .quantity(QUANTITY)
        .payment(UPFRONT_PAYMENT_ZERO)
        .build();
    CurrencyAmount computed3 = TRADE_PRICER.presentValueFromCleanPrice(trade3, provider1, cleanPrice);
    CurrencyAmount computed3Continuous = TRADE_PRICER.presentValueFromCleanPriceWithZSpread(
        trade3, provider1, cleanPrice, Z_SPREAD, CONTINUOUS, 0);
    CurrencyAmount computed3Periodic = TRADE_PRICER.presentValueFromCleanPriceWithZSpread(
        trade3, provider1, cleanPrice, Z_SPREAD, PERIODIC, PERIOD_PER_YEAR);
    assertEquals(computed3.getAmount(), QUANTITY * basePv1, NOTIONAL * QUANTITY * TOL);
    assertEquals(computed3Continuous.getAmount(), QUANTITY * basePv1, NOTIONAL * QUANTITY * TOL);
    assertEquals(computed3Periodic.getAmount(), QUANTITY * basePv1, NOTIONAL * QUANTITY * TOL);

    // standard settlement < detachment date < trade settlement
    LocalDate settlement4 = SETTLE_ON_DETACHMENT.plusDays(1);
    TradeInfo tradeInfo4 = TradeInfo.builder()
        .tradeDate(TRADE_BEFORE)
        .settlementDate(settlement4)
        .build();
    FixedCouponBondTrade trade4 = FixedCouponBondTrade.builder()
        .securityLink(SECURITY_LINK)
        .tradeInfo(tradeInfo4)
        .quantity(QUANTITY)
        .payment(UPFRONT_PAYMENT_ZERO)
        .build();
    LocalDate standardSettlement4 = PRODUCT.getSettlementDateOffset().adjust(TRADE_BEFORE);
    double df4 = ZeroRateDiscountFactors.of(EUR, TRADE_BEFORE, CURVE_REPO).discountFactor(standardSettlement4);
    double accruedInterest4 = PRODUCT_PRICER.accruedInterest(PRODUCT, standardSettlement4);
    double basePv4 = cleanPrice * df4 * NOTIONAL + accruedInterest4 * df4;
    double pvExtra4 = COUPON_PRICER.presentValue(periodExtra, PROVIDER_BEFORE.issuerCurveDiscountFactors(ISSUER_ID, EUR));
    double pvExtra4Continuous = COUPON_PRICER.presentValueWithSpread(
        periodExtra, PROVIDER_BEFORE.issuerCurveDiscountFactors(ISSUER_ID, EUR), Z_SPREAD, CONTINUOUS, 0);
    double pvExtra4Periodic = COUPON_PRICER.presentValueWithSpread(periodExtra, 
        PROVIDER_BEFORE.issuerCurveDiscountFactors(ISSUER_ID, EUR), Z_SPREAD, PERIODIC, PERIOD_PER_YEAR);
    CurrencyAmount computed4 = TRADE_PRICER.presentValueFromCleanPrice(trade4, PROVIDER_BEFORE, cleanPrice);
    CurrencyAmount computed4Continuous = TRADE_PRICER.presentValueFromCleanPriceWithZSpread(
        trade4, PROVIDER_BEFORE, cleanPrice, Z_SPREAD, CONTINUOUS, 0);
    CurrencyAmount computed4Periodic = TRADE_PRICER.presentValueFromCleanPriceWithZSpread(
        trade4, PROVIDER_BEFORE, cleanPrice, Z_SPREAD, PERIODIC, PERIOD_PER_YEAR);
    assertEquals(computed4.getAmount(), QUANTITY * (basePv4 - pvExtra4), NOTIONAL * QUANTITY * TOL);
    assertEquals(computed4Continuous.getAmount(), QUANTITY * (basePv4 - pvExtra4Continuous), NOTIONAL * QUANTITY * TOL);
    assertEquals(computed4Periodic.getAmount(), QUANTITY * (basePv4 - pvExtra4Periodic), NOTIONAL * QUANTITY * TOL);
    // standard settlement < trade settlement < detachment date
    TradeInfo tradeInfo5 = TradeInfo.builder()
        .tradeDate(TRADE_BEFORE)
        .settlementDate(TRADE_BEFORE.plusDays(7))
        .build();
    FixedCouponBondTrade trade5 = FixedCouponBondTrade.builder()
        .securityLink(SECURITY_LINK)
        .tradeInfo(tradeInfo5)
        .quantity(QUANTITY)
        .payment(UPFRONT_PAYMENT_ZERO)
        .build();
    CurrencyAmount computed5 = TRADE_PRICER.presentValueFromCleanPrice(trade5, PROVIDER_BEFORE, cleanPrice);
    CurrencyAmount computed5Continuous = TRADE_PRICER.presentValueFromCleanPriceWithZSpread(
        trade5, PROVIDER_BEFORE, cleanPrice, Z_SPREAD, CONTINUOUS, 0);
    CurrencyAmount computed5Periodic = TRADE_PRICER.presentValueFromCleanPriceWithZSpread(
        trade5, PROVIDER_BEFORE, cleanPrice, Z_SPREAD, PERIODIC, PERIOD_PER_YEAR);
    assertEquals(computed5.getAmount(), QUANTITY * basePv4, NOTIONAL * QUANTITY * TOL);
    assertEquals(computed5Continuous.getAmount(), QUANTITY * basePv4, NOTIONAL * QUANTITY * TOL);
    assertEquals(computed5Periodic.getAmount(), QUANTITY * basePv4, NOTIONAL * QUANTITY * TOL);
    // trade settlement < standard settlement < detachment date
    FixedCouponBondTrade trade6 = FixedCouponBondTrade.builder()
        .securityLink(SECURITY_LINK)
        .tradeInfo(TRADE_INFO_BEFORE)
        .quantity(QUANTITY)
        .payment(UPFRONT_PAYMENT_ZERO)
        .build();
    CurrencyAmount computed6 = TRADE_PRICER.presentValueFromCleanPrice(trade6, PROVIDER_BEFORE, cleanPrice);
    CurrencyAmount computed6Continuous = TRADE_PRICER.presentValueFromCleanPriceWithZSpread(
        trade6, PROVIDER_BEFORE, cleanPrice, Z_SPREAD, CONTINUOUS, 0);
    CurrencyAmount computed6Periodic = TRADE_PRICER.presentValueFromCleanPriceWithZSpread(
        trade6, PROVIDER_BEFORE, cleanPrice, Z_SPREAD, PERIODIC, PERIOD_PER_YEAR);
    assertEquals(computed6.getAmount(), QUANTITY * basePv4, NOTIONAL * QUANTITY * TOL);
    assertEquals(computed6Continuous.getAmount(), QUANTITY * basePv4, NOTIONAL * QUANTITY * TOL);
    assertEquals(computed6Periodic.getAmount(), QUANTITY * basePv4, NOTIONAL * QUANTITY * TOL);
  }

  public void test_presentValueFromCleanPrice_dateLogic_noExcoupon() {
    double cleanPrice = 0.985;
    FixedCouponBondPaymentPeriod periodExtra = findPeriod(PRODUCT_NO_EXCOUPON.expand(), SETTLE_BEFORE, SETTLEMENT);
    // trade settlement < coupon date < standard settlement
    LocalDate valuation1 = SETTLE_ON_COUPON.minusDays(1);
    TradeInfo tradeInfo1 = TradeInfo.builder()
        .tradeDate(valuation1)
        .settlementDate(valuation1)
        .build();
    FixedCouponBondTrade trade1 = FixedCouponBondTrade.builder()
        .securityLink(SECURITY_LINK_NO_EXCOUPON)
        .tradeInfo(tradeInfo1)
        .quantity(QUANTITY)
        .payment(UPFRONT_PAYMENT_ZERO)
        .build();
    LegalEntityDiscountingProvider provider1 = createRatesProvider(valuation1);
    LocalDate standardSettlement1 = PRODUCT_NO_EXCOUPON.getSettlementDateOffset().adjust(valuation1);
    double df1 = ZeroRateDiscountFactors.of(EUR, valuation1, CURVE_REPO).discountFactor(standardSettlement1);
    double accruedInterest1 = PRODUCT_PRICER.accruedInterest(PRODUCT_NO_EXCOUPON, standardSettlement1);
    double basePv1 = cleanPrice * df1 * NOTIONAL + accruedInterest1 * df1;
    double pvExtra1 = COUPON_PRICER.presentValue(periodExtra, provider1.issuerCurveDiscountFactors(ISSUER_ID, EUR));
    double pvExtra1Continuous = COUPON_PRICER.presentValueWithSpread(
        periodExtra, provider1.issuerCurveDiscountFactors(ISSUER_ID, EUR), Z_SPREAD, CONTINUOUS, 0);
    double pvExtra1Periodic = COUPON_PRICER.presentValueWithSpread(
        periodExtra, provider1.issuerCurveDiscountFactors(ISSUER_ID, EUR), Z_SPREAD, PERIODIC, PERIOD_PER_YEAR);
    CurrencyAmount computed1 = TRADE_PRICER.presentValueFromCleanPrice(trade1, provider1, cleanPrice);
    CurrencyAmount computed1Continuous = TRADE_PRICER.presentValueFromCleanPriceWithZSpread(
        trade1, provider1, cleanPrice, Z_SPREAD, CONTINUOUS, 0);
    CurrencyAmount computed1Periodic = TRADE_PRICER.presentValueFromCleanPriceWithZSpread(
        trade1, provider1, cleanPrice, Z_SPREAD, PERIODIC, PERIOD_PER_YEAR);
    assertEquals(computed1.getAmount(), QUANTITY * (basePv1 + pvExtra1), NOTIONAL * QUANTITY * TOL);
    assertEquals(computed1Continuous.getAmount(), QUANTITY * (basePv1 + pvExtra1Continuous), NOTIONAL * QUANTITY * TOL);
    assertEquals(computed1Periodic.getAmount(), QUANTITY * (basePv1 + pvExtra1Periodic), NOTIONAL * QUANTITY * TOL);
    // coupon date < trade settlement < standard settlement
    TradeInfo tradeInfo2 = TradeInfo.builder()
        .tradeDate(valuation1)
        .settlementDate(SETTLE_ON_COUPON.plusDays(2))
        .build();
    FixedCouponBondTrade trade2 = FixedCouponBondTrade.builder()
        .securityLink(SECURITY_LINK_NO_EXCOUPON)
        .tradeInfo(tradeInfo2)
        .quantity(QUANTITY)
        .payment(UPFRONT_PAYMENT_ZERO)
        .build();
    CurrencyAmount computed2 = TRADE_PRICER.presentValueFromCleanPrice(trade2, provider1, cleanPrice);
    CurrencyAmount computed2Continuous = TRADE_PRICER.presentValueFromCleanPriceWithZSpread(
        trade2, provider1, cleanPrice, Z_SPREAD, CONTINUOUS, 0);
    CurrencyAmount computed2Periodic = TRADE_PRICER.presentValueFromCleanPriceWithZSpread(
        trade2, provider1, cleanPrice, Z_SPREAD, PERIODIC, PERIOD_PER_YEAR);
    assertEquals(computed2.getAmount(), QUANTITY * basePv1, NOTIONAL * QUANTITY * TOL);
    assertEquals(computed2Continuous.getAmount(), QUANTITY * basePv1, NOTIONAL * QUANTITY * TOL);
    assertEquals(computed2Periodic.getAmount(), QUANTITY * basePv1, NOTIONAL * QUANTITY * TOL);
    // coupon date < standard settlement < trade settlement
    TradeInfo tradeInfo3 = TradeInfo.builder()
        .tradeDate(valuation1)
        .settlementDate(SETTLE_ON_COUPON.plusDays(7))
        .build();
    FixedCouponBondTrade trade3 = FixedCouponBondTrade.builder()
        .securityLink(SECURITY_LINK_NO_EXCOUPON)
        .tradeInfo(tradeInfo3)
        .quantity(QUANTITY)
        .payment(UPFRONT_PAYMENT_ZERO)
        .build();
    CurrencyAmount computed3 = TRADE_PRICER.presentValueFromCleanPrice(trade3, provider1, cleanPrice);
    CurrencyAmount computed3Continuous = TRADE_PRICER.presentValueFromCleanPriceWithZSpread(
        trade3, provider1, cleanPrice, Z_SPREAD, CONTINUOUS, 0);
    CurrencyAmount computed3Periodic = TRADE_PRICER.presentValueFromCleanPriceWithZSpread(
        trade3, provider1, cleanPrice, Z_SPREAD, PERIODIC, PERIOD_PER_YEAR);
    assertEquals(computed3.getAmount(), QUANTITY * basePv1, NOTIONAL * QUANTITY * TOL);
    assertEquals(computed3Continuous.getAmount(), QUANTITY * basePv1, NOTIONAL * QUANTITY * TOL);
    assertEquals(computed3Periodic.getAmount(), QUANTITY * basePv1, NOTIONAL * QUANTITY * TOL);

    // standard settlement < coupon date < trade settlement
    LocalDate settlement4 = SETTLE_ON_COUPON.plusDays(1);
    TradeInfo tradeInfo4 = TradeInfo.builder()
        .tradeDate(TRADE_BEFORE)
        .settlementDate(settlement4)
        .build();
    FixedCouponBondTrade trade4 = FixedCouponBondTrade.builder()
        .securityLink(SECURITY_LINK_NO_EXCOUPON)
        .tradeInfo(tradeInfo4)
        .quantity(QUANTITY)
        .payment(UPFRONT_PAYMENT_ZERO)
        .build();
    LocalDate standardSettlement4 = PRODUCT_NO_EXCOUPON.getSettlementDateOffset().adjust(TRADE_BEFORE);
    double df4 = ZeroRateDiscountFactors.of(EUR, TRADE_BEFORE, CURVE_REPO).discountFactor(standardSettlement4);
    double accruedInterest4 = PRODUCT_PRICER.accruedInterest(PRODUCT_NO_EXCOUPON, standardSettlement4);
    double basePv4 = cleanPrice * df4 * NOTIONAL + accruedInterest4 * df4;
    double pvExtra4 = COUPON_PRICER.presentValue(periodExtra,
        PROVIDER_BEFORE.issuerCurveDiscountFactors(ISSUER_ID, EUR));
    double pvExtra4Continuous = COUPON_PRICER.presentValueWithSpread(periodExtra,
        PROVIDER_BEFORE.issuerCurveDiscountFactors(ISSUER_ID, EUR), Z_SPREAD, CONTINUOUS, 0);
    double pvExtra4Periodic = COUPON_PRICER.presentValueWithSpread(periodExtra,
        PROVIDER_BEFORE.issuerCurveDiscountFactors(ISSUER_ID, EUR), Z_SPREAD, PERIODIC, PERIOD_PER_YEAR);
    CurrencyAmount computed4 = TRADE_PRICER.presentValueFromCleanPrice(trade4, PROVIDER_BEFORE, cleanPrice);
    CurrencyAmount computed4Continuous = TRADE_PRICER.presentValueFromCleanPriceWithZSpread(
        trade4, PROVIDER_BEFORE, cleanPrice, Z_SPREAD, CONTINUOUS, 0);
    CurrencyAmount computed4Periodic = TRADE_PRICER.presentValueFromCleanPriceWithZSpread(
        trade4, PROVIDER_BEFORE, cleanPrice, Z_SPREAD, PERIODIC, PERIOD_PER_YEAR);
    assertEquals(computed4.getAmount(), QUANTITY * (basePv4 - pvExtra4), NOTIONAL * QUANTITY * TOL);
    assertEquals(computed4Continuous.getAmount(), QUANTITY * (basePv4 - pvExtra4Continuous), NOTIONAL * QUANTITY * TOL);
    assertEquals(computed4Periodic.getAmount(), QUANTITY * (basePv4 - pvExtra4Periodic), NOTIONAL * QUANTITY * TOL);
    // standard settlement < trade settlement < coupon date
    TradeInfo tradeInfo5 = TradeInfo.builder()
        .tradeDate(TRADE_BEFORE)
        .settlementDate(TRADE_BEFORE.plusDays(7))
        .build();
    FixedCouponBondTrade trade5 = FixedCouponBondTrade.builder()
        .securityLink(SECURITY_LINK_NO_EXCOUPON)
        .tradeInfo(tradeInfo5)
        .quantity(QUANTITY)
        .payment(UPFRONT_PAYMENT_ZERO)
        .build();
    CurrencyAmount computed5 = TRADE_PRICER.presentValueFromCleanPrice(trade5, PROVIDER_BEFORE, cleanPrice);
    CurrencyAmount computed5Continuous = TRADE_PRICER.presentValueFromCleanPriceWithZSpread(
        trade5, PROVIDER_BEFORE, cleanPrice, Z_SPREAD, CONTINUOUS, 0);
    CurrencyAmount computed5Periodic = TRADE_PRICER.presentValueFromCleanPriceWithZSpread(
        trade5, PROVIDER_BEFORE, cleanPrice, Z_SPREAD, PERIODIC, PERIOD_PER_YEAR);
    assertEquals(computed5.getAmount(), QUANTITY * basePv4, NOTIONAL * QUANTITY * TOL);
    assertEquals(computed5Continuous.getAmount(), QUANTITY * basePv4, NOTIONAL * QUANTITY * TOL);
    assertEquals(computed5Periodic.getAmount(), QUANTITY * basePv4, NOTIONAL * QUANTITY * TOL);
    // trade settlement < standard settlement < coupon date
    FixedCouponBondTrade trade6 = FixedCouponBondTrade.builder()
        .securityLink(SECURITY_LINK_NO_EXCOUPON)
        .tradeInfo(TRADE_INFO_BEFORE)
        .quantity(QUANTITY)
        .payment(UPFRONT_PAYMENT_ZERO)
        .build();
    CurrencyAmount computed6 = TRADE_PRICER.presentValueFromCleanPrice(trade6, PROVIDER_BEFORE, cleanPrice);
    CurrencyAmount computed6Continuous = TRADE_PRICER.presentValueFromCleanPriceWithZSpread(
        trade6, PROVIDER_BEFORE, cleanPrice, Z_SPREAD, CONTINUOUS, 0);
    CurrencyAmount computed6Periodic = TRADE_PRICER.presentValueFromCleanPriceWithZSpread(
        trade6, PROVIDER_BEFORE, cleanPrice, Z_SPREAD, PERIODIC, PERIOD_PER_YEAR);
    assertEquals(computed6.getAmount(), QUANTITY * basePv4, NOTIONAL * QUANTITY * TOL);
    assertEquals(computed6Continuous.getAmount(), QUANTITY * basePv4, NOTIONAL * QUANTITY * TOL);
    assertEquals(computed6Periodic.getAmount(), QUANTITY * basePv4, NOTIONAL * QUANTITY * TOL);
  }

  //-------------------------------------------------------------------------
  public void test_presentValueFromCleanPrice_coherency() {
    double priceDirty = PRODUCT_PRICER.dirtyPriceFromCurves(BOND_SECURITY, PROVIDER);
    LocalDate standardSettlementDate = PRODUCT.getSettlementDateOffset().adjust(PROVIDER.getValuationDate());
    double priceCleanComputed = PRODUCT_PRICER.cleanPriceFromDirtyPrice(PRODUCT, standardSettlementDate, priceDirty);
    CurrencyAmount pvCleanPrice = TRADE_PRICER.presentValueFromCleanPrice(TRADE, PROVIDER, priceCleanComputed);
    CurrencyAmount pvCurves = TRADE_PRICER.presentValue(TRADE, PROVIDER);
    assertEquals(pvCleanPrice.getAmount(), pvCurves.getAmount(), NOTIONAL * TOL);
  }

  public void test_presentValueFromCleanPriceWithZSpread_continuous_coherency() {
    double priceDirty = PRODUCT_PRICER
        .dirtyPriceFromCurvesWithZSpread(BOND_SECURITY, PROVIDER, Z_SPREAD, CONTINUOUS, 0);
    LocalDate standardSettlementDate = PRODUCT.getSettlementDateOffset().adjust(PROVIDER.getValuationDate());
    double priceCleanComputed = PRODUCT_PRICER.cleanPriceFromDirtyPrice(PRODUCT, standardSettlementDate, priceDirty);
    CurrencyAmount pvCleanPrice = TRADE_PRICER.presentValueFromCleanPriceWithZSpread(
        TRADE, PROVIDER, priceCleanComputed, Z_SPREAD, CONTINUOUS, 0);
    CurrencyAmount pvCurves = TRADE_PRICER.presentValueWithZSpread(TRADE, PROVIDER, Z_SPREAD, CONTINUOUS, 0);
    assertEquals(pvCleanPrice.getAmount(), pvCurves.getAmount(), NOTIONAL * TOL);
  }

  public void test_presentValueFromCleanPriceWithZSpread_periodic_coherency() {
    double priceDirty = PRODUCT_PRICER.dirtyPriceFromCurvesWithZSpread(
        BOND_SECURITY, PROVIDER, Z_SPREAD, PERIODIC, PERIOD_PER_YEAR);
    LocalDate standardSettlementDate = PRODUCT.getSettlementDateOffset().adjust(PROVIDER.getValuationDate());
    double priceCleanComputed = PRODUCT_PRICER.cleanPriceFromDirtyPrice(PRODUCT, standardSettlementDate, priceDirty);
    CurrencyAmount pvCleanPrice = TRADE_PRICER.presentValueFromCleanPriceWithZSpread(
        TRADE, PROVIDER, priceCleanComputed, Z_SPREAD, PERIODIC, PERIOD_PER_YEAR);
    CurrencyAmount pvCurves = TRADE_PRICER
        .presentValueWithZSpread(TRADE, PROVIDER, Z_SPREAD, PERIODIC, PERIOD_PER_YEAR);
    assertEquals(pvCleanPrice.getAmount(), pvCurves.getAmount(), NOTIONAL * TOL);
  }

  public void test_presentValueFromCleanPrice_noExcoupon_coherency() {
    double priceDirty = PRODUCT_PRICER.dirtyPriceFromCurves(BOND_SECURITY_NO_EXCOUPON, PROVIDER);
    LocalDate standardSettlementDate = PRODUCT.getSettlementDateOffset().adjust(PROVIDER.getValuationDate());
    double priceCleanComputed = PRODUCT_PRICER.cleanPriceFromDirtyPrice(PRODUCT, standardSettlementDate, priceDirty);
    CurrencyAmount pvCleanPrice = TRADE_PRICER.presentValueFromCleanPrice(
        TRADE_NO_EXCOUPON, PROVIDER, priceCleanComputed);
    CurrencyAmount pvCurves = TRADE_PRICER.presentValue(TRADE_NO_EXCOUPON, PROVIDER);
    assertEquals(pvCleanPrice.getAmount(), pvCurves.getAmount(), NOTIONAL * TOL);
  }

  public void test_presentValueFromCleanPriceWithZSpread_continuous_noExcoupon_coherency() {
    double priceDirty = PRODUCT_PRICER.dirtyPriceFromCurvesWithZSpread(
        BOND_SECURITY_NO_EXCOUPON, PROVIDER, Z_SPREAD, CONTINUOUS, 0);
    LocalDate standardSettlementDate = PRODUCT.getSettlementDateOffset().adjust(PROVIDER.getValuationDate());
    double priceCleanComputed = PRODUCT_PRICER.cleanPriceFromDirtyPrice(PRODUCT, standardSettlementDate, priceDirty);
    CurrencyAmount pvCleanPrice = TRADE_PRICER.presentValueFromCleanPriceWithZSpread(
        TRADE_NO_EXCOUPON, PROVIDER, priceCleanComputed, Z_SPREAD, CONTINUOUS, 0);
    CurrencyAmount pvCurves = TRADE_PRICER
        .presentValueWithZSpread(TRADE_NO_EXCOUPON, PROVIDER, Z_SPREAD, CONTINUOUS, 0);
    assertEquals(pvCleanPrice.getAmount(), pvCurves.getAmount(), NOTIONAL * TOL);
  }

  public void test_presentValueFromCleanPriceWithZSpread_periodic_noExcoupon_coherency() {
    double priceDirty = PRODUCT_PRICER.dirtyPriceFromCurvesWithZSpread(
        BOND_SECURITY_NO_EXCOUPON, PROVIDER, Z_SPREAD, PERIODIC, PERIOD_PER_YEAR);
    LocalDate standardSettlementDate = PRODUCT.getSettlementDateOffset().adjust(PROVIDER.getValuationDate());
    double priceCleanComputed = PRODUCT_PRICER.cleanPriceFromDirtyPrice(PRODUCT, standardSettlementDate, priceDirty);
    CurrencyAmount pvCleanPrice = TRADE_PRICER.presentValueFromCleanPriceWithZSpread(
        TRADE_NO_EXCOUPON, PROVIDER, priceCleanComputed, Z_SPREAD, PERIODIC, PERIOD_PER_YEAR);
    CurrencyAmount pvCurves = TRADE_PRICER.presentValueWithZSpread(
        TRADE_NO_EXCOUPON, PROVIDER, Z_SPREAD, PERIODIC, PERIOD_PER_YEAR);
    assertEquals(pvCleanPrice.getAmount(), pvCurves.getAmount(), NOTIONAL * TOL);
  }

  //-------------------------------------------------------------------------
  public void test_presentValueSensitivity() {
    PointSensitivityBuilder pointTrade = TRADE_PRICER.presentValueSensitivity(TRADE, PROVIDER);
    CurveCurrencyParameterSensitivities computedTrade = PROVIDER.curveParameterSensitivity(pointTrade.build());
    CurveCurrencyParameterSensitivities expectedTrade = FD_CAL.sensitivity(PROVIDER,
        (p) -> TRADE_PRICER.presentValue(TRADE, (p)));
    assertTrue(computedTrade.equalWithTolerance(expectedTrade, 30d * NOTIONAL * QUANTITY * EPS));
  }

  public void test_presentValueSensitivityWithZSpread_continuous() {
    PointSensitivityBuilder pointTrade =
        TRADE_PRICER.presentValueSensitivityWithZSpread(TRADE, PROVIDER, Z_SPREAD, CONTINUOUS, 0);
    CurveCurrencyParameterSensitivities computedTrade = PROVIDER.curveParameterSensitivity(pointTrade.build());
    CurveCurrencyParameterSensitivities expectedTrade = FD_CAL.sensitivity(
        PROVIDER, (p) -> TRADE_PRICER.presentValueWithZSpread(TRADE, (p), Z_SPREAD, CONTINUOUS, 0));
    assertTrue(computedTrade.equalWithTolerance(expectedTrade, 20d * NOTIONAL * QUANTITY * EPS));
  }

  public void test_presentValueSensitivityWithZSpread_periodic() {
    PointSensitivityBuilder pointTrade =
        TRADE_PRICER.presentValueSensitivityWithZSpread(TRADE, PROVIDER, Z_SPREAD, PERIODIC, PERIOD_PER_YEAR);
    CurveCurrencyParameterSensitivities computedTrade = PROVIDER.curveParameterSensitivity(pointTrade.build());
    CurveCurrencyParameterSensitivities expectedTrade = FD_CAL.sensitivity(PROVIDER,
        (p) -> TRADE_PRICER.presentValueWithZSpread(TRADE, (p), Z_SPREAD, PERIODIC, PERIOD_PER_YEAR));
    assertTrue(computedTrade.equalWithTolerance(expectedTrade, 20d * NOTIONAL * QUANTITY * EPS));
  }

  public void test_presentValueProductSensitivity_noExcoupon() {
    PointSensitivityBuilder pointTrade = TRADE_PRICER.presentValueSensitivity(TRADE_NO_EXCOUPON, PROVIDER);
    CurveCurrencyParameterSensitivities computedTrade = PROVIDER.curveParameterSensitivity(pointTrade.build());
    CurveCurrencyParameterSensitivities expectedTrade = FD_CAL.sensitivity(
        PROVIDER, (p) -> TRADE_PRICER.presentValue(TRADE_NO_EXCOUPON, (p)));
    assertTrue(computedTrade.equalWithTolerance(expectedTrade, 30d * NOTIONAL * QUANTITY * EPS));
  }

  public void test_presentValueSensitivityWithZSpread_continuous_noExcoupon() {
    PointSensitivityBuilder pointTrade =
        TRADE_PRICER.presentValueSensitivityWithZSpread(TRADE_NO_EXCOUPON, PROVIDER, Z_SPREAD, CONTINUOUS, 0);
    CurveCurrencyParameterSensitivities computedTrade = PROVIDER.curveParameterSensitivity(pointTrade.build());
    CurveCurrencyParameterSensitivities expectedTrade = FD_CAL.sensitivity(PROVIDER, (p) ->
        TRADE_PRICER.presentValueWithZSpread(TRADE_NO_EXCOUPON, (p), Z_SPREAD, CONTINUOUS, 0));
    assertTrue(computedTrade.equalWithTolerance(expectedTrade, 20d * NOTIONAL * QUANTITY * EPS));
  }

  public void test_presentValueSensitivityWithZSpread_periodic_noExcoupon() {
    PointSensitivityBuilder pointTrade = TRADE_PRICER.presentValueSensitivityWithZSpread(
        TRADE_NO_EXCOUPON, PROVIDER, Z_SPREAD, PERIODIC, PERIOD_PER_YEAR);
    CurveCurrencyParameterSensitivities computedTrade = PROVIDER.curveParameterSensitivity(pointTrade.build());
    CurveCurrencyParameterSensitivities expectedTrade = FD_CAL.sensitivity(PROVIDER, (p) ->
        TRADE_PRICER.presentValueWithZSpread(TRADE_NO_EXCOUPON, (p), Z_SPREAD, PERIODIC, PERIOD_PER_YEAR));
    assertTrue(computedTrade.equalWithTolerance(expectedTrade, 20d * NOTIONAL * QUANTITY * EPS));
  }

  //-------------------------------------------------------------------------
  public void test_presentValueSensitivity_dateLogic() {
    FixedCouponBondTrade tradeAfter = FixedCouponBondTrade.builder()
        .securityLink(SECURITY_LINK)
        .tradeInfo(TRADE_INFO)
        .quantity(QUANTITY)
        .payment(UPFRONT_PAYMENT_ZERO)
        .build();
    PointSensitivities computedTradeAfter = TRADE_PRICER.presentValueSensitivity(tradeAfter, PROVIDER_BEFORE).build();
    // settle before detachment date
    FixedCouponBondTrade tradeBefore = FixedCouponBondTrade.builder()
        .securityLink(SECURITY_LINK)
        .tradeInfo(TRADE_INFO_BEFORE)
        .quantity(QUANTITY)
        .payment(UPFRONT_PAYMENT_ZERO)
        .build();
    PointSensitivities computedTradeBefore = TRADE_PRICER.presentValueSensitivity(tradeBefore, PROVIDER_BEFORE).build();
    FixedCouponBondPaymentPeriod periodExtra = findPeriod(PRODUCT.expand(), SETTLE_BEFORE, SETTLEMENT);
    PointSensitivities sensiExtra = COUPON_PRICER
        .presentValueSensitivity(periodExtra, PROVIDER_BEFORE.issuerCurveDiscountFactors(ISSUER_ID, EUR)).build();
    assertTrue(computedTradeBefore.normalized().equalWithTolerance(
        computedTradeAfter.combinedWith(sensiExtra.multipliedBy(QUANTITY)).normalized(), NOTIONAL * QUANTITY * TOL));
    // settle on detachment date
    FixedCouponBondTrade tradeOnDetachment = FixedCouponBondTrade.builder()
        .securityLink(SECURITY_LINK)
        .tradeInfo(TRADE_INFO_ON_DETACHMENT)
        .quantity(QUANTITY)
        .payment(UPFRONT_PAYMENT_ZERO)
        .build();
    PointSensitivities computedTradeOnDetachment =
        TRADE_PRICER.presentValueSensitivity(tradeOnDetachment, PROVIDER_BEFORE).build();
    assertTrue(computedTradeOnDetachment.equalWithTolerance(computedTradeAfter, NOTIONAL * QUANTITY * TOL));
    // settle between detachment date and coupon date
    FixedCouponBondTrade tradeBtwnDetachmentCoupon = FixedCouponBondTrade.builder()
        .securityLink(SECURITY_LINK)
        .tradeInfo(TRADE_INFO_BTWN_DETACHMENT_COUPON)
        .quantity(QUANTITY)
        .payment(UPFRONT_PAYMENT_ZERO)
        .build();
    PointSensitivities computedTradeBtwnDetachmentCoupon =
        TRADE_PRICER.presentValueSensitivity(tradeBtwnDetachmentCoupon, PROVIDER_BEFORE).build();
    assertTrue(computedTradeBtwnDetachmentCoupon.equalWithTolerance(computedTradeAfter, NOTIONAL * QUANTITY * TOL));
  }

  public void test_presentValueSensitivity_dateLogic_pastSettle() {
    FixedCouponBondTrade tradeAfter = FixedCouponBondTrade.builder()
        .securityLink(SECURITY_LINK)
        .tradeInfo(TRADE_INFO)
        .quantity(QUANTITY)
        .payment(UPFRONT_PAYMENT_ZERO)
        .build();
    PointSensitivities computedTradeAfter = TRADE_PRICER.presentValueSensitivity(tradeAfter, PROVIDER).build();
    // settle before detachment date
    FixedCouponBondTrade tradeBefore = FixedCouponBondTrade.builder()
        .securityLink(SECURITY_LINK)
        .tradeInfo(TRADE_INFO_BEFORE)
        .quantity(QUANTITY)
        .payment(UPFRONT_PAYMENT_ZERO)
        .build();
    PointSensitivities computedTradeBefore = TRADE_PRICER.presentValueSensitivity(tradeBefore, PROVIDER).build();
    assertTrue(computedTradeBefore.equalWithTolerance(computedTradeAfter, NOTIONAL * QUANTITY * TOL));
    // settle on detachment date
    FixedCouponBondTrade tradeOnDetachment = FixedCouponBondTrade.builder()
        .securityLink(SECURITY_LINK)
        .tradeInfo(TRADE_INFO_ON_DETACHMENT)
        .quantity(QUANTITY)
        .payment(UPFRONT_PAYMENT_ZERO)
        .build();
    PointSensitivities computedTradeOnDetachment =
        TRADE_PRICER.presentValueSensitivity(tradeOnDetachment, PROVIDER).build();
    assertTrue(computedTradeOnDetachment.equalWithTolerance(computedTradeAfter, NOTIONAL * QUANTITY * TOL));
    // settle between detachment date and coupon date
    FixedCouponBondTrade tradeBtwnDetachmentCoupon = FixedCouponBondTrade.builder()
        .securityLink(SECURITY_LINK)
        .tradeInfo(TRADE_INFO_BTWN_DETACHMENT_COUPON)
        .quantity(QUANTITY)
        .payment(UPFRONT_PAYMENT_ZERO)
        .build();
    PointSensitivities computedTradeBtwnDetachmentCoupon =
        TRADE_PRICER.presentValueSensitivity(tradeBtwnDetachmentCoupon, PROVIDER).build();
    assertTrue(computedTradeBtwnDetachmentCoupon.equalWithTolerance(computedTradeAfter, NOTIONAL * QUANTITY * TOL));
  }

  public void test_presentValueSensitivity_dateLogic_noExcoupon() {
    FixedCouponBondTrade tradeAfter = FixedCouponBondTrade.builder()
        .securityLink(SECURITY_LINK_NO_EXCOUPON)
        .tradeInfo(TRADE_INFO)
        .quantity(QUANTITY)
        .payment(UPFRONT_PAYMENT_ZERO)
        .build();
    PointSensitivities computedTradeAfter = TRADE_PRICER.presentValueSensitivity(tradeAfter, PROVIDER_BEFORE).build();
    // settle before coupon date
    FixedCouponBondTrade tradeBefore = FixedCouponBondTrade.builder()
        .securityLink(SECURITY_LINK_NO_EXCOUPON)
        .tradeInfo(TRADE_INFO_BEFORE)
        .quantity(QUANTITY)
        .payment(UPFRONT_PAYMENT_ZERO)
        .build();
    PointSensitivities computedTradeBefore = TRADE_PRICER.presentValueSensitivity(tradeBefore, PROVIDER_BEFORE).build();
    FixedCouponBondPaymentPeriod periodExtra = findPeriod(PRODUCT_NO_EXCOUPON.expand(), SETTLE_BEFORE, SETTLEMENT);
    PointSensitivities sensiExtra = COUPON_PRICER
        .presentValueSensitivity(periodExtra, PROVIDER_BEFORE.issuerCurveDiscountFactors(ISSUER_ID, EUR)).build();
    assertTrue(computedTradeBefore.normalized().equalWithTolerance(
        computedTradeAfter.combinedWith(sensiExtra.multipliedBy(QUANTITY)).normalized(), NOTIONAL * QUANTITY * TOL));
    // settle on coupon date
    FixedCouponBondTrade tradeOnCoupon = FixedCouponBondTrade.builder()
        .securityLink(SECURITY_LINK_NO_EXCOUPON)
        .tradeInfo(TRADE_INFO_ON_COUPON)
        .quantity(QUANTITY)
        .payment(UPFRONT_PAYMENT_ZERO)
        .build();
    PointSensitivities computedTradeOnCoupon = TRADE_PRICER.presentValueSensitivity(tradeOnCoupon, PROVIDER_BEFORE)
        .build();
    assertTrue(computedTradeOnCoupon.equalWithTolerance(computedTradeAfter, NOTIONAL * QUANTITY * TOL));
  }

  public void test_presentValueSensitivity_dateLogic_pastSettle_noExcoupon() {
    FixedCouponBondTrade tradeAfter = FixedCouponBondTrade.builder()
        .securityLink(SECURITY_LINK_NO_EXCOUPON)
        .tradeInfo(TRADE_INFO)
        .quantity(QUANTITY)
        .payment(UPFRONT_PAYMENT_ZERO)
        .build();
    PointSensitivities computedTradeAfter = TRADE_PRICER.presentValueSensitivity(tradeAfter, PROVIDER).build();
    // settle before coupon date
    FixedCouponBondTrade tradeBefore = FixedCouponBondTrade.builder()
        .securityLink(SECURITY_LINK_NO_EXCOUPON)
        .tradeInfo(TRADE_INFO_BEFORE)
        .quantity(QUANTITY)
        .payment(UPFRONT_PAYMENT_ZERO)
        .build();
    PointSensitivities computedTradeBefore = TRADE_PRICER.presentValueSensitivity(tradeBefore, PROVIDER).build();
    assertTrue(computedTradeBefore.equalWithTolerance(computedTradeAfter, NOTIONAL * QUANTITY * TOL));
    // settle on coupon date
    FixedCouponBondTrade tradeOnCoupon = FixedCouponBondTrade.builder()
        .securityLink(SECURITY_LINK_NO_EXCOUPON)
        .tradeInfo(TRADE_INFO_ON_COUPON)
        .quantity(QUANTITY)
        .payment(UPFRONT_PAYMENT_ZERO)
        .build();
    PointSensitivities computedTradeOnCoupon = TRADE_PRICER.presentValueSensitivity(tradeOnCoupon, PROVIDER).build();
    assertTrue(computedTradeOnCoupon.equalWithTolerance(computedTradeAfter, NOTIONAL * QUANTITY * TOL));
  }

  //-------------------------------------------------------------------------
  public void test_currencyExposure() {
    MultiCurrencyAmount ceComputed = TRADE_PRICER.currencyExposure(TRADE, PROVIDER);
    CurrencyAmount pv = TRADE_PRICER.presentValue(TRADE, PROVIDER);
    assertEquals(ceComputed, MultiCurrencyAmount.of(pv));
  }

  public void test_currencyExposureWithZSpread() {
    MultiCurrencyAmount ceComputed = TRADE_PRICER.currencyExposureWithZSpread(
        TRADE, PROVIDER, Z_SPREAD, PERIODIC, PERIOD_PER_YEAR);
    CurrencyAmount pv = TRADE_PRICER.presentValueWithZSpread(TRADE, PROVIDER, Z_SPREAD, PERIODIC, PERIOD_PER_YEAR);
    assertEquals(ceComputed, MultiCurrencyAmount.of(pv));
  }

  public void test_currentCash_zero() {
    CurrencyAmount ccComputed = TRADE_PRICER.currentCash(TRADE, VAL_DATE);
    assertEquals(ccComputed, CurrencyAmount.zero(EUR));
  }

  public void test_currentCash_valuationAtSettlement() {
    CurrencyAmount ccComputed = TRADE_PRICER.currentCash(TRADE, SETTLEMENT);
    assertEquals(ccComputed, UPFRONT_PAYMENT.getValue());
  }

  public void test_currentCash_valuationAtPayment() {
    LocalDate paymentDate = LocalDate.of(2016, 10, 12);
    CurrencyAmount ccComputed = TRADE_PRICER.currentCash(TRADE, paymentDate);
    assertEquals(ccComputed, CurrencyAmount.zero(EUR));
  }

  public void test_currentCash_valuationAtPayment_noExcoupon() {
    LocalDate startDate = LocalDate.of(2016, 4, 12);
    LocalDate paymentDate = LocalDate.of(2016, 10, 12);
    double yc = DAY_COUNT.relativeYearFraction(startDate, paymentDate);
    CurrencyAmount ccComputed = TRADE_PRICER.currentCash(TRADE_NO_EXCOUPON, paymentDate);
    assertEquals(ccComputed, CurrencyAmount.of(EUR, FIXED_RATE * NOTIONAL * yc * QUANTITY));
  }

  public void test_currentCash_valuationAtMaturity() {
    LocalDate paymentDate = LocalDate.of(2025, 4, 14);
    CurrencyAmount ccComputed = TRADE_PRICER.currentCash(TRADE, paymentDate);
    assertEquals(ccComputed, CurrencyAmount.of(EUR, NOTIONAL * QUANTITY));
  }

  public void test_currentCash_valuationAtMaturity_noExcoupon() {
    LocalDate startDate = LocalDate.of(2024, 10, 14);
    LocalDate paymentDate = LocalDate.of(2025, 4, 14);
    double yc = DAY_COUNT.relativeYearFraction(startDate, paymentDate);
    CurrencyAmount ccComputed = TRADE_PRICER.currentCash(TRADE_NO_EXCOUPON, paymentDate);
    assertEquals(ccComputed, CurrencyAmount.of(EUR, NOTIONAL * (1d + yc * FIXED_RATE) * QUANTITY));
  }

  //-------------------------------------------------------------------------
  private static LegalEntityDiscountingProvider createRatesProvider(LocalDate valuationDate) {
    DiscountFactors dscRepo = ZeroRateDiscountFactors.of(EUR, valuationDate, CURVE_REPO);
    DiscountFactors dscIssuer = ZeroRateDiscountFactors.of(EUR, valuationDate, CURVE_ISSUER);
    LegalEntityDiscountingProvider provider = LegalEntityDiscountingProvider.builder()
        .issuerCurves(ImmutableMap.<Pair<LegalEntityGroup, Currency>, DiscountFactors>of(
            Pair.<LegalEntityGroup, Currency>of(GROUP_ISSUER, EUR), dscIssuer))
        .legalEntityMap(ImmutableMap.<StandardId, LegalEntityGroup>of(ISSUER_ID, GROUP_ISSUER))
        .repoCurves(ImmutableMap.<Pair<BondGroup, Currency>, DiscountFactors>of(
            Pair.<BondGroup, Currency>of(GROUP_REPO, EUR), dscRepo))
        .bondMap(ImmutableMap.<StandardId, BondGroup>of(SECURITY_ID, GROUP_REPO))
        .valuationDate(valuationDate)
        .build();
    return provider;
  }

  private FixedCouponBondPaymentPeriod findPeriod(ExpandedFixedCouponBond bond, LocalDate date1, LocalDate date2) {
    ImmutableList<FixedCouponBondPaymentPeriod> list = bond.getPeriodicPayments();
    for (FixedCouponBondPaymentPeriod period : list) {
      if (period.getDetachmentDate().equals(period.getPaymentDate())) {
        if (period.getPaymentDate().isAfter(date1) && period.getPaymentDate().isBefore(date2)) {
          return period;
        }
      } else {
        if (period.getDetachmentDate().isAfter(date1) && period.getDetachmentDate().isBefore(date2)) {
          return period;
        }
      }
    }
    return null;
  }
}
