package edu.velv.wikidata_universe_api._v1.models;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import edu.velv.wikidata_universe_api.models.Point3D;

public class Point3DTests {

  @Test
  public void constructs_default_point3D() {
    Point3D point = new Point3D();
    assertEquals(0.0, point.getX());
    assertEquals(0.0, point.getY());
    assertEquals(0.0, point.getZ());
  }

  @Test
  public void constructs_point3D_with_doubles() {
    Point3D point = new Point3D(1.0, 2.0, 3.0);
    assertEquals(1.0, point.getX());
    assertEquals(2.0, point.getY());
    assertEquals(3.0, point.getZ());
  }

  @Test
  public void gets_Z() {
    Point3D point = new Point3D(1.0, 2.0, 3.0);
    assertEquals(3.0, point.getZ());
  }

  @Test
  public void sets_location_with_doubles() {
    Point3D point = new Point3D();
    point.setLocation(4.0, 5.0, 6.0);
    assertEquals(4.0, point.getX());
    assertEquals(5.0, point.getY());
    assertEquals(6.0, point.getZ());
  }

  @Test
  public void sets_location_with_point3D() {
    Point3D point1 = new Point3D(1.0, 2.0, 3.0);
    Point3D point2 = new Point3D();
    point2.setLocation(point1);
    assertEquals(1.0, point2.getX());
    assertEquals(2.0, point2.getY());
    assertEquals(3.0, point2.getZ());
  }

  @Test
  public void static_calculates_distance_square() {
    double distanceSq = Point3D.distanceSq(1.0, 2.0, 3.0, 4.0, 5.0, 6.0);
    assertEquals(27.0, distanceSq);
  }

  @Test
  public void calculates_distance_square() {
    Point3D point = new Point3D(1.0, 2.0, 3.0);
    double distanceSq = point.distanceSq(4.0, 5.0, 6.0);
    assertEquals(27.0, distanceSq);
  }

  @Test
  public void calculates_destance_square_with_points3D() {
    Point3D point1 = new Point3D(1.0, 2.0, 3.0);
    Point3D point2 = new Point3D(4.0, 5.0, 6.0);
    double distanceSq = point1.distanceSq(point2);
    assertEquals(27.0, distanceSq);
  }

  @Test
  public void static_calculates_distance() {
    double distance = Point3D.distance(1.0, 2.0, 3.0, 4.0, 5.0, 6.0);
    assertEquals(Math.sqrt(27.0), distance);
  }

  @Test
  public void calculates_distance() {
    Point3D point = new Point3D(1.0, 2.0, 3.0);
    double distance = point.distance(4.0, 5.0, 6.0);
    assertEquals(Math.sqrt(27.0), distance);
  }

  @Test
  public void calculates_distance_with_points3D() {
    Point3D point1 = new Point3D(1.0, 2.0, 3.0);
    Point3D point2 = new Point3D(4.0, 5.0, 6.0);
    double distance = point1.distance(point2);
    assertEquals(Math.sqrt(27.0), distance);
  }

  @Test
  public void returns_string_representation() {
    Point3D point = new Point3D(1.0, 2.0, 3.0);
    assertEquals("xyz:[1.0, 2.0, 3.0]", point.toString());
  }

  @Test
  public void knows_when_its_met_its_equal() {
    Point3D point1 = new Point3D(1.0, 2.0, 3.0);
    Point3D point2 = new Point3D(1.0, 2.0, 3.0);
    assertTrue(point1.equals(point2));
  }

  @Test
  public void knows_when_its_met_an_unequal() {
    Point3D point1 = new Point3D(1.0, 2.0, 3.0);
    Point3D point2 = new Point3D(4.0, 5.0, 6.0);
    assertFalse(point1.equals(point2));
  }

  @Test
  public void hashCode_consistently_hashes_points() {
    Point3D point = new Point3D(1.0, 2.0, 3.0);
    int initialHashCode = point.hashCode();
    assertEquals(initialHashCode, point.hashCode(), "Hash code should be consistent");
  }

  @Test
  public void hashCode_equality_working() {
    Point3D point1 = new Point3D(1.0, 2.0, 3.0);
    Point3D point2 = new Point3D(1.0, 2.0, 3.0);
    assertEquals(point1.hashCode(), point2.hashCode(), "Hash codes should be equal for equal points");
  }

  @Test
  public void hashCode_inequality_working() {
    Point3D point1 = new Point3D(1.0, 2.0, 3.0);
    Point3D point2 = new Point3D(4.0, 5.0, 6.0);
    assertNotEquals(point1.hashCode(), point2.hashCode(), "Hash codes should be different for different points");
  }
}