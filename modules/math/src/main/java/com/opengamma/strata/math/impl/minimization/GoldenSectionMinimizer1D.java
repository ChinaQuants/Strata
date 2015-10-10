/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.strata.math.impl.minimization;

import com.opengamma.strata.collect.ArgChecker;
import com.opengamma.strata.math.impl.MathException;
import com.opengamma.strata.math.impl.function.Function1D;

/**
 * 
 */
public class GoldenSectionMinimizer1D implements ScalarMinimizer {

  private static final double GOLDEN = 0.61803399;
  private static final MinimumBracketer BRACKETER = new ParabolicMinimumBracketer();
  private static final int MAX_ITER = 10000;
  private static final double EPS = 1e-12;

  @Override
  public double minimize(Function1D<Double, Double> f, double startPosition, double lower, double upper) {
    return minimize(f, lower, upper);
  }

  public double minimize(Function1D<Double, Double> f, double lower, double upper) {
    ArgChecker.notNull(f, "function");
    double x0, x1, x2, x3, f1, f2, temp;
    int i = 0;
    double[] triplet = BRACKETER.getBracketedPoints(f, lower, upper);
    x0 = triplet[0];
    x3 = triplet[2];
    if (Math.abs(triplet[2] - triplet[1]) > Math.abs(triplet[1] - triplet[0])) {
      x1 = triplet[1];
      x2 = triplet[2] + GOLDEN * (triplet[1] - triplet[2]);
    } else {
      x2 = triplet[1];
      x1 = triplet[0] + GOLDEN * (triplet[1] - triplet[0]);
    }
    f1 = f.evaluate(x1);
    f2 = f.evaluate(x2);
    while (Math.abs(x3 - x0) > EPS * (Math.abs(x1) + Math.abs(x2))) {
      if (f2 < f1) {
        temp = GOLDEN * (x2 - x3) + x3;
        x0 = x1;
        x1 = x2;
        x2 = temp;
        f1 = f2;
        f2 = f.evaluate(temp);
      } else {
        temp = GOLDEN * (x1 - x0) + x0;
        x3 = x2;
        x2 = x1;
        x1 = temp;
        f2 = f1;
        f1 = f.evaluate(temp);
      }
      i++;
      if (i > MAX_ITER) {
        throw new MathException(
            "Could not find minimum: this should not happen because minimum should have been successfully bracketed");
      }
    }
    if (f1 < f2) {
      return x1;
    }
    return x2;
  }

  @Override
  public Double minimize(Function1D<Double, Double> function, Double startPosition) {
    throw new UnsupportedOperationException("Need lower and upper bounds to use this minimization method");
  }
}
