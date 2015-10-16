/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.math.impl.util;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunctionLagrangeForm;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.optim.PointValuePair;
import org.testng.annotations.Test;

import com.opengamma.strata.math.impl.ComplexNumber;
import com.opengamma.strata.math.impl.function.Function1D;
import com.opengamma.strata.math.impl.function.FunctionND;
import com.opengamma.strata.math.impl.function.RealPolynomialFunction1D;
import com.opengamma.strata.math.impl.matrix.DoubleMatrix1D;
import com.opengamma.strata.math.impl.matrix.DoubleMatrix2D;

/**
 * Test {@link CommonsMathWrapper}.
 */
@Test
public class CommonsMathWrapperTest {

  private static final DoubleMatrix1D OG_VECTOR = DoubleMatrix1D.of(1, 2, 3);
  private static final DoubleMatrix2D OG_MATRIX = DoubleMatrix2D.copyOf(
      new double[][] { {1, 2, 3}, {4, 5, 6}, {7, 8, 9}});
  private static final Function1D<Double, Double> OG_FUNCTION_1D = new Function1D<Double, Double>() {
    @Override
    public Double evaluate(final Double x) {
      return x * x + 7 * x + 12;
    }

  };
  private static final ComplexNumber OG_COMPLEX = new ComplexNumber(1, 2);
  private static final FunctionND<Double, Double> OG_FUNCTION_ND = new FunctionND<Double, Double>() {

    @Override
    protected Double evaluateFunction(final Double[] x) {
      return x[0] * x[0] + 2 * x[1] - 3 * x[2] + x[3];
    }
  };
  private static final RealPolynomialFunction1D OG_POLYNOMIAL =
      new RealPolynomialFunction1D(new double[] {3, 4, -1, 5, -3});

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull1DMatrix() {
    CommonsMathWrapper.wrap((DoubleMatrix1D) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVector() {
    CommonsMathWrapper.unwrap((RealVector) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull1DFunction() {
    CommonsMathWrapper.wrapUnivariate((Function1D<Double, Double>) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullNDFunction() {
    CommonsMathWrapper.wrap((FunctionND<Double, Double>) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMatrix() {
    CommonsMathWrapper.wrap((DoubleMatrix2D) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRealMatrix() {
    CommonsMathWrapper.unwrap((RealMatrix) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullComplexNumber() {
    CommonsMathWrapper.wrap((ComplexNumber) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullLagrange() {
    CommonsMathWrapper.unwrap((PolynomialFunctionLagrangeForm) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPointVectorPair() {
    CommonsMathWrapper.unwrap((PointValuePair) null);
  }

  @Test
  public void testVector() {
    RealVector commons = CommonsMathWrapper.wrap(OG_VECTOR);
    assertEquals(CommonsMathWrapper.unwrap(commons), OG_VECTOR);
  }

  @Test
  public void testVectorAsMatrix() {
    RealMatrix commons = CommonsMathWrapper.wrapAsMatrix(OG_VECTOR);
    double[][] data = commons.getData();
    assertEquals(data.length, OG_VECTOR.size());
    assertEquals(data[0].length, 1);
  }

  @Test
  public void test1DFunction() {
    UnivariateFunction commons = CommonsMathWrapper.wrapUnivariate(OG_FUNCTION_1D);
    for (int i = 0; i < 100; i++) {
      assertEquals(OG_FUNCTION_1D.evaluate((double) i), commons.value(i), 1e-15);
    }
  }

  @Test
  public void testNDFunction() {
    Double[] x1 = new Double[4];
    double[] x2 = new double[4];
    MultivariateFunction commons = CommonsMathWrapper.wrap(OG_FUNCTION_ND);
    for (int i = 0; i < 100; i++) {
      for (int j = 0; j < 4; j++) {
        x1[j] = (double) i;
        x2[j] = x1[j];
      }
      assertEquals(OG_FUNCTION_ND.evaluate(x1), commons.value(x2), 1e-15);
    }
  }

  @Test
  public void testMatrix() {
    RealMatrix commons = CommonsMathWrapper.wrap(OG_MATRIX);
    double[][] unwrapped = CommonsMathWrapper.unwrap(commons).toArray();
    double[][] ogData = OG_MATRIX.toArray();
    int n = unwrapped.length;
    assertEquals(n, ogData.length);
    for (int i = 0; i < n; i++) {
      assertArrayEquals(unwrapped[i], ogData[i], 1e-15);
    }
  }

  @Test
  public void testComplexNumber() {
    Complex commons = CommonsMathWrapper.wrap(OG_COMPLEX);
    assertEquals(commons.getReal(), OG_COMPLEX.getReal(), 0);
    assertEquals(commons.getImaginary(), OG_COMPLEX.getImaginary(), 0);
  }

  @Test
  public void testLagrange() {
    int n = OG_POLYNOMIAL.getCoefficients().length;
    double[] x = new double[n];
    double[] y = new double[n];
    for (int i = 0; i < n; i++) {
      x[i] = i;
      y[i] = OG_POLYNOMIAL.evaluate(x[i]);
    }
    Function1D<Double, Double> unwrapped = CommonsMathWrapper.unwrap(new PolynomialFunctionLagrangeForm(x, y));
    for (int i = 0; i < 100; i++) {
      assertEquals(unwrapped.evaluate(i + 0.5), OG_POLYNOMIAL.evaluate(i + 0.5), 1e-9);
    }
  }

  @Test
  public void testPointValuePair() {
    double[] x = new double[] {1, 2, 3};
    double[] y = CommonsMathWrapper.unwrap(new PointValuePair(x, 0));
    assertArrayEquals(x, y, 0);
  }

}