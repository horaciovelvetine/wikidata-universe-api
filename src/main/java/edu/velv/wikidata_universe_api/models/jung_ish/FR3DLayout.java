package edu.velv.wikidata_universe_api.models.jung_ish;

import java.awt.Dimension;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.ConcurrentModificationException;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

import io.vavr.Tuple2;
import edu.velv.wikidata_universe_api.errors.*;
import edu.velv.wikidata_universe_api.errors.Err.LayoutProcessError;
import edu.velv.wikidata_universe_api.utils.Loggable;

public class FR3DLayout implements Loggable {
  protected Graphset graph;
  protected Dimension size;
  // CONST
  protected final int MAX_ITER = 700;
  protected final int ITER_MVMNT_MAX = 5;
  protected final int DITH_MAGN = 2;
  protected final int BRDR_FACT = 50;
  protected final double EPSILON = 0.000001;
  protected final double ATTR_MULT = 0.75;
  protected final double REP_MULT = 0.75;
  // CALC'D
  protected double forceConst;
  protected double temperature;
  protected double attrConst;
  protected double repConst;
  protected double maxDimension;
  protected double borderWidth;
  // 'STATE'
  protected int currentIteration;
  protected boolean initialized = false;
  protected Set<Vertex> lockedVertices = new HashSet<>();
  // DATA STORAGE
  @SuppressWarnings("null")
  LoadingCache<Vertex, Point3D> locationData = CacheBuilder.newBuilder().build(new CacheLoader<Vertex, Point3D>() {
    public Point3D load(Vertex v) throws Exception {
      return new Point3D();
    }
  });
  @SuppressWarnings("null")
  protected LoadingCache<Vertex, Point3D> offsetData = CacheBuilder.newBuilder()
      .build(new CacheLoader<Vertex, Point3D>() {
        public Point3D load(Vertex v) throws Exception {
          return new Point3D();
        }
      });

  protected FR3DLayout() {
  }

  public FR3DLayout(Graphset graph, Dimension size) {
    if (graph == null || size == null) {
      throw new IllegalArgumentException("Graph and size must be non-null");
    }
    this.graph = graph;
    this.size = size;
  }

  protected void doInit() {
    if (graph != null && size != null) {
      Double width = size.getWidth();
      Double height = size.getHeight();

      setAndInitPositions(new RandomLocation3D<Vertex>(size));

      Vertex origin = graph.getOriginRef();
      if (origin == null) {

      }
      getLocationData(origin).setLocation(new Point3D(EPSILON, EPSILON, EPSILON));
      lockedVertices.add(origin);

      currentIteration = 0;
      temperature = width / 10;
      forceConst = Math.sqrt(height * width / graph.vertexCount());
      attrConst = forceConst * ATTR_MULT;
      repConst = forceConst * REP_MULT;
      maxDimension = Math.max(width, height);
      borderWidth = Math.min(width, height) / BRDR_FACT;
    }
  }

  /**
   * Attempts to initialize a layout with the sessions current graph
   */
  public Optional<Err> initialize() {
    try {
      doInit();
      return Optional.empty();
    } catch (Exception e) {
      print(e.getMessage());
      return Optional.of(new LayoutProcessError("Unable to initialize layout: ", e));
    }
  }

  /**
   * Main function of the layout. Given a vertex, returns the 3D coordinates representing that point
   * in the layout.
   */
  public Point3D apply(Vertex v) {
    return getLocationData(v);
  }

  /**
   * Reverse the lock state of the given vertex
   */
  public void lock(Vertex v, boolean state) {
    if (state) {
      lockedVertices.add(v);
    } else {
      lockedVertices.remove(v);
    }
  }

  /**
   * Locks or unlock the state of all vertices in the graph
   */
  public void lock(boolean lock) {
    for (Vertex v : graph.vertices()) {
      lock(v, lock);
    }
  }

  /**
   * Run the layout calculations and increase the currentIteration by 1
   */
  public void step() {
    currentIteration++;
    while (true) {
      try {
        for (Vertex v : graph.vertices()) {
          if (isLocked(v)) {
            continue;
          }
          calcRepulsion(v);
        }
        break;
      } catch (ConcurrentModificationException cme) {
      }
    }
    while (true) {
      try {
        for (Edge e : graph.edges()) {
          calcAttraction(e);
        }
        break;
      } catch (ConcurrentModificationException cme) {
      }
    }
    while (true) {
      try {
        for (Vertex v : graph.vertices()) {
          if (isLocked(v))
            continue;
          calcPositions(v);
        }
        break;
      } catch (ConcurrentModificationException cme) {
      }
    }
    cool();
  }

  /**
   * Checks if this layout has reached the max allowed number of iterations or 
   * a temperature which indicates this layout is a suitable representation of
   * the Graphset. 
   */
  public boolean done() {
    if (currentIteration > MAX_ITER || temperature < 1.0 / maxDimension) {
      return true;
    }
    return false;
  }

  // Force Calculations...
  //=====================================================================================================================>
  //=====================================================================================================================>
  //=====================================================================================================================>

  protected void calcRepulsion(Vertex v1) {
    if (isLocked(v1))
      return;

    Point3D offset = getOffsetData(v1);

    if (offset == null)
      return;

    offset.setLocation(0, 0, 0); //! initialize offset on ea. iterations start...

    try {
      for (Vertex v2 : graph.vertices()) {
        if (v1 == v2 || (isLocked(v1) && isLocked(v2)))
          continue;

        Point3D p1 = getLocationData(v1);
        Point3D p2 = getLocationData(v2);
        if (p1 == null || p2 == null)
          continue;

        double dx = p1.getX() - p2.getX();
        double dy = p1.getY() - p2.getY();
        double dz = p1.getZ() - p2.getZ();
        double dl = Math.max(EPSILON, p1.distance(p2));
        double repForce = (repConst * repConst) / dl;

        if (Double.isNaN(repForce))
          throw new IllegalArgumentException("NaN in repulsion force calculation");

        double xDisp = dx * repForce;
        double yDisp = dy * repForce;
        double zDisp = dz * repForce;

        updateOffset(v1, xDisp, yDisp, zDisp, isLocked(v2)); //v2 is locked, offset v1 x 2
      }
    } catch (ConcurrentModificationException cme) {
      calcRepulsion(v1); // retry
    }
  }

  protected void calcAttraction(Edge e) {
    Optional<Tuple2<Vertex, Vertex>> endpoints = graph.getEndpoints(e);
    if (endpoints.isEmpty()) {
      return;
    }

    Vertex v1 = endpoints.get()._1();
    Vertex v2 = endpoints.get()._2();
    if ((isLocked(v1) && isLocked(v2)) || v1 == v2) {
      return;
    }

    Point3D p1 = getLocationData(v1);
    Point3D p2 = getLocationData(v2);
    if (p1 == null || p2 == null)
      return;

    double dx = p1.getX() - p2.getX();
    double dy = p1.getY() - p2.getY();
    double dz = p1.getZ() - p2.getZ();
    double dl = Math.max(EPSILON, p1.distance(p2));
    double force = dl * dl / attrConst;

    if (Double.isNaN(force))
      throw new IllegalArgumentException("NaN in attraction force calculation");

    double xDisp = dx * force / dl;
    double yDisp = dy * force / dl;
    double zDisp = dz * force / dl;

    if (!isLocked(v1)) {
      updateOffset(v1, -xDisp, -yDisp, -zDisp, isLocked(v2)); // opposite direction(s)
    }

    if (!isLocked(v2)) {
      updateOffset(v2, xDisp, yDisp, zDisp, isLocked(v1));
    }
  }

  protected void calcPositions(Vertex v) {
    if (isLocked(v))
      return;

    Point3D p = getLocationData(v);
    if (p == null)
      return;

    Point3D offset = getOffsetData(v);
    Double dl = Math.max(EPSILON, offset.distanceSq(offset));

    double xDisp = offset.getX() / dl * Math.min(dl, temperature);
    double yDisp = offset.getY() / dl * Math.min(dl, temperature);
    double zDisp = offset.getZ() / dl * Math.min(dl, temperature);

    if (Double.isNaN(xDisp) || Double.isNaN(yDisp) || Double.isNaN(zDisp))
      throw new IllegalArgumentException("NaN in position calculation");

    double newX = p.getX() + Math.max(-ITER_MVMNT_MAX, Math.min(ITER_MVMNT_MAX, xDisp));
    double newY = p.getY() + Math.max(-ITER_MVMNT_MAX, Math.min(ITER_MVMNT_MAX, yDisp));
    double newZ = p.getZ() + Math.max(-ITER_MVMNT_MAX, Math.min(ITER_MVMNT_MAX, zDisp));

    double width = size.getWidth();
    double height = size.getHeight();

    newX = adjustPositionToBorderBox(newX, width);
    newY = adjustPositionToBorderBox(newY, height);
    newZ = adjustPositionToBorderBox(newZ, Math.min(width, height)); // z min of x, y

    p.setLocation(newX, newY, newZ);
  }

  // Utils...
  //=====================================================================================================================>
  //=====================================================================================================================>
  //=====================================================================================================================>

  protected void setAndInitPositions(Function<Vertex, Point3D> initializer) {

    Function<Vertex, Point3D> chain = Functions.<Vertex, Point3D, Point3D>compose(new Function<Point3D, Point3D>() {
      public Point3D apply(Point3D input) {
        return (Point3D) input.clone();
      }
    }, initializer);

    this.locationData = CacheBuilder.newBuilder().build(CacheLoader.from(chain));
    this.offsetData = CacheBuilder.newBuilder().build(CacheLoader.from(chain));
    initialized = true;
  }

  protected boolean isLocked(Vertex v) {
    return lockedVertices.contains(v);
  }

  protected Point3D getOffsetData(Vertex v) {
    return offsetData.getUnchecked(v);
  }

  protected Point3D getLocationData(Vertex V) {
    return locationData.getUnchecked(V);
  }

  protected void offsetVertexLocation(Vertex v, double dx, double dy, double dz) {
    Point3D p = getLocationData(v);
    double ox = p.getX() + dx;
    double oy = p.getY() + dy;
    double oz = p.getZ() + dz;

    p.setLocation(ox, oy, oz);
  }

  public void cool() {
    temperature *= (1.0 - currentIteration / (double) MAX_ITER);
    if (currentIteration % 100 == 0) {
      adjustForceConstants();
    }
  }

  protected void updateOffset(Vertex v, double xDisp, double yDisp, double zDisp,
      boolean opposingIsLocked) {
    if (isLocked(v)) {
      return;
    }
    Point3D offset = getOffsetData(v);
    int factor = opposingIsLocked ? 2 : 1;

    offset.setLocation(offset.getX() + factor * xDisp, offset.getY() + factor * yDisp,
        offset.getZ() + factor * zDisp);
  }

  protected double adjustPositionToBorderBox(double coordPos, double dimMax) {
    if (coordPos < borderWidth) {

    } else if (coordPos > dimMax - borderWidth) {

    }
    return coordPos;
  }

  private void adjustForceConstants() {
    double averageRepulsionForce = calculateAverageRepulsionForce();
    double averageAttractionForce = calculateAverageAttractionForce();

    if (averageRepulsionForce > averageAttractionForce) {
      repConst *= 0.9;
      attrConst *= 1.1;
    } else if (averageAttractionForce > averageRepulsionForce) {
      repConst *= 1.1;
      attrConst *= 0.9;
    }
  }

  private double calculateAverageRepulsionForce() {
    double totalRepulsionForce = 0;
    int count = 0;
    Collection<Vertex> verts = graph.vertices();
    for (Vertex v1 : verts) {
      for (Vertex v2 : verts) {
        if (v1 != v2) {
          Point3D p1 = getLocationData(v1);
          Point3D p2 = getLocationData(v2);
          double dl = p1.distance(p2);
          double repForce = (repConst * repConst) / dl;
          totalRepulsionForce += repForce;
          count++;
        }
      }
    }

    return totalRepulsionForce / count;
  }

  private double calculateAverageAttractionForce() {
    double totalAttractionForce = 0;
    int count = 0;
    Collection<Edge> eds = graph.edges();

    for (Edge e : eds) {
      Optional<Tuple2<Vertex, Vertex>> endpoints = graph.getEndpoints(e);
      if (endpoints.isEmpty()) {
        continue;
      }
      Vertex v1 = endpoints.get()._1();
      Vertex v2 = endpoints.get()._2();

      Point3D p1 = getLocationData(v1);
      Point3D p2 = getLocationData(v2);

      double dl = p1.distance(p2);
      double force = dl * dl / attrConst;
      totalAttractionForce += force;

      count++;
    }

    return totalAttractionForce / count;
  }

}
