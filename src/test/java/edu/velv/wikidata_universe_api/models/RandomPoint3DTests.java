package edu.velv.wikidata_universe_api.models;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Dimension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.velv.wikidata_universe_api.services.FailedTestMsgTemplates;

public class RandomPoint3DTests implements FailedTestMsgTemplates {
  private final String src_ = "@RandomPoint3DTests:: ";

  private RandomPoint3D<Object> randomPoint3D;
  private Dimension dimension;

  @BeforeEach
  public void setUp() {
    dimension = new Dimension(100, 200);
    randomPoint3D = new RandomPoint3D<>(dimension);
  }

  @Test
  public void can_be_initialized_with_dimension() {
    assertNotNull(randomPoint3D, src_ + shouldBe + inited);
  }

  @Test
  public void returns_random_point_on_apply() {
    Point3D point = randomPoint3D.apply(new Object());
    assertNotNull(point, src_ + shouldNotBe + "null on" + inited);
    assertTrue(point.getX() >= 0 && point.getX() <= dimension.width,
        src_ + "X coordinates" + shouldBe + "in-bounds");
    assertTrue(point.getY() >= 0 && point.getY() <= dimension.height,
        src_ + "Y coordinates" + shouldBe + "in-bounds");
    double max = Math.max(dimension.width, dimension.height);
    assertTrue(point.getZ() >= 0 && point.getZ() <= max,
        src_ + "Z coordinates" + shouldBe + "in-bounds");
  }

  @Test
  public void _0_0_dimensions_limit_point_generation() {
    Dimension zeroDimension = new Dimension(0, 0);
    RandomPoint3D<Object> zeroDimRandomPoint3D = new RandomPoint3D<>(zeroDimension);
    Point3D point = zeroDimRandomPoint3D.apply(new Object());
    assertNotNull(point, src_ + shouldNotBe + "null on" + inited);
    assertTrue(point.getX() == 0, src_ + "X coordinates" + shouldBe + 0);
    assertTrue(point.getY() == 0, src_ + "Y coordinates" + shouldBe + 0);
    assertTrue(point.getZ() == 0, src_ + "Z coordinates" + shouldBe + 0);
  }
}