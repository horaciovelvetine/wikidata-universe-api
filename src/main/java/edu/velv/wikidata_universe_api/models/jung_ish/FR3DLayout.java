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
import edu.velv.wikidata_universe_api.models.Constables;
import edu.velv.wikidata_universe_api.utils.Loggable;

public class FR3DLayout implements Loggable {
  protected Graphset graph;
  protected Dimension size;
  protected final double EPSILON = 0.0000001;
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

  public FR3DLayout() {
    // default empty constructor
  }

  public FR3DLayout(Graphset graph, Dimension size) {
    if (graph == null || size == null) {
      throw new IllegalArgumentException("Graph and size must be non-null");
    }
    this.graph = graph;
    this.size = size;
  }

  protected void doInit() {
    int totalVertices = graph.vertexCount();
    double width = size.getWidth();
    double height = size.getHeight();
    double maxDim = Math.max(width, height);
    double minDim = Math.min(width, height);
    double volume = width * height * maxDim;
    double currentDensity = (totalVertices * Math.pow(20, 3) / volume);
    print("===============================================================");
    print("LAYOUT SCALE IS::" + currentDensity);
    print("===============================================================");
    // double scaleFactor = Math.cbrt(0.0000001 / currentDensity);
    // width *= scaleFactor;
    // height *= scaleFactor;
    // maxDim *= scaleFactor;
    // minDim *= scaleFactor;
    size.setSize(width, height);

    setAndInitPositions(new RandomLocation3D<Vertex>(size));

    currentIteration = 0;
    temperature = width / 10;
    forceConst = Math.sqrt(height * width / graph.vertexCount());
    attrConst = forceConst * Constables.F3D_ATTR_MULT;
    repConst = forceConst * Constables.F3D_REP_MULT;
    maxDimension = maxDim;
    borderWidth = minDim / Constables.F3D_BRDR_FACT;

  }

  /**
   * Attempts to initialize a layout with the sessions current graph
   */
  public void initialize() {
    doInit();
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
          calcPosition(v);
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
    if (currentIteration > Constables.F3D_MAX_ITER || temperature < 1.0 / maxDimension) {
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
    if ((isLocked(v1) && isLocked(v2)) || v1.id() == v2.id()) {
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
      updateOffset(v1, -xDisp, -yDisp, -zDisp, isLocked(v2));
    }

    if (!isLocked(v2)) {
      updateOffset(v2, xDisp, yDisp, zDisp, isLocked(v1));
    }
  }

  protected void calcPosition(Vertex v) {
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

    double newX = p.getX()
        + Math.max(-Constables.F3D_MAX_MVMNT_PER_ITER, Math.min(Constables.F3D_MAX_MVMNT_PER_ITER, xDisp));
    double newY = p.getY()
        + Math.max(-Constables.F3D_MAX_MVMNT_PER_ITER, Math.min(Constables.F3D_MAX_MVMNT_PER_ITER, yDisp));
    double newZ = p.getZ()
        + Math.max(-Constables.F3D_MAX_MVMNT_PER_ITER, Math.min(Constables.F3D_MAX_MVMNT_PER_ITER, zDisp));

    double width = size.getWidth();
    double height = size.getHeight();

    newX = adjustPositionToBorderBox(newX, width);
    newY = adjustPositionToBorderBox(newY, height);
    newZ = adjustPositionToBorderBox(newZ, Math.max(width, height));

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
    }, new Function<Vertex, Point3D>() {
      public Point3D apply(Vertex v) {
        if (lockedVertices.contains(v) && v.coords != null) {
          return v.coords();
        } else {
          return initializer.apply(v);
        }
      }
    });

    this.locationData = CacheBuilder.newBuilder().build(CacheLoader.from(chain));
    this.offsetData = CacheBuilder.newBuilder().build(CacheLoader.from(chain));
    initialized = true;
  }

  protected boolean isLocked(Vertex v) {
    for (Vertex lockedVertex : lockedVertices) {
      if (lockedVertex.id().equals(v.id())) {
        return true;
      }
    }
    return false;
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
    temperature *= (1.0 - currentIteration / (double) Constables.F3D_MAX_ITER);
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
