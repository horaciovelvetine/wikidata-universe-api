package edu.velv.wikidata_universe_api.models.jung;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Point3DTests {
  @Test
  public void init_wDefaultCoordinates() {
    Point3D point3D = new Point3D();
    assertEquals(0.0, point3D.getX(), "X coordinate should be 0.0");
    assertEquals(0.0, point3D.getY(), "Y coordinate should be 0.0");
    assertEquals(0.0, point3D.getZ(), "Z coordinate should be 0.0");
  }

  @Test
  public void init_wProvidedCoordinates() {
    Point3D point3D = new Point3D(1.0, 2.0, 3.0);
    assertEquals(1.0, point3D.getX(), "X coordinate should be 1.0");
    assertEquals(2.0, point3D.getY(), "Y coordinate should be 2.0");
    assertEquals(3.0, point3D.getZ(), "Z coordinate should be 3.0");
  }

  @Test
  public void hasEquality() {
    Point3D point3D1 = new Point3D(1.0, 2.0, 3.0);
    Point3D point3D2 = new Point3D(1.0, 2.0, 3.0);
    assertEquals(point3D1, point3D2, "Two points with the same coordinates should be equal");
  }

  @Test
  public void getsZ() {
    Point3D point3D = new Point3D(1.0, 2.0, 3.0);
    assertEquals(3.0, point3D.getZ(), "getZ() should return Z coordinate");
  }

  @Test
  public void setLocation_wDblCoordinates() {
    Point3D point3D = new Point3D();
    point3D.setLocation(4.0, 5.0, 6.0);
    assertEquals(4.0, point3D.getX(), "X coordinate should be  4.0");
    assertEquals(5.0, point3D.getY(), "Y coordinate should be  5.0");
    assertEquals(6.0, point3D.getZ(), "Z coordinate should be  6.0");
  }

  @Test
  public void setsLocation_wPoint3D() {
    Point3D point3D1 = new Point3D(1.0, 2.0, 3.0);
    Point3D point3D2 = new Point3D(4.0, 5.0, 6.0);
    point3D1.setLocation(point3D2);
    assertEquals(4.0, point3D1.getX(), "X coordinate should be  4.0");
    assertEquals(5.0, point3D1.getY(), "Y coordinate should be  5.0");
    assertEquals(6.0, point3D1.getZ(), "Z coordinate should be  6.0");
  }

  @Test
  public void calculatesDistanceSquared() {
    Point3D point3D1 = new Point3D(1.0, 2.0, 3.0);
    Point3D point3D2 = new Point3D(4.0, 6.0, 8.0);
    assertEquals(50.00, point3D1.distanceSq(point3D2), "Distance squared should be calculated correctly");
  }

  @Test
  public void calculatesDistance() {
    Point3D point3D1 = new Point3D(1.0, 2.0, 3.0);
    Point3D point3D2 = new Point3D(4.0, 6.0, 8.0);
    assertEquals(Math.sqrt(50.00), point3D1.distance(point3D2), "Distance should be calculated correctly");
  }
}
