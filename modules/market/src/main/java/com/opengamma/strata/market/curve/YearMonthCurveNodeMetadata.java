/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.market.curve;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.ImmutablePreBuild;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.strata.collect.ArgChecker;

/**
 * Curve node metadata for a curve node with a specific year-month.
 * <p>
 * This is typically used for futures.
 */
@BeanDefinition(builderScope = "private")
public final class YearMonthCurveNodeMetadata
    implements DatedCurveParameterMetadata, ImmutableBean, Serializable {

  /**
   * Formatter for Jan15.
   */
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMMyy").withLocale(Locale.ENGLISH);

  /**
   * The date of the curve node.
   * <p>
   * This is the date that the node on the curve is defined as.
   * There is not necessarily a direct relationship with a date from an underlying instrument.
   * It may be the effective date or the maturity date but equally it may not.
   */
  @PropertyDefinition(validate = "notNull", overrideGet = true)
  private final LocalDate date;
  /**
   * The year-month of the instrument behind the curve node.
   */
  @PropertyDefinition(validate = "notNull")
  private final YearMonth yearMonth;
  /**
   * The label that describes the node, defaulted to the year-month.
   */
  @PropertyDefinition(validate = "notEmpty", overrideGet = true)
  private final String label;

  //-------------------------------------------------------------------------
  /**
   * Creates node metadata using date and year-month.
   * 
   * @param date  the date of the curve node
   * @param yearMonth  the year-month of the curve node
   * @return node metadata based on a year-month
   */
  public static YearMonthCurveNodeMetadata of(LocalDate date, YearMonth yearMonth) {
    ArgChecker.notNull(date, "date");
    ArgChecker.notNull(yearMonth, "yearMonth");
    return new YearMonthCurveNodeMetadata(date, yearMonth, yearMonth.format(FORMATTER));
  }

  /**
   * Creates node metadata using date, year-month and label.
   * 
   * @param date  the date of the curve node
   * @param yearMonth  the year-month of the curve node
   * @param label  the label to use
   * @return node metadata based on a year-month
   */
  public static YearMonthCurveNodeMetadata of(LocalDate date, YearMonth yearMonth, String label) {
    return new YearMonthCurveNodeMetadata(date, yearMonth, label);
  }

  @ImmutablePreBuild
  private static void preBuild(Builder builder) {
    if (builder.label == null && builder.yearMonth != null) {
      builder.label = builder.yearMonth.format(FORMATTER);
    }
  }

  /**
   * Gets the identifier, which is the year-month.
   *
   * @return the year-month
   */
  @Override
  public YearMonth getIdentifier() {
    return yearMonth;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code YearMonthCurveNodeMetadata}.
   * @return the meta-bean, not null
   */
  public static YearMonthCurveNodeMetadata.Meta meta() {
    return YearMonthCurveNodeMetadata.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(YearMonthCurveNodeMetadata.Meta.INSTANCE);
  }

  /**
   * The serialization version id.
   */
  private static final long serialVersionUID = 1L;

  private YearMonthCurveNodeMetadata(
      LocalDate date,
      YearMonth yearMonth,
      String label) {
    JodaBeanUtils.notNull(date, "date");
    JodaBeanUtils.notNull(yearMonth, "yearMonth");
    JodaBeanUtils.notEmpty(label, "label");
    this.date = date;
    this.yearMonth = yearMonth;
    this.label = label;
  }

  @Override
  public YearMonthCurveNodeMetadata.Meta metaBean() {
    return YearMonthCurveNodeMetadata.Meta.INSTANCE;
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
   * Gets the date of the curve node.
   * <p>
   * This is the date that the node on the curve is defined as.
   * There is not necessarily a direct relationship with a date from an underlying instrument.
   * It may be the effective date or the maturity date but equally it may not.
   * @return the value of the property, not null
   */
  @Override
  public LocalDate getDate() {
    return date;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the year-month of the instrument behind the curve node.
   * @return the value of the property, not null
   */
  public YearMonth getYearMonth() {
    return yearMonth;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the label that describes the node, defaulted to the year-month.
   * @return the value of the property, not empty
   */
  @Override
  public String getLabel() {
    return label;
  }

  //-----------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      YearMonthCurveNodeMetadata other = (YearMonthCurveNodeMetadata) obj;
      return JodaBeanUtils.equal(date, other.date) &&
          JodaBeanUtils.equal(yearMonth, other.yearMonth) &&
          JodaBeanUtils.equal(label, other.label);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(date);
    hash = hash * 31 + JodaBeanUtils.hashCode(yearMonth);
    hash = hash * 31 + JodaBeanUtils.hashCode(label);
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(128);
    buf.append("YearMonthCurveNodeMetadata{");
    buf.append("date").append('=').append(date).append(',').append(' ');
    buf.append("yearMonth").append('=').append(yearMonth).append(',').append(' ');
    buf.append("label").append('=').append(JodaBeanUtils.toString(label));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code YearMonthCurveNodeMetadata}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code date} property.
     */
    private final MetaProperty<LocalDate> date = DirectMetaProperty.ofImmutable(
        this, "date", YearMonthCurveNodeMetadata.class, LocalDate.class);
    /**
     * The meta-property for the {@code yearMonth} property.
     */
    private final MetaProperty<YearMonth> yearMonth = DirectMetaProperty.ofImmutable(
        this, "yearMonth", YearMonthCurveNodeMetadata.class, YearMonth.class);
    /**
     * The meta-property for the {@code label} property.
     */
    private final MetaProperty<String> label = DirectMetaProperty.ofImmutable(
        this, "label", YearMonthCurveNodeMetadata.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "date",
        "yearMonth",
        "label");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 3076014:  // date
          return date;
        case -496678845:  // yearMonth
          return yearMonth;
        case 102727412:  // label
          return label;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends YearMonthCurveNodeMetadata> builder() {
      return new YearMonthCurveNodeMetadata.Builder();
    }

    @Override
    public Class<? extends YearMonthCurveNodeMetadata> beanType() {
      return YearMonthCurveNodeMetadata.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code date} property.
     * @return the meta-property, not null
     */
    public MetaProperty<LocalDate> date() {
      return date;
    }

    /**
     * The meta-property for the {@code yearMonth} property.
     * @return the meta-property, not null
     */
    public MetaProperty<YearMonth> yearMonth() {
      return yearMonth;
    }

    /**
     * The meta-property for the {@code label} property.
     * @return the meta-property, not null
     */
    public MetaProperty<String> label() {
      return label;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 3076014:  // date
          return ((YearMonthCurveNodeMetadata) bean).getDate();
        case -496678845:  // yearMonth
          return ((YearMonthCurveNodeMetadata) bean).getYearMonth();
        case 102727412:  // label
          return ((YearMonthCurveNodeMetadata) bean).getLabel();
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
   * The bean-builder for {@code YearMonthCurveNodeMetadata}.
   */
  private static final class Builder extends DirectFieldsBeanBuilder<YearMonthCurveNodeMetadata> {

    private LocalDate date;
    private YearMonth yearMonth;
    private String label;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case 3076014:  // date
          return date;
        case -496678845:  // yearMonth
          return yearMonth;
        case 102727412:  // label
          return label;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 3076014:  // date
          this.date = (LocalDate) newValue;
          break;
        case -496678845:  // yearMonth
          this.yearMonth = (YearMonth) newValue;
          break;
        case 102727412:  // label
          this.label = (String) newValue;
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
    public YearMonthCurveNodeMetadata build() {
      preBuild(this);
      return new YearMonthCurveNodeMetadata(
          date,
          yearMonth,
          label);
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(128);
      buf.append("YearMonthCurveNodeMetadata.Builder{");
      buf.append("date").append('=').append(JodaBeanUtils.toString(date)).append(',').append(' ');
      buf.append("yearMonth").append('=').append(JodaBeanUtils.toString(yearMonth)).append(',').append(' ');
      buf.append("label").append('=').append(JodaBeanUtils.toString(label));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
