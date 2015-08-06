/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.engine.config;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
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

import com.google.common.collect.ImmutableList;
import com.opengamma.strata.basics.CalculationTarget;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.Guavate;

/**
 * A reporting currency rule that delegates to multiple underlying rules, returning the first currency it finds.
 */
@BeanDefinition
final class CompositeReportingRules implements ReportingRules, ImmutableBean {

  /** The delegate rules. */
  @PropertyDefinition(validate = "notNull")
  private final ImmutableList<ReportingRules> rules;

  @Override
  public ReportingRules composedWith(ReportingRules rule) {
    List<ReportingRules> newRules = ImmutableList.<ReportingRules>builder().addAll(rules).add(rule).build();
    return new CompositeReportingRules(newRules);
  }

  @Override
  public Optional<Currency> reportingCurrency(CalculationTarget target) {
    return rules.stream()
        .map(rule -> rule.reportingCurrency(target))
        .flatMap(Guavate::stream)
        .findFirst();
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code CompositeReportingRules}.
   * @return the meta-bean, not null
   */
  public static CompositeReportingRules.Meta meta() {
    return CompositeReportingRules.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(CompositeReportingRules.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static CompositeReportingRules.Builder builder() {
    return new CompositeReportingRules.Builder();
  }

  private CompositeReportingRules(
      List<ReportingRules> rules) {
    JodaBeanUtils.notNull(rules, "rules");
    this.rules = ImmutableList.copyOf(rules);
  }

  @Override
  public CompositeReportingRules.Meta metaBean() {
    return CompositeReportingRules.Meta.INSTANCE;
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
   * Gets the delegate rules.
   * @return the value of the property, not null
   */
  public ImmutableList<ReportingRules> getRules() {
    return rules;
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
      CompositeReportingRules other = (CompositeReportingRules) obj;
      return JodaBeanUtils.equal(getRules(), other.getRules());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getRules());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(64);
    buf.append("CompositeReportingRules{");
    buf.append("rules").append('=').append(JodaBeanUtils.toString(getRules()));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code CompositeReportingRules}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code rules} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<ImmutableList<ReportingRules>> rules = DirectMetaProperty.ofImmutable(
        this, "rules", CompositeReportingRules.class, (Class) ImmutableList.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "rules");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 108873975:  // rules
          return rules;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public CompositeReportingRules.Builder builder() {
      return new CompositeReportingRules.Builder();
    }

    @Override
    public Class<? extends CompositeReportingRules> beanType() {
      return CompositeReportingRules.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code rules} property.
     * @return the meta-property, not null
     */
    public MetaProperty<ImmutableList<ReportingRules>> rules() {
      return rules;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 108873975:  // rules
          return ((CompositeReportingRules) bean).getRules();
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
   * The bean-builder for {@code CompositeReportingRules}.
   */
  public static final class Builder extends DirectFieldsBeanBuilder<CompositeReportingRules> {

    private List<ReportingRules> rules = ImmutableList.of();

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    private Builder(CompositeReportingRules beanToCopy) {
      this.rules = beanToCopy.getRules();
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case 108873975:  // rules
          return rules;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 108873975:  // rules
          this.rules = (List<ReportingRules>) newValue;
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
    public CompositeReportingRules build() {
      return new CompositeReportingRules(
          rules);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the delegate rules.
     * @param rules  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder rules(List<ReportingRules> rules) {
      JodaBeanUtils.notNull(rules, "rules");
      this.rules = rules;
      return this;
    }

    /**
     * Sets the {@code rules} property in the builder
     * from an array of objects.
     * @param rules  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder rules(ReportingRules... rules) {
      return rules(ImmutableList.copyOf(rules));
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(64);
      buf.append("CompositeReportingRules.Builder{");
      buf.append("rules").append('=').append(JodaBeanUtils.toString(rules));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
