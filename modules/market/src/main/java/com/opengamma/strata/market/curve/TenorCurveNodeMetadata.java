/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.market.curve;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.strata.basics.date.Tenor;

/**
 * Curve node metadata for a curve node with a specific tenor.
 */
@BeanDefinition(builderScope = "private")
public final class TenorCurveNodeMetadata
    implements CurveParameterMetadata, ImmutableBean, Serializable {

  /**
   * The date of the curve node.
   * <p>
   * This is the date that the node on the curve is defined as.
   * There is not necessarily a direct relationship with a date from an underlying instrument.
   * It may be the effective date or the maturity date but equally it may not.
   */
  @PropertyDefinition(validate = "notNull")
  private final LocalDate date;
  /**
   * The tenor of the instrument behind the curve node.
   */
  @PropertyDefinition(validate = "notNull")
  private final Tenor tenor;

  /**
   * Returns node metadata described by a tenor.
   * 
   * @param date  the date of the curve node
   * @param tenor  the tenor of the curve node
   * @return a curve node ID for the tenor
   */
  public static TenorCurveNodeMetadata of(LocalDate date, Tenor tenor) {
    return new TenorCurveNodeMetadata(date, tenor);
  }

  @Override
  public String getDescription() {
    return tenor.getPeriod().toString().substring(1);
  }

  /**
   * Returns the {@link #getTenor() tenor} of the node.
   *
   * @return the {@link #getTenor() tenor} of the node
   */
  @Override
  public Tenor getIdentifier() {
    return tenor;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code TenorCurveNodeMetadata}.
   * @return the meta-bean, not null
   */
  public static TenorCurveNodeMetadata.Meta meta() {
    return TenorCurveNodeMetadata.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(TenorCurveNodeMetadata.Meta.INSTANCE);
  }

  /**
   * The serialization version id.
   */
  private static final long serialVersionUID = 1L;

  private TenorCurveNodeMetadata(
      LocalDate date,
      Tenor tenor) {
    JodaBeanUtils.notNull(date, "date");
    JodaBeanUtils.notNull(tenor, "tenor");
    this.date = date;
    this.tenor = tenor;
  }

  @Override
  public TenorCurveNodeMetadata.Meta metaBean() {
    return TenorCurveNodeMetadata.Meta.INSTANCE;
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
  public LocalDate getDate() {
    return date;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the tenor of the instrument behind the curve node.
   * @return the value of the property, not null
   */
  public Tenor getTenor() {
    return tenor;
  }

  //-----------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      TenorCurveNodeMetadata other = (TenorCurveNodeMetadata) obj;
      return JodaBeanUtils.equal(getDate(), other.getDate()) &&
          JodaBeanUtils.equal(getTenor(), other.getTenor());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getDate());
    hash = hash * 31 + JodaBeanUtils.hashCode(getTenor());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(96);
    buf.append("TenorCurveNodeMetadata{");
    buf.append("date").append('=').append(getDate()).append(',').append(' ');
    buf.append("tenor").append('=').append(JodaBeanUtils.toString(getTenor()));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code TenorCurveNodeMetadata}.
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
        this, "date", TenorCurveNodeMetadata.class, LocalDate.class);
    /**
     * The meta-property for the {@code tenor} property.
     */
    private final MetaProperty<Tenor> tenor = DirectMetaProperty.ofImmutable(
        this, "tenor", TenorCurveNodeMetadata.class, Tenor.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "date",
        "tenor");

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
        case 110246592:  // tenor
          return tenor;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends TenorCurveNodeMetadata> builder() {
      return new TenorCurveNodeMetadata.Builder();
    }

    @Override
    public Class<? extends TenorCurveNodeMetadata> beanType() {
      return TenorCurveNodeMetadata.class;
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
     * The meta-property for the {@code tenor} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Tenor> tenor() {
      return tenor;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 3076014:  // date
          return ((TenorCurveNodeMetadata) bean).getDate();
        case 110246592:  // tenor
          return ((TenorCurveNodeMetadata) bean).getTenor();
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
   * The bean-builder for {@code TenorCurveNodeMetadata}.
   */
  private static final class Builder extends DirectFieldsBeanBuilder<TenorCurveNodeMetadata> {

    private LocalDate date;
    private Tenor tenor;

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
        case 110246592:  // tenor
          return tenor;
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
        case 110246592:  // tenor
          this.tenor = (Tenor) newValue;
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
    public TenorCurveNodeMetadata build() {
      return new TenorCurveNodeMetadata(
          date,
          tenor);
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(96);
      buf.append("TenorCurveNodeMetadata.Builder{");
      buf.append("date").append('=').append(JodaBeanUtils.toString(date)).append(',').append(' ');
      buf.append("tenor").append('=').append(JodaBeanUtils.toString(tenor));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
