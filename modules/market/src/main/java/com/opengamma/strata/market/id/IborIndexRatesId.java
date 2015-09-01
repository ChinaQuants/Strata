/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.market.id;

import java.io.Serializable;
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

import com.opengamma.strata.basics.index.IborIndex;
import com.opengamma.strata.basics.market.MarketDataFeed;
import com.opengamma.strata.basics.market.MarketDataId;
import com.opengamma.strata.market.curve.CurveGroupName;
import com.opengamma.strata.market.value.IborIndexRates;

/**
 * Market data ID identifying an Ibor index, providing both historic and forward rates.
 * <p>
 * This is used when there is a need to obtain an instance of {@link IborIndexRates}
 * which provides both historic and forward rates for the specified index.
 */
@BeanDefinition(builderScope = "private")
public final class IborIndexRatesId
    implements MarketDataId<IborIndexRates>, ImmutableBean, Serializable {

  /**
   * The index that is required.
   * For example, 'GBP-LIBOR-3M'.
   */
  @PropertyDefinition(validate = "notNull")
  private final IborIndex index;
  /**
   * The name of the curve group containing the curve.
   */
  @PropertyDefinition(validate = "notNull")
  private final CurveGroupName curveGroupName;
  /**
   * The market data feed which provides quotes used to build the curve.
   */
  @PropertyDefinition(validate = "notNull")
  private final MarketDataFeed marketDataFeed;

  //-------------------------------------------------------------------------
  /**
   * Obtains an ID used to find the Ibor rates associated with an index.
   *
   * @param index  the index to find the rates for
   * @param curveGroupName  the name of the curve group containing the curve
   * @param marketDataFeed  the market data feed which provides quotes used to build the curve
   * @return an ID that identifies the Ibor rates for the specified index
   */
  public static IborIndexRatesId of(IborIndex index, CurveGroupName curveGroupName, MarketDataFeed marketDataFeed) {
    return new IborIndexRatesId(index, curveGroupName, marketDataFeed);
  }

  //-------------------------------------------------------------------------
  @Override
  public Class<IborIndexRates> getMarketDataType() {
    return IborIndexRates.class;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code IborIndexRatesId}.
   * @return the meta-bean, not null
   */
  public static IborIndexRatesId.Meta meta() {
    return IborIndexRatesId.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(IborIndexRatesId.Meta.INSTANCE);
  }

  /**
   * The serialization version id.
   */
  private static final long serialVersionUID = 1L;

  private IborIndexRatesId(
      IborIndex index,
      CurveGroupName curveGroupName,
      MarketDataFeed marketDataFeed) {
    JodaBeanUtils.notNull(index, "index");
    JodaBeanUtils.notNull(curveGroupName, "curveGroupName");
    JodaBeanUtils.notNull(marketDataFeed, "marketDataFeed");
    this.index = index;
    this.curveGroupName = curveGroupName;
    this.marketDataFeed = marketDataFeed;
  }

  @Override
  public IborIndexRatesId.Meta metaBean() {
    return IborIndexRatesId.Meta.INSTANCE;
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
   * Gets the index that is required.
   * For example, 'GBP-LIBOR-3M'.
   * @return the value of the property, not null
   */
  public IborIndex getIndex() {
    return index;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the name of the curve group containing the curve.
   * @return the value of the property, not null
   */
  public CurveGroupName getCurveGroupName() {
    return curveGroupName;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the market data feed which provides quotes used to build the curve.
   * @return the value of the property, not null
   */
  public MarketDataFeed getMarketDataFeed() {
    return marketDataFeed;
  }

  //-----------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      IborIndexRatesId other = (IborIndexRatesId) obj;
      return JodaBeanUtils.equal(getIndex(), other.getIndex()) &&
          JodaBeanUtils.equal(getCurveGroupName(), other.getCurveGroupName()) &&
          JodaBeanUtils.equal(getMarketDataFeed(), other.getMarketDataFeed());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getIndex());
    hash = hash * 31 + JodaBeanUtils.hashCode(getCurveGroupName());
    hash = hash * 31 + JodaBeanUtils.hashCode(getMarketDataFeed());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(128);
    buf.append("IborIndexRatesId{");
    buf.append("index").append('=').append(getIndex()).append(',').append(' ');
    buf.append("curveGroupName").append('=').append(getCurveGroupName()).append(',').append(' ');
    buf.append("marketDataFeed").append('=').append(JodaBeanUtils.toString(getMarketDataFeed()));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code IborIndexRatesId}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code index} property.
     */
    private final MetaProperty<IborIndex> index = DirectMetaProperty.ofImmutable(
        this, "index", IborIndexRatesId.class, IborIndex.class);
    /**
     * The meta-property for the {@code curveGroupName} property.
     */
    private final MetaProperty<CurveGroupName> curveGroupName = DirectMetaProperty.ofImmutable(
        this, "curveGroupName", IborIndexRatesId.class, CurveGroupName.class);
    /**
     * The meta-property for the {@code marketDataFeed} property.
     */
    private final MetaProperty<MarketDataFeed> marketDataFeed = DirectMetaProperty.ofImmutable(
        this, "marketDataFeed", IborIndexRatesId.class, MarketDataFeed.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "index",
        "curveGroupName",
        "marketDataFeed");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 100346066:  // index
          return index;
        case -382645893:  // curveGroupName
          return curveGroupName;
        case 842621124:  // marketDataFeed
          return marketDataFeed;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends IborIndexRatesId> builder() {
      return new IborIndexRatesId.Builder();
    }

    @Override
    public Class<? extends IborIndexRatesId> beanType() {
      return IborIndexRatesId.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code index} property.
     * @return the meta-property, not null
     */
    public MetaProperty<IborIndex> index() {
      return index;
    }

    /**
     * The meta-property for the {@code curveGroupName} property.
     * @return the meta-property, not null
     */
    public MetaProperty<CurveGroupName> curveGroupName() {
      return curveGroupName;
    }

    /**
     * The meta-property for the {@code marketDataFeed} property.
     * @return the meta-property, not null
     */
    public MetaProperty<MarketDataFeed> marketDataFeed() {
      return marketDataFeed;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 100346066:  // index
          return ((IborIndexRatesId) bean).getIndex();
        case -382645893:  // curveGroupName
          return ((IborIndexRatesId) bean).getCurveGroupName();
        case 842621124:  // marketDataFeed
          return ((IborIndexRatesId) bean).getMarketDataFeed();
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
   * The bean-builder for {@code IborIndexRatesId}.
   */
  private static final class Builder extends DirectFieldsBeanBuilder<IborIndexRatesId> {

    private IborIndex index;
    private CurveGroupName curveGroupName;
    private MarketDataFeed marketDataFeed;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case 100346066:  // index
          return index;
        case -382645893:  // curveGroupName
          return curveGroupName;
        case 842621124:  // marketDataFeed
          return marketDataFeed;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 100346066:  // index
          this.index = (IborIndex) newValue;
          break;
        case -382645893:  // curveGroupName
          this.curveGroupName = (CurveGroupName) newValue;
          break;
        case 842621124:  // marketDataFeed
          this.marketDataFeed = (MarketDataFeed) newValue;
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
    public IborIndexRatesId build() {
      return new IborIndexRatesId(
          index,
          curveGroupName,
          marketDataFeed);
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(128);
      buf.append("IborIndexRatesId.Builder{");
      buf.append("index").append('=').append(JodaBeanUtils.toString(index)).append(',').append(' ');
      buf.append("curveGroupName").append('=').append(JodaBeanUtils.toString(curveGroupName)).append(',').append(' ');
      buf.append("marketDataFeed").append('=').append(JodaBeanUtils.toString(marketDataFeed));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
