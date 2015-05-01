/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.basics.currency;

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.ImmutableValidator;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.google.common.math.DoubleMath;
import com.opengamma.strata.collect.ArgChecker;

/**
 * A single foreign exchange rate between two currencies, such as 'EUR/USD 1.25'.
 * <p>
 * This represents a rate of foreign exchange. The rate 'EUR/USD 1.25' consists of three
 * elements - the base currency 'EUR', the counter currency 'USD' and the rate '1.25'.
 * When performing a conversion a rate of '1.25' means that '1 EUR = 1.25 USD'.
 * <p>
 * See {@link CurrencyPair} for the representation that does not contain a rate.
 * <p>
 * This class is immutable and thread-safe.
 */
@BeanDefinition(builderScope = "private")
public final class FxRate
    implements FxRateProvider, ImmutableBean, Serializable {

  /**
   * Regular expression to parse the textual format.
   */
  private static final Pattern REGEX_FORMAT = Pattern.compile("([A-Z]{3})[/]([A-Z]{3})[ ]([0-9+.-]+)");

  /**
   * The currency pair.
   * The pair is formed of two parts, the base and the counter.
   * In the pair 'AAA/BBB' the base is 'AAA' and the counter is 'BBB'.
   */
  @PropertyDefinition(validate = "notNull")
  private final CurrencyPair pair;
  /**
   * The rate applicable to the currency pair.
   * One unit of the base currency is exchanged for this amount of the counter currency.
   */
  @PropertyDefinition(validate = "ArgChecker.notNegativeOrZero")
  private final double rate;

  //-------------------------------------------------------------------------
  /**
   * Obtains an FX rate from two currencies.
   * <p>
   * The first currency is the base and the second is the counter.
   * The two currencies may be the same, but if they are then the rate must be one.
   * 
   * @param base  the base currency
   * @param counter  the counter currency
   * @param rate  the conversion rate, greater than zero
   * @return the FX rate
   * @throws IllegalArgumentException if the rate is invalid
   */
  public static FxRate of(Currency base, Currency counter, double rate) {
    return new FxRate(CurrencyPair.of(base, counter), rate);
  }

  /**
   * Obtains an FX rate from a currency pair.
   * <p>
   * The two currencies may be the same, but if they are then the rate must be one.
   * 
   * @param pair  the currency pair
   * @param rate  the conversion rate, greater than zero
   * @return the FX rate
   * @throws IllegalArgumentException if the rate is invalid
   */
  public static FxRate of(CurrencyPair pair, double rate) {
    return new FxRate(pair, rate);
  }

  //-------------------------------------------------------------------------
  /**
   * Parses a rate from a string with format AAA/BBB RATE.
   * <p>
   * The parsed format is '${baseCurrency}/${counterCurrency} ${rate}'.
   * Currency parsing is case insensitive.
   * 
   * @param rateStr  the rate as a string AAA/BBB RATE
   * @return the FX rate
   * @throws IllegalArgumentException if the FX rate cannot be parsed
   */
  public static FxRate parse(String rateStr) {
    ArgChecker.notNull(rateStr, "rateStr");
    Matcher matcher = REGEX_FORMAT.matcher(rateStr.toUpperCase(Locale.ENGLISH));
    if (matcher.matches() == false) {
      throw new IllegalArgumentException("Invalid rate: " + rateStr);
    }
    try {
      Currency base = Currency.parse(matcher.group(1));
      Currency counter = Currency.parse(matcher.group(2));
      double rate = Double.parseDouble(matcher.group(3));
      return new FxRate(CurrencyPair.of(base, counter), rate);
    } catch (RuntimeException ex) {
      throw new IllegalArgumentException("Unable to parse rate: " + rateStr, ex);
    }
  }

  //-------------------------------------------------------------------------
  @ImmutableValidator
  private void validate() {
    if (pair.getBase().equals(pair.getCounter()) && rate != 1d) {
      throw new IllegalArgumentException("Conversion rate between identical currencies must be one");
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the inverse rate.
   * <p>
   * The inverse rate has the same currencies but in reverse order.
   * The rate is the reciprocal of the original.
   * 
   * @return the inverse pair
   */
  public FxRate inverse() {
    return new FxRate(pair.inverse(), 1d / rate);
  }

  /**
   * Gets the FX rate for the specified base currency.
   * <p>
   * This returns either the stored rate of the inverse rate.
   * The stored rate is returned if the required base currency matches the stored
   * base currency. The inverse or the stored rate is returned if the required
   * base currency matches the stored counter currency.
   * <p>
   * The rate returned is the rate from the required base currency to the other currency
   * as defined by this formula: {@code (1 * requiredBaseCurrency = fxRate * otherCurrency)}.
   * 
   * @param requiredBaseCurrency  the base currency of the returned rate
   * @return the FX rate from the specified base currency to the other currency
   * @throws IllegalArgumentException if the base currency is not one of those in this object
   */
  public double fxRate(Currency requiredBaseCurrency) {
    if (requiredBaseCurrency.equals(pair.getBase())) {
      return rate;
    }
    if (requiredBaseCurrency.equals(pair.getCounter())) {
      return 1d / rate;
    }
    throw new IllegalArgumentException("Required currency is not contained in " + pair + ": " + requiredBaseCurrency);
  }

  /**
   * Gets the FX rate for the specified currency pair.
   * <p>
   * The rate returned is the rate from the base currency to the counter currency
   * as defined by this formula: {@code (1 * baseCurrency = fxRate * counterCurrency)}.
   * <p>
   * This will return the rate or inverse rate, or 1 if the two input currencies are the same.
   * 
   * @param baseCurrency  the base currency, to convert from
   * @param counterCurrency  the counter currency, to convert to
   * @return the FX rate for the currency pair
   * @throws IllegalArgumentException if no FX rate could be found
   */
  @Override
  public double fxRate(Currency baseCurrency, Currency counterCurrency) {
    if (baseCurrency.equals(counterCurrency)) {
      return 1d;
    }
    if (baseCurrency.equals(pair.getBase()) && counterCurrency.equals(pair.getCounter())) {
      return rate;
    }
    if (counterCurrency.equals(pair.getBase()) && baseCurrency.equals(pair.getCounter())) {
      return 1d / rate;
    }
    throw new IllegalArgumentException("Unknown rate: " + baseCurrency + "/" + counterCurrency);
  }

  //-------------------------------------------------------------------------
  /**
   * Returns the formatted string version of the currency pair.
   * <p>
   * The format is '${baseCurrency}/${counterCurrency} ${rate}'.
   * 
   * @return the formatted string
   */
  @Override
  public String toString() {
    return pair + " " + (DoubleMath.isMathematicalInteger(rate) ? Long.toString((long) rate) : Double.toString(rate));
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code FxRate}.
   * @return the meta-bean, not null
   */
  public static FxRate.Meta meta() {
    return FxRate.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(FxRate.Meta.INSTANCE);
  }

  /**
   * The serialization version id.
   */
  private static final long serialVersionUID = 1L;

  private FxRate(
      CurrencyPair pair,
      double rate) {
    JodaBeanUtils.notNull(pair, "pair");
    ArgChecker.notNegativeOrZero(rate, "rate");
    this.pair = pair;
    this.rate = rate;
    validate();
  }

  @Override
  public FxRate.Meta metaBean() {
    return FxRate.Meta.INSTANCE;
  }

  @Override
  public <R> Property<R> property(String propertyName) {
    return metaBean().<R>metaProperty(propertyName).createProperty(this);
  }

  @Override
  public Set<String> propertyNames() {
    return metaBean().metaPropertyMap().keySet();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the currency pair.
   * The pair is formed of two parts, the base and the counter.
   * In the pair 'AAA/BBB' the base is 'AAA' and the counter is 'BBB'.
   * @return the value of the property, not null
   */
  public CurrencyPair getPair() {
    return pair;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the rate applicable to the currency pair.
   * One unit of the base currency is exchanged for this amount of the counter currency.
   * @return the value of the property
   */
  public double getRate() {
    return rate;
  }

  //-----------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      FxRate other = (FxRate) obj;
      return JodaBeanUtils.equal(getPair(), other.getPair()) &&
          JodaBeanUtils.equal(getRate(), other.getRate());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getPair());
    hash = hash * 31 + JodaBeanUtils.hashCode(getRate());
    return hash;
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code FxRate}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code pair} property.
     */
    private final MetaProperty<CurrencyPair> pair = DirectMetaProperty.ofImmutable(
        this, "pair", FxRate.class, CurrencyPair.class);
    /**
     * The meta-property for the {@code rate} property.
     */
    private final MetaProperty<Double> rate = DirectMetaProperty.ofImmutable(
        this, "rate", FxRate.class, Double.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "pair",
        "rate");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 3433178:  // pair
          return pair;
        case 3493088:  // rate
          return rate;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends FxRate> builder() {
      return new FxRate.Builder();
    }

    @Override
    public Class<? extends FxRate> beanType() {
      return FxRate.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code pair} property.
     * @return the meta-property, not null
     */
    public MetaProperty<CurrencyPair> pair() {
      return pair;
    }

    /**
     * The meta-property for the {@code rate} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Double> rate() {
      return rate;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 3433178:  // pair
          return ((FxRate) bean).getPair();
        case 3493088:  // rate
          return ((FxRate) bean).getRate();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      metaProperty(propertyName);
      if (quiet) {
        return;
      }
      throw new UnsupportedOperationException("Property cannot be written: " + propertyName);
    }

  }

  //-----------------------------------------------------------------------
  /**
   * The bean-builder for {@code FxRate}.
   */
  private static final class Builder extends DirectFieldsBeanBuilder<FxRate> {

    private CurrencyPair pair;
    private double rate;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case 3433178:  // pair
          return pair;
        case 3493088:  // rate
          return rate;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 3433178:  // pair
          this.pair = (CurrencyPair) newValue;
          break;
        case 3493088:  // rate
          this.rate = (Double) newValue;
          break;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
      return this;
    }

    @Override
    public Builder set(MetaProperty<?> property, Object value) {
      super.set(property, value);
      return this;
    }

    @Override
    public Builder setString(String propertyName, String value) {
      setString(meta().metaProperty(propertyName), value);
      return this;
    }

    @Override
    public Builder setString(MetaProperty<?> property, String value) {
      super.setString(property, value);
      return this;
    }

    @Override
    public Builder setAll(Map<String, ? extends Object> propertyValueMap) {
      super.setAll(propertyValueMap);
      return this;
    }

    @Override
    public FxRate build() {
      return new FxRate(
          pair,
          rate);
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(96);
      buf.append("FxRate.Builder{");
      buf.append("pair").append('=').append(JodaBeanUtils.toString(pair)).append(',').append(' ');
      buf.append("rate").append('=').append(JodaBeanUtils.toString(rate));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
