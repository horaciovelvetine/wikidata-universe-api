package edu.velv.wikidata_universe_api.models.jung_ish;

import java.awt.geom.Point2D;

public class Point3D extends Point2D.Double {
  public double z;

  /**
   * Constructs a new JPoint3D object with default coordinates (0, 0, 0).
   */
  public Point3D() {
    super(0.0, 0.0);
    z = 0.0;
  }

  /**
   * Constructs a new JPoint3D object with the specified coordinates.
   *
   * @param x The x-coordinate of the point.
   * @param y The y-coordinate of the point.
   * @param z The z-coordinate of the point.
   */
  public Point3D(double x, double y, double z) {
    super(x, y);
    this.z = z;
  }

  /**
   * Returns the z-coordinate of the point.
   *
   * @return The z-coordinate of the point.
   */
  public double getZ() {
    return z;
  }

  /**
   * Sets the location of the point to the specified coordinates.
   *
   * @param x The x-coordinate of the point.
   * @param y The y-coordinate of the point.
   * @param z The z-coordinate of the point.
   */
  public void setLocation(double x, double y, double z) {
    super.setLocation(x, y);
    this.z = z;
  }

  /**
   * Sets the location of the point to the coordinates of the specified JPoint3D object.
   *
   * @param p The JPoint3D object whose coordinates will be used.
   */
  public void setLocation(Point3D p) {
    setLocation(p.x, p.y, p.z);
  }

  /**
   * Returns the square of the Euclidean distance between two points in 3D space.
   *
   * @param x1 The x-coordinate of the first point.
   * @param y1 The y-coordinate of the first point.
   * @param z1 The z-coordinate of the first point.
   * @param x2 The x-coordinate of the second point.
   * @param y2 The y-coordinate of the second point.
   * @param z2 The z-coordinate of the second point.
   * @return The square of the Euclidean distance between the two points.
   */
  public static double distanceSq(double x1, double y1, double z1, double x2, double y2,
      double z2) {
    double dx = x2 - x1;
    double dy = y2 - y1;
    double dz = z2 - z1;
    return dx * dx + dy * dy + dz * dz;
  }

  /**
   * Returns the square of the Euclidean distance between this point and the specified point in 3D space.
   *
   * @param x The x-coordinate of the point.
   * @param y The y-coordinate of the point.
   * @param z The z-coordinate of the point.
   * @return The square of the Euclidean distance between this point and the specified point.
   */
  public double distanceSq(double x, double y, double z) {
    return distanceSq(getX(), getY(), this.z, x, y, z);
  }

  /**
   * Returns the square of the Euclidean distance between this point and the specified JPoint3D object in 3D space.
   *
   * @param pt The JPoint3D object.
   * @return The square of the Euclidean distance between this point and the specified JPoint3D object.
   */
  public double distanceSq(Point3D pt) {
    return distanceSq(x, y, z, pt.getX(), pt.getY(), pt.getZ());
  }

  /**
   * Returns the Euclidean distance between two points in 3D space.
   *
   * @param x1 The x-coordinate of the first point.
   * @param y1 The y-coordinate of the first point.
   * @param z1 The z-coordinate of the first point.
   * @param x2 The x-coordinate of the second point.
   * @param y2 The y-coordinate of the second point.
   * @param z2 The z-coordinate of the second point.
   * @return The Euclidean distance between the two points.
   */
  public static double distance(double x1, double y1, double z1, double x2, double y2, double z2) {
    return Math.sqrt(distanceSq(x1, y1, z1, x2, y2, z2));
  }

  /**
   * Returns the Euclidean distance between this point and the specified point in 3D space.
   *
   * @param x The x-coordinate of the point.
   * @param y The y-coordinate of the point.
   * @param z The z-coordinate of the point.
   * @return The Euclidean distance between this point and the specified point.
   */
  public double distance(double x, double y, double z) {
    return Math.sqrt(distanceSq(x, y, z));
  }

  /**
   * Returns the Euclidean distance between this point and the specified JPoint3D object in 3D space.
   *
   * @param pt The JPoint3D object.
   * @return The Euclidean distance between this point and the specified JPoint3D object.
   */
  public double distance(Point3D pt) {
    return Math.sqrt(distanceSq(pt));
  }

  /**
   * Returns a string representation of this JPoint3D object.
   *
   * @return A string representation of this JPoint3D object.
   */
  @Override
  public String toString() {
    String c = ", ";
    return "xyz:[" + getX() + c + getY() + c + z + "]";
  }

  /**
   * Checks if this JPoint3D object is equal to the specified object.
   *
   * @param obj The object to compare.
   * @return true if the objects are equal, false otherwise.
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Point3D) {
      Point3D p = (Point3D) obj;
      return x == p.getX() && y == p.getY() && z == p.getZ();
    }
    return super.equals(obj);
  }

  /**
   * Returns the hash code value for this JPoint3D object.
   *
   * @return The hash code value for this JPoint3D object.
   */
  @Override
  public int hashCode() {
    long bits = java.lang.Double.doubleToLongBits(x);
    int hash = (int) (bits ^ (bits >>> 32));
    hash = 31 * hash + (int) (java.lang.Double.doubleToLongBits(y)
        ^ (java.lang.Double.doubleToLongBits(y) >>> 32));
    hash = 31 * hash + (int) (java.lang.Double.doubleToLongBits(z)
        ^ (java.lang.Double.doubleToLongBits(z) >>> 32));
    return hash;
  }
}
