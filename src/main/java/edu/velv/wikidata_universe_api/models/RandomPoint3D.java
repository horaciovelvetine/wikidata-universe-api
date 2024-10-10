package edu.velv.wikidata_universe_api.models;

import java.awt.Dimension;
import java.util.Date;
import java.util.Random;

import com.google.common.base.Function;

/**
 * Provides a completely random set of coordinates in 3D space defined by the provided dimensions on initializiation 
 */
public class RandomPoint3D<V> implements Function<V, Point3D> {
  private Random random;
  private Dimension dimensions;

  public RandomPoint3D(Dimension dim) {
    this.dimensions = dim;
    this.random = new Random(new Date().getTime());
  }

  /**
   * Given a vertex, get 3D coordinates in the provided space. Max Z-Coords follows application wide convention
   */
  @Override
  public Point3D apply(V input) {
    double max = Math.max(dimensions.width, dimensions.height);
    return new Point3D(
        (random.nextDouble() - 0.5) * 2 * dimensions.width,
        (random.nextDouble() - 0.5) * 2 * dimensions.height,
        (random.nextDouble() - 0.5) * 2 * max);
  }

}
