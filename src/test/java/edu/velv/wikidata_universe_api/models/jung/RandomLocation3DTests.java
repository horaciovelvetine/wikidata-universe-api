package edu.velv.wikidata_universe_api.models.jung;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.awt.Dimension;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class RandomLocation3DTests {
  private RandomLocation3D<Object> randomLocation3D;
  private Dimension dimension;

  @BeforeEach
  public void setUp() {
    dimension = new Dimension(100, 200);
    randomLocation3D = new RandomLocation3D<>(dimension);
  }

  @Test
  public void testApplyMethodReturnsPointWithinDimension() {
    Object dummyInput = new Object();
    Point3D point = randomLocation3D.apply(dummyInput);

    assertTrue(0 <= point.getX() && point.getX() <= dimension.width, "X-coordinate is within range");
    assertTrue(0 <= point.getY() && point.getY() <= dimension.height, "Y-coordinate is within range");
    assertTrue(0 <= point.getZ() && point.getZ() <= dimension.height, "Y-coordinate is within range");
  }

  @Test
  public void testApplyMethodReturnsDifferentPointsForSameInput() {
    Object dummyInput = new Object();
    Point3D point1 = randomLocation3D.apply(dummyInput);
    Point3D point2 = randomLocation3D.apply(dummyInput);

    boolean differentPoints = point1.getX() != point2.getX() || point1.getY() != point2.getY()
        || point1.getZ() != point2.getZ();
    assertTrue(differentPoints, "Apply method returns different points for the same input");
  }
}
