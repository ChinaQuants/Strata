/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.market.curve.definition;

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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.index.IborIndex;
import com.opengamma.strata.basics.index.OvernightIndex;

/**
 * A single entry in the curve group definition.
 * <p>
 * Each entry stores the definition of a single curve and how it is to be used.
 * This structure allows the curve itself to be used for multiple purposes.
 * <p>
 * In the simple case a curve is only used for a single purpose.
 * For example, if a curve is used for discounting it will have one key of type {@code DiscountCurveKey}.
 * <p>
 * A single curve can also be used as both a discounting curve and a forward curve.
 * In that case its key set would contain a {@code DiscountCurveKey} and a {@code RateIndexCurveKey}.
 * <p>
 * Every curve must be associated with at least once key.
 */
@BeanDefinition
public final class CurveGroupEntry
    implements ImmutableBean, Serializable {

  /**
   * The curve definition.
   */
  @PropertyDefinition(validate = "notNull")
  private final NodalCurveDefinition curveDefinition;
  /**
   * The currencies for which the curve provides discount rates.
   * This is empty if the curve is not used for Ibor rates.
   */
  @PropertyDefinition(validate = "notNull")
  private final ImmutableSet<Currency> discountCurrencies;
  /**
   * The Ibor indices for which the curve provides forward rates.
   * This is empty if the curve is not used for Ibor rates.
   */
  @PropertyDefinition(validate = "notNull")
  private final ImmutableSet<IborIndex> iborIndices;
  /**
   * The Overnight indices for which the curve provides forward rates.
   * This is empty if the curve is not used for Overnight rates.
   */
  @PropertyDefinition(validate = "notNull")
  private final ImmutableSet<OvernightIndex> overnightIndices;

  //-------------------------------------------------------------------------
  /**
   * Merges the specified entry with this entry, returning a new entry.
   * <p>
   * The two entries must have the same curve definition.
   * 
   * @param newEntry  the new entry
   * @return the merged entry
   */
  CurveGroupEntry merge(CurveGroupEntry newEntry) {
    return CurveGroupEntry.builder()
        .curveDefinition(curveDefinition)
        .discountCurrencies(Sets.union(discountCurrencies, newEntry.discountCurrencies))
        .iborIndices(Sets.union(iborIndices, newEntry.iborIndices))
        .overnightIndices(Sets.union(overnightIndices, newEntry.overnightIndices))
        .build();
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code CurveGroupEntry}.
   * @return the meta-bean, not null
   */
  public static CurveGroupEntry.Meta meta() {
    return CurveGroupEntry.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(CurveGroupEntry.Meta.INSTANCE);
  }

  /**
   * The serialization version id.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static CurveGroupEntry.Builder builder() {
    return new CurveGroupEntry.Builder();
  }

  private CurveGroupEntry(
      NodalCurveDefinition curveDefinition,
      Set<Currency> discountCurrencies,
      Set<IborIndex> iborIndices,
      Set<OvernightIndex> overnightIndices) {
    JodaBeanUtils.notNull(curveDefinition, "curveDefinition");
    JodaBeanUtils.notNull(discountCurrencies, "discountCurrencies");
    JodaBeanUtils.notNull(iborIndices, "iborIndices");
    JodaBeanUtils.notNull(overnightIndices, "overnightIndices");
    this.curveDefinition = curveDefinition;
    this.discountCurrencies = ImmutableSet.copyOf(discountCurrencies);
    this.iborIndices = ImmutableSet.copyOf(iborIndices);
    this.overnightIndices = ImmutableSet.copyOf(overnightIndices);
  }

  @Override
  public CurveGroupEntry.Meta metaBean() {
    return CurveGroupEntry.Meta.INSTANCE;
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
   * Gets the curve definition.
   * @return the value of the property, not null
   */
  public NodalCurveDefinition getCurveDefinition() {
    return curveDefinition;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the currencies for which the curve provides discount rates.
   * This is empty if the curve is not used for Ibor rates.
   * @return the value of the property, not null
   */
  public ImmutableSet<Currency> getDiscountCurrencies() {
    return discountCurrencies;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the Ibor indices for which the curve provides forward rates.
   * This is empty if the curve is not used for Ibor rates.
   * @return the value of the property, not null
   */
  public ImmutableSet<IborIndex> getIborIndices() {
    return iborIndices;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the Overnight indices for which the curve provides forward rates.
   * This is empty if the curve is not used for Overnight rates.
   * @return the value of the property, not null
   */
  public ImmutableSet<OvernightIndex> getOvernightIndices() {
    return overnightIndices;
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
      CurveGroupEntry other = (CurveGroupEntry) obj;
      return JodaBeanUtils.equal(curveDefinition, other.curveDefinition) &&
          JodaBeanUtils.equal(discountCurrencies, other.discountCurrencies) &&
          JodaBeanUtils.equal(iborIndices, other.iborIndices) &&
          JodaBeanUtils.equal(overnightIndices, other.overnightIndices);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(curveDefinition);
    hash = hash * 31 + JodaBeanUtils.hashCode(discountCurrencies);
    hash = hash * 31 + JodaBeanUtils.hashCode(iborIndices);
    hash = hash * 31 + JodaBeanUtils.hashCode(overnightIndices);
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(160);
    buf.append("CurveGroupEntry{");
    buf.append("curveDefinition").append('=').append(curveDefinition).append(',').append(' ');
    buf.append("discountCurrencies").append('=').append(discountCurrencies).append(',').append(' ');
    buf.append("iborIndices").append('=').append(iborIndices).append(',').append(' ');
    buf.append("overnightIndices").append('=').append(JodaBeanUtils.toString(overnightIndices));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code CurveGroupEntry}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code curveDefinition} property.
     */
    private final MetaProperty<NodalCurveDefinition> curveDefinition = DirectMetaProperty.ofImmutable(
        this, "curveDefinition", CurveGroupEntry.class, NodalCurveDefinition.class);
    /**
     * The meta-property for the {@code discountCurrencies} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<ImmutableSet<Currency>> discountCurrencies = DirectMetaProperty.ofImmutable(
        this, "discountCurrencies", CurveGroupEntry.class, (Class) ImmutableSet.class);
    /**
     * The meta-property for the {@code iborIndices} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<ImmutableSet<IborIndex>> iborIndices = DirectMetaProperty.ofImmutable(
        this, "iborIndices", CurveGroupEntry.class, (Class) ImmutableSet.class);
    /**
     * The meta-property for the {@code overnightIndices} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<ImmutableSet<OvernightIndex>> overnightIndices = DirectMetaProperty.ofImmutable(
        this, "overnightIndices", CurveGroupEntry.class, (Class) ImmutableSet.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "curveDefinition",
        "discountCurrencies",
        "iborIndices",
        "overnightIndices");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1257770078:  // curveDefinition
          return curveDefinition;
        case -538086256:  // discountCurrencies
          return discountCurrencies;
        case -118808757:  // iborIndices
          return iborIndices;
        case 1523471171:  // overnightIndices
          return overnightIndices;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public CurveGroupEntry.Builder builder() {
      return new CurveGroupEntry.Builder();
    }

    @Override
    public Class<? extends CurveGroupEntry> beanType() {
      return CurveGroupEntry.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code curveDefinition} property.
     * @return the meta-property, not null
     */
    public MetaProperty<NodalCurveDefinition> curveDefinition() {
      return curveDefinition;
    }

    /**
     * The meta-property for the {@code discountCurrencies} property.
     * @return the meta-property, not null
     */
    public MetaProperty<ImmutableSet<Currency>> discountCurrencies() {
      return discountCurrencies;
    }

    /**
     * The meta-property for the {@code iborIndices} property.
     * @return the meta-property, not null
     */
    public MetaProperty<ImmutableSet<IborIndex>> iborIndices() {
      return iborIndices;
    }

    /**
     * The meta-property for the {@code overnightIndices} property.
     * @return the meta-property, not null
     */
    public MetaProperty<ImmutableSet<OvernightIndex>> overnightIndices() {
      return overnightIndices;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1257770078:  // curveDefinition
          return ((CurveGroupEntry) bean).getCurveDefinition();
        case -538086256:  // discountCurrencies
          return ((CurveGroupEntry) bean).getDiscountCurrencies();
        case -118808757:  // iborIndices
          return ((CurveGroupEntry) bean).getIborIndices();
        case 1523471171:  // overnightIndices
          return ((CurveGroupEntry) bean).getOvernightIndices();
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
   * The bean-builder for {@code CurveGroupEntry}.
   */
  public static final class Builder extends DirectFieldsBeanBuilder<CurveGroupEntry> {

    private NodalCurveDefinition curveDefinition;
    private Set<Currency> discountCurrencies = ImmutableSet.of();
    private Set<IborIndex> iborIndices = ImmutableSet.of();
    private Set<OvernightIndex> overnightIndices = ImmutableSet.of();

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    private Builder(CurveGroupEntry beanToCopy) {
      this.curveDefinition = beanToCopy.getCurveDefinition();
      this.discountCurrencies = beanToCopy.getDiscountCurrencies();
      this.iborIndices = beanToCopy.getIborIndices();
      this.overnightIndices = beanToCopy.getOvernightIndices();
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1257770078:  // curveDefinition
          return curveDefinition;
        case -538086256:  // discountCurrencies
          return discountCurrencies;
        case -118808757:  // iborIndices
          return iborIndices;
        case 1523471171:  // overnightIndices
          return overnightIndices;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case -1257770078:  // curveDefinition
          this.curveDefinition = (NodalCurveDefinition) newValue;
          break;
        case -538086256:  // discountCurrencies
          this.discountCurrencies = (Set<Currency>) newValue;
          break;
        case -118808757:  // iborIndices
          this.iborIndices = (Set<IborIndex>) newValue;
          break;
        case 1523471171:  // overnightIndices
          this.overnightIndices = (Set<OvernightIndex>) newValue;
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
    public CurveGroupEntry build() {
      return new CurveGroupEntry(
          curveDefinition,
          discountCurrencies,
          iborIndices,
          overnightIndices);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the curve definition.
     * @param curveDefinition  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder curveDefinition(NodalCurveDefinition curveDefinition) {
      JodaBeanUtils.notNull(curveDefinition, "curveDefinition");
      this.curveDefinition = curveDefinition;
      return this;
    }

    /**
     * Sets the currencies for which the curve provides discount rates.
     * This is empty if the curve is not used for Ibor rates.
     * @param discountCurrencies  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder discountCurrencies(Set<Currency> discountCurrencies) {
      JodaBeanUtils.notNull(discountCurrencies, "discountCurrencies");
      this.discountCurrencies = discountCurrencies;
      return this;
    }

    /**
     * Sets the {@code discountCurrencies} property in the builder
     * from an array of objects.
     * @param discountCurrencies  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder discountCurrencies(Currency... discountCurrencies) {
      return discountCurrencies(ImmutableSet.copyOf(discountCurrencies));
    }

    /**
     * Sets the Ibor indices for which the curve provides forward rates.
     * This is empty if the curve is not used for Ibor rates.
     * @param iborIndices  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder iborIndices(Set<IborIndex> iborIndices) {
      JodaBeanUtils.notNull(iborIndices, "iborIndices");
      this.iborIndices = iborIndices;
      return this;
    }

    /**
     * Sets the {@code iborIndices} property in the builder
     * from an array of objects.
     * @param iborIndices  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder iborIndices(IborIndex... iborIndices) {
      return iborIndices(ImmutableSet.copyOf(iborIndices));
    }

    /**
     * Sets the Overnight indices for which the curve provides forward rates.
     * This is empty if the curve is not used for Overnight rates.
     * @param overnightIndices  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder overnightIndices(Set<OvernightIndex> overnightIndices) {
      JodaBeanUtils.notNull(overnightIndices, "overnightIndices");
      this.overnightIndices = overnightIndices;
      return this;
    }

    /**
     * Sets the {@code overnightIndices} property in the builder
     * from an array of objects.
     * @param overnightIndices  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder overnightIndices(OvernightIndex... overnightIndices) {
      return overnightIndices(ImmutableSet.copyOf(overnightIndices));
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(160);
      buf.append("CurveGroupEntry.Builder{");
      buf.append("curveDefinition").append('=').append(JodaBeanUtils.toString(curveDefinition)).append(',').append(' ');
      buf.append("discountCurrencies").append('=').append(JodaBeanUtils.toString(discountCurrencies)).append(',').append(' ');
      buf.append("iborIndices").append('=').append(JodaBeanUtils.toString(iborIndices)).append(',').append(' ');
      buf.append("overnightIndices").append('=').append(JodaBeanUtils.toString(overnightIndices));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
