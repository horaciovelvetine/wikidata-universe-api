package edu.velv.wikidata_universe_api._TestUtils._v1.models;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Dimension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.velv.wikidata_universe_api.models.Point3D;
import edu.velv.wikidata_universe_api.models.RandomPoint3D;

public class RandomPoint3DTests {

  private RandomPoint3D<Object> randomPoint3D;
  private Dimension dimension;

  @BeforeEach
  public void setUp() {
    dimension = new Dimension(100, 200);
    randomPoint3D = new RandomPoint3D<>(dimension);
  }

  @Test
  public void can_be_initialized_with_dimension() {
    assertNotNull(randomPoint3D, "RandomPoint3D should be initialized");
  }

  @Test
  public void returns_random_point_on_apply() {
    Point3D point = randomPoint3D.apply(new Object());
    assertNotNull(point, "Point3D should not be null");
    assertTrue(point.getX() >= 0 && point.getX() <= dimension.width, "X coordinate is out of bounds");
    assertTrue(point.getY() >= 0 && point.getY() <= dimension.height, "Y coordinate is out of bounds");
    double max = Math.max(dimension.width, dimension.height);
    assertTrue(point.getZ() >= 0 && point.getZ() <= max, "Z coordinate is out of bounds");
  }

  @Test
  public void _0_0_dimensions_limit_point_generation() {
    Dimension zeroDimension = new Dimension(0, 0);
    RandomPoint3D<Object> zeroDimRandomPoint3D = new RandomPoint3D<>(zeroDimension);
    Point3D point = zeroDimRandomPoint3D.apply(new Object());
    assertNotNull(point, "Point3D should not be null");
    assertTrue(point.getX() == 0, "X coordinate should be 0");
    assertTrue(point.getY() == 0, "Y coordinate should be 0");
    assertTrue(point.getZ() == 0, "Z coordinate should be 0");
  }
}