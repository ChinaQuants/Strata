/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.finance.credit;

import java.io.Serializable;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.joda.beans.Bean;
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

/**
 * The fee leg of a credit default swap (CDS).
 * <p>
 * A CDS has two legs, a fee leg and a protection leg.
 * The fee leg defines a regular schedule of payments that are made in exchange for the protection.
 */
@BeanDefinition
public final class FeeLeg
    implements ImmutableBean, Serializable {
  // TODO: consider moving fee up to Cds and flattening PeriodicPayments

  /**
   * The upfront fee.
   * <p>
   * This specifies a single amount payable by the buyer to the seller
   * This amount occurs on the specified date and is considered to be a fee separate from the regular payments.
   */
  @PropertyDefinition(validate = "notNull")
  final SinglePayment upfrontFee;
  /**
   * The periodic schedule of payments.
   * <p>
   * This specifies a periodic schedule of fixed amounts that are payable by the buyer to the seller
   * on the fixed rate payer payment dates. The fixed amount to be paid on each payment date can be
   * specified in terms of a known currency amount or as an amount calculated on a formula basis
   * by reference to a per annum fixed rate. The applicable business day convention and business
   * day for adjusting any fixed rate payer payment date if it would otherwise fall on a day that
   * is not a business day are those specified in the dateAdjustments element within the
   * generalTerms component.
   */
  @PropertyDefinition(validate = "notNull")
  final PeriodicPayments periodicPayments;

  //-------------------------------------------------------------------------
  /**
   * Creates a fee leg from the fee and payments.
   * 
   * @param upfrontFee  the upfront fee
   * @param periodicPayments  the payments
   * @return the leg
   */
  public static FeeLeg of(SinglePayment upfrontFee, PeriodicPayments periodicPayments) {
    return new FeeLeg(upfrontFee, periodicPayments);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code FeeLeg}.
   * @return the meta-bean, not null
   */
  public static FeeLeg.Meta meta() {
    return FeeLeg.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(FeeLeg.Meta.INSTANCE);
  }

  /**
   * The serialization version id.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static FeeLeg.Builder builder() {
    return new FeeLeg.Builder();
  }

  private FeeLeg(
      SinglePayment upfrontFee,
      PeriodicPayments periodicPayments) {
    JodaBeanUtils.notNull(upfrontFee, "upfrontFee");
    JodaBeanUtils.notNull(periodicPayments, "periodicPayments");
    this.upfrontFee = upfrontFee;
    this.periodicPayments = periodicPayments;
  }

  @Override
  public FeeLeg.Meta metaBean() {
    return FeeLeg.Meta.INSTANCE;
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
   * Gets the upfront fee.
   * <p>
   * This specifies a single amount payable by the buyer to the seller
   * This amount occurs on the specified date and is considered to be a fee separate from the regular payments.
   * @return the value of the property, not null
   */
  public SinglePayment getUpfrontFee() {
    return upfrontFee;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the periodic schedule of payments.
   * <p>
   * This specifies a periodic schedule of fixed amounts that are payable by the buyer to the seller
   * on the fixed rate payer payment dates. The fixed amount to be paid on each payment date can be
   * specified in terms of a known currency amount or as an amount calculated on a formula basis
   * by reference to a per annum fixed rate. The applicable business day convention and business
   * day for adjusting any fixed rate payer payment date if it would otherwise fall on a day that
   * is not a business day are those specified in the dateAdjustments element within the
   * generalTerms component.
   * @return the value of the property, not null
   */
  public PeriodicPayments getPeriodicPayments() {
    return periodicPayments;
  }

  //-----------------------------------------------------------------------
  /**
   * Returns a builder that allows this bean to be mutated.
   * @return the mutable builder, not null
   */
  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      FeeLeg other = (FeeLeg) obj;
      return JodaBeanUtils.equal(getUpfrontFee(), other.getUpfrontFee()) &&
          JodaBeanUtils.equal(getPeriodicPayments(), other.getPeriodicPayments());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getUpfrontFee());
    hash = hash * 31 + JodaBeanUtils.hashCode(getPeriodicPayments());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(96);
    buf.append("FeeLeg{");
    buf.append("upfrontFee").append('=').append(getUpfrontFee()).append(',').append(' ');
    buf.append("periodicPayments").append('=').append(JodaBeanUtils.toString(getPeriodicPayments()));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code FeeLeg}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code upfrontFee} property.
     */
    private final MetaProperty<SinglePayment> upfrontFee = DirectMetaProperty.ofImmutable(
        this, "upfrontFee", FeeLeg.class, SinglePayment.class);
    /**
     * The meta-property for the {@code periodicPayments} property.
     */
    private final MetaProperty<PeriodicPayments> periodicPayments = DirectMetaProperty.ofImmutable(
        this, "periodicPayments", FeeLeg.class, PeriodicPayments.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "upfrontFee",
        "periodicPayments");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 963468344:  // upfrontFee
          return upfrontFee;
        case -367345944:  // periodicPayments
          return periodicPayments;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public FeeLeg.Builder builder() {
      return new FeeLeg.Builder();
    }

    @Override
    public Class<? extends FeeLeg> beanType() {
      return FeeLeg.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code upfrontFee} property.
     * @return the meta-property, not null
     */
    public MetaProperty<SinglePayment> upfrontFee() {
      return upfrontFee;
    }

    /**
     * The meta-property for the {@code periodicPayments} property.
     * @return the meta-property, not null
     */
    public MetaProperty<PeriodicPayments> periodicPayments() {
      return periodicPayments;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 963468344:  // upfrontFee
          return ((FeeLeg) bean).getUpfrontFee();
        case -367345944:  // periodicPayments
          return ((FeeLeg) bean).getPeriodicPayments();
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
   * The bean-builder for {@code FeeLeg}.
   */
  public static final class Builder extends DirectFieldsBeanBuilder<FeeLeg> {

    private SinglePayment upfrontFee;
    private PeriodicPayments periodicPayments;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    private Builder(FeeLeg beanToCopy) {
      this.upfrontFee = beanToCopy.getUpfrontFee();
      this.periodicPayments = beanToCopy.getPeriodicPayments();
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case 963468344:  // upfrontFee
          return upfrontFee;
        case -367345944:  // periodicPayments
          return periodicPayments;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 963468344:  // upfrontFee
          this.upfrontFee = (SinglePayment) newValue;
          break;
        case -367345944:  // periodicPayments
          this.periodicPayments = (PeriodicPayments) newValue;
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
    public FeeLeg build() {
      return new FeeLeg(
          upfrontFee,
          periodicPayments);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the upfront fee.
     * <p>
     * This specifies a single amount payable by the buyer to the seller
     * This amount occurs on the specified date and is considered to be a fee separate from the regular payments.
     * @param upfrontFee  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder upfrontFee(SinglePayment upfrontFee) {
      JodaBeanUtils.notNull(upfrontFee, "upfrontFee");
      this.upfrontFee = upfrontFee;
      return this;
    }

    /**
     * Sets the periodic schedule of payments.
     * <p>
     * This specifies a periodic schedule of fixed amounts that are payable by the buyer to the seller
     * on the fixed rate payer payment dates. The fixed amount to be paid on each payment date can be
     * specified in terms of a known currency amount or as an amount calculated on a formula basis
     * by reference to a per annum fixed rate. The applicable business day convention and business
     * day for adjusting any fixed rate payer payment date if it would otherwise fall on a day that
     * is not a business day are those specified in the dateAdjustments element within the
     * generalTerms component.
     * @param periodicPayments  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder periodicPayments(PeriodicPayments periodicPayments) {
      JodaBeanUtils.notNull(periodicPayments, "periodicPayments");
      this.periodicPayments = periodicPayments;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(96);
      buf.append("FeeLeg.Builder{");
      buf.append("upfrontFee").append('=').append(JodaBeanUtils.toString(upfrontFee)).append(',').append(' ');
      buf.append("periodicPayments").append('=').append(JodaBeanUtils.toString(periodicPayments));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
