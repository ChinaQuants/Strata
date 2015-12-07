/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.calc.runner.function.result;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Stream;

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
import org.joda.beans.BeanBuilder;

/**
 * A container for multiple results produced by performing a single calculation across multiple scenarios.
 * <p>
 * The results can be any type and the engine will not attempt to automatically convert the currency to
 * the reporting currency.
 * <p>
 * The number of results is required to be the same as the number of scenarios in the market data
 * provided to the function.
 * 
 * @param <T>  the type of the result
 */
@BeanDefinition(builderScope = "private")
public final class DefaultScenarioResult<T> implements ScenarioResult<T>, ImmutableBean, Serializable {

  /** The individual results. */
  @PropertyDefinition(validate = "notNull")
  private final ImmutableList<T> results;

  /**
   * Returns a set of scenario results containing the specified individual results.
   *
   * @param results  the individual results
   * @return a set of scenario results containing the specified individual results
   */
  public static <T> DefaultScenarioResult<T> of(List<T> results) {
    return new DefaultScenarioResult<>(results);
  }

  /**
   * Returns a set of scenario results containing the specified individual results.
   *
   * @param results  the individual results
   * @return a set of scenario results containing the specified individual results
   */
  @SafeVarargs
  public static <T> DefaultScenarioResult<T> of(T... results) {
    return new DefaultScenarioResult<>(ImmutableList.copyOf(results));
  }

  @Override
  public int size() {
    return results.size();
  }

  @Override
  public T get(int index) {
    return results.get(index);
  }

  @Override
  public Stream<T> stream() {
    return results.stream();
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code DefaultScenarioResult}.
   * @return the meta-bean, not null
   */
  @SuppressWarnings("rawtypes")
  public static DefaultScenarioResult.Meta meta() {
    return DefaultScenarioResult.Meta.INSTANCE;
  }

  /**
   * The meta-bean for {@code DefaultScenarioResult}.
   * @param <R>  the bean's generic type
   * @param cls  the bean's generic type
   * @return the meta-bean, not null
   */
  @SuppressWarnings("unchecked")
  public static <R> DefaultScenarioResult.Meta<R> metaDefaultScenarioResult(Class<R> cls) {
    return DefaultScenarioResult.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(DefaultScenarioResult.Meta.INSTANCE);
  }

  /**
   * The serialization version id.
   */
  private static final long serialVersionUID = 1L;

  private DefaultScenarioResult(
      List<T> results) {
    JodaBeanUtils.notNull(results, "results");
    this.results = ImmutableList.copyOf(results);
  }

  @SuppressWarnings("unchecked")
  @Override
  public DefaultScenarioResult.Meta<T> metaBean() {
    return DefaultScenarioResult.Meta.INSTANCE;
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
   * Gets the individual results.
   * @return the value of the property, not null
   */
  public ImmutableList<T> getResults() {
    return results;
  }

  //-----------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      DefaultScenarioResult<?> other = (DefaultScenarioResult<?>) obj;
      return JodaBeanUtils.equal(results, other.results);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(results);
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(64);
    buf.append("DefaultScenarioResult{");
    buf.append("results").append('=').append(JodaBeanUtils.toString(results));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code DefaultScenarioResult}.
   * @param <T>  the type
   */
  public static final class Meta<T> extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    @SuppressWarnings("rawtypes")
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code results} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<ImmutableList<T>> results = DirectMetaProperty.ofImmutable(
        this, "results", DefaultScenarioResult.class, (Class) ImmutableList.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "results");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 1097546742:  // results
          return results;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends DefaultScenarioResult<T>> builder() {
      return new DefaultScenarioResult.Builder<T>();
    }

    @SuppressWarnings({"unchecked", "rawtypes" })
    @Override
    public Class<? extends DefaultScenarioResult<T>> beanType() {
      return (Class) DefaultScenarioResult.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code results} property.
     * @return the meta-property, not null
     */
    public MetaProperty<ImmutableList<T>> results() {
      return results;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1097546742:  // results
          return ((DefaultScenarioResult<?>) bean).getResults();
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
   * The bean-builder for {@code DefaultScenarioResult}.
   * @param <T>  the type
   */
  private static final class Builder<T> extends DirectFieldsBeanBuilder<DefaultScenarioResult<T>> {

    private List<T> results = ImmutableList.of();

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case 1097546742:  // results
          return results;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Builder<T> set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 1097546742:  // results
          this.results = (List<T>) newValue;
          break;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
      return this;
    }

    @Override
    public Builder<T> set(MetaProperty<?> property, Object value) {
      super.set(property, value);
      return this;
    }

    @Override
    public Builder<T> setString(String propertyName, String value) {
      setString(meta().metaProperty(propertyName), value);
      return this;
    }

    @Override
    public Builder<T> setString(MetaProperty<?> property, String value) {
      super.setString(property, value);
      return this;
    }

    @Override
    public Builder<T> setAll(Map<String, ? extends Object> propertyValueMap) {
      super.setAll(propertyValueMap);
      return this;
    }

    @Override
    public DefaultScenarioResult<T> build() {
      return new DefaultScenarioResult<T>(
          results);
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(64);
      buf.append("DefaultScenarioResult.Builder{");
      buf.append("results").append('=').append(JodaBeanUtils.toString(results));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
