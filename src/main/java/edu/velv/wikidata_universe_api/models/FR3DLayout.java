package edu.velv.wikidata_universe_api.models;

import java.awt.Dimension;
import java.util.Set;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Optional;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import edu.velv.wikidata_universe_api.Constables;
import io.vavr.Tuple2;

/**
 * This FR3DLayout represents a 3D adaption of the graph layout algorithim from Fruchterman Reingold. 
 * With <3 & much credit @see => https://github.com/jrtom/jung to JUNG & it's contributors - thank you!
 * This implementation uses the @FRLayout & @FRLayout2 implementations, adding an additional dimension.
 * 
 * This process is covered in depth inside my prototype(-ing) repo, with detailed README's here:
 * @see https://github.com/horaciovelvetine/ForceDrawnGraphs
 */
public class FR3DLayout {
  private Graphset graph;
  private Set<Vertex> lockedVertices = new HashSet<>();
  LoadingCache<Vertex, Point3D> locationData = CacheBuilder.newBuilder().build(new CacheLoader<Vertex, Point3D>() {
    public Point3D load(Vertex v) throws Exception {
      return new Point3D();
    }
  });
  LoadingCache<Vertex, Point3D> offsetData = CacheBuilder.newBuilder().build(new CacheLoader<Vertex, Point3D>() {
    public Point3D load(Vertex v) throws Exception {
      return new Point3D();
    }
  });

  private Dimension size;
  private int curIteration;
  private final double EPSILON = 0.000000001; // prevents 0 div
  private double temperature;
  private double forceConst;
  private double attrConst;
  private double repConst;

  public FR3DLayout(Dimension dimensions, Graphset graph) {
    this.size = dimensions;
    this.graph = graph;
  }

  /**
   * Run prior to stepping the layout: scales the layout dimensions to the Graphset's details, 
   * initializes positions for any unlocked positions, and calculates constants used to simulate
   * the 'physical constants' of the layout itself. 
   */
  public void initialize() {
    scaleDimensionsToGraphsetSize();
    setInitialRandomPositions(new RandomPoint3D<>(size));
    initializeLayoutConstants();
  }

  /**
   * Main function of the layout. Given a vertex, returns the 3D coordinates representing that point
   * in the layout.
   *
   * @param Vertex to get the location data of
   * @return Point3D position
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
   * Moves the algorithm exactly 1 step forward - calculating the cumulative offset effected on each member of the Graphset
   * then applying that cumulative offset to each Vertice's current location to move to their new 'better fit' position
   * 
   * @apiNote This approach explicitly ignores and retries when data is concurrently modified
   */
  public void step() {
    curIteration++;
    while (true) {
      try {
        for (Vertex v : graph.vertices()) {
          if (isLocked(v))
            continue;
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
   * Checks wether or not the current layout has reached either the maximum iterations or an acceptable temperature.
  */
  public boolean done() {
    return curIteration > Constables.MAX_ITERS || temperature < 1.0 / maxDim();
  }

  //=====================================================================================================================>
  //! FORCE CALCULATIONS...
  //=====================================================================================================================>
  //=====================================================================================================================>
  //=====================================================================================================================>
  //=====================================================================================================================>
  //=====================================================================================================================>

  private void calcRepulsion(Vertex v) {
    Point3D curOffset = getOffsetData(v);
    curOffset.setLocation(0, 0, 0); //? offset data resets on step here @ start of iteration

    for (Vertex v2 : graph.vertices()) {
      if (bothVerticesLocked(v, v2))
        continue;

      Point3D p1 = getLocationData(v);
      Point3D p2 = getLocationData(v2);

      double dl = Math.max(EPSILON, p1.distance(p2));
      double force = (repConst * repConst) / dl;

      if (Double.isNaN(force))
        throw new IllegalArgumentException("NaN in repulsion force calc.");

      int oppositeLockMult = isLocked(v2) ? 2 : 1;
      double xDisp = xDiff(p1, p2) * force * oppositeLockMult;
      double yDisp = yDiff(p1, p2) * force * oppositeLockMult;
      double zDisp = zDiff(p1, p2) * force * oppositeLockMult;
      Point3D dispVec = new Point3D(xDisp, yDisp, zDisp);

      updateOffset(v, dispVec);
    }
  }

  private void calcAttraction(Edge e) {
    Optional<Tuple2<Vertex, Vertex>> endpoints = graph.getEndpoints(e);
    if (endpoints.isEmpty())
      return;

    Vertex v1 = endpoints.get()._1();
    Vertex v2 = endpoints.get()._2();
    if (bothVerticesLocked(v1, v2))
      return;

    Point3D p1 = getLocationData(v1);
    Point3D p2 = getLocationData(v2);

    double dl = Math.max(EPSILON, p1.distance(p2));
    double force = dl * dl / attrConst;

    if (Double.isNaN(force))
      throw new IllegalArgumentException("NaN in repulsion force calc.");

    double xDisp = xDiff(p1, p2) * force / dl;
    double yDisp = yDiff(p1, p2) * force / dl;
    double zDisp = zDiff(p1, p2) * force / dl;

    if (!isLocked(v1)) {
      int scale = isLocked(v2) ? 2 : 1;
      Point3D dispVec = new Point3D(-(scale * xDisp), -(scale * yDisp), -(scale * zDisp));
      updateOffset(v1, dispVec);
    }

    if (!isLocked(v2)) {
      int scale = isLocked(v1) ? 2 : 1;
      Point3D dispVec = new Point3D(-(scale * xDisp), -(scale * yDisp), -(scale * zDisp));
      updateOffset(v2, dispVec);
    }

  }

  private void calcPosition(Vertex v) {
    Point3D offset = getOffsetData(v);

    double dl = Math.max(EPSILON, offset.distanceSq(offset));
    double xDisp = offset.getX() / dl * Math.min(dl, temperature);
    double yDisp = offset.getY() / dl * Math.min(dl, temperature);
    double zDisp = offset.getZ() / dl * Math.min(dl, temperature);

    if (Double.isNaN(xDisp) || Double.isNaN(yDisp) || Double.isNaN(zDisp))
      throw new IllegalArgumentException("NaN value found in update position displacement calcs");

    Point3D location = getLocationData(v);
    location.setLocation(location.getX() + xDisp, location.getY() + yDisp, location.getZ() + zDisp);
  }

  //=====================================================================================================================>
  //=====================================================================================================================>
  //=====================================================================================================================>
  //=====================================================================================================================>
  //=====================================================================================================================>
  //! HELPERS...
  //=====================================================================================================================>

  /**
   * Composes a chain function to call which guarentees unrelated Random Positions are initialized for any unlocked Vertex.
   * This needs to be called before the step() function is called for the layout to work correctly.
   *
   * @param initializer - any function which given a Vertex provides a Point3D in return
   */
  private void setInitialRandomPositions(Function<Vertex, Point3D> initializer) {
    Function<Vertex, Point3D> chain = Functions.<Vertex, Point3D, Point3D>compose(new Function<Point3D, Point3D>() {
      public Point3D apply(Point3D input) {
        return (Point3D) input.clone();
      }
    }, new Function<Vertex, Point3D>() {
      public Point3D apply(Vertex v) {
        if (lockedVertices.contains(v) && v.coords() != null) {
          return v.coords();
        } else {
          return initializer.apply(v);
        }
      }
    });
    this.locationData = CacheBuilder.newBuilder().build(CacheLoader.from(chain));
    //? sets up corresponding {K,V} to ref -> ea. iter resets to: {0,0,0}
    this.offsetData = CacheBuilder.newBuilder().build(CacheLoader.from(chain));
  }

  /**
   * Calculates the density of Vertices in the overall space, then scales them to be placed in a way which makes them 
   * the most readable for the Client (application). Simple mean calculates current density, then uses it to scale and
   * setSize() for the layout with the scaled width and height.
   *
   * @apinote Uses the maximum of Width || Height for Depth per app convention
   */
  private void scaleDimensionsToGraphsetSize() {
    int totalVerts = graph.vertexCount();
    double initWidth = size.getWidth();
    double initHeight = size.getHeight();
    double initDepth = Math.max(initHeight, initWidth);
    double initVol = initWidth * initHeight * initDepth;
    double curDens = (totalVerts * Math.pow(Vertex.RADIUS, 3) / initVol);
    double scale = Math.cbrt(Constables.TARGET_DATA_DENSITY / curDens);
    int scWidth = (int) Math.ceil(initWidth * scale);
    int scHeigt = (int) Math.ceil(initHeight * scale);
    Dimension scDimension = new Dimension(scWidth, scHeigt);
    this.size = scDimension;
  }

  /**
   * Sets the 'physical' constants used in each calculation for the layout, these define the overall shape
   */
  private void initializeLayoutConstants() {
    curIteration = 0;
    temperature = size.getWidth() / Constables.TEMP_MULT;
    forceConst = Math.sqrt(size.getHeight() * size.getWidth() / graph.vertexCount());
    attrConst = forceConst * Constables.ATTR_MULT;
    repConst = forceConst * Constables.REP_MULT;
  }

  /**
   * Gets the offset data from the cache for the provided Vertex
   */
  private Point3D getOffsetData(Vertex v) {
    return offsetData.getUnchecked(v);
  }

  /**
   * Gets the location data from the cache for the provided Vertex
   */
  private Point3D getLocationData(Vertex v) {
    return locationData.getUnchecked(v);
  }

  /**
   * Checks if the provided Vertex's position is locked and should not be moved
   */
  protected boolean isLocked(Vertex v) {
    for (Vertex lockedVertex : lockedVertices) {
      if (lockedVertex.id().equals(v.id())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Gets the largest of the two Dimensions (used for a depth (Z) value throghout app)
   */
  protected double maxDim() {
    return Math.max(size.getWidth(), size.getHeight());
  }

  /**
   * Checks the both provided vertices are in the locked vertex
   */
  private boolean bothVerticesLocked(Vertex v1, Vertex v2) {
    return isLocked(v1) && isLocked(v2);
  }

  /**
   * Update the current offset values for the given vertex using the {x,y,z} values of the provided Point3D
   */
  private void updateOffset(Vertex v, Point3D dispVector) {
    Point3D curOffset = getOffsetData(v);
    double newX = curOffset.getX() + dispVector.getX();
    double newY = curOffset.getY() + dispVector.getY();
    double newZ = curOffset.getZ() + dispVector.getZ();
    curOffset.setLocation(newX, newY, newZ);
  }

  private double xDiff(Point3D p1, Point3D p2) {
    return p1.getX() - p2.getX();
  }

  private double yDiff(Point3D p1, Point3D p2) {
    return p1.getY() - p2.getY();
  }

  private double zDiff(Point3D p1, Point3D p2) {
    return p1.getZ() - p2.getZ();
  }

  /**
   * Called once per-step() this simulates the physical 'annealing' process by decreasing overall temperature as
   * the algorithim steps, allowing for less overall movement and a measure of 'completeness'
   */
  private void cool() {
    temperature *= (1.0 - curIteration / (double) Constables.MAX_ITERS);
    if (curIteration % 100 == 0) {
      adjustForceConstants();
    }
  }

  /**
   * Called every 100 iterations to adjust forces based on how the overall layout is proceeding, overall leads to 
   * more consistent layout performance by tweaking each value to force constant to nudge the layout towards balance
   */
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

  /**
   * The average of all repulsive forces between each Vertex used to assess
   * current status of the layout for dynamic adjustment during the layout step process
   */
  private double calculateAverageRepulsionForce() {
    double totalRepulsionForce = 0;
    int count = 0;
    Set<Vertex> verts = graph.vertices();
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

  /**
   * The average of all attractive forces between each existing Edge's endpoints used to assess 
   * current status of the layout for dynamic adjustment during the layout step process
   */
  private double calculateAverageAttractionForce() {
    double totalAttractionForce = 0;
    int count = 0;
    Set<Edge> eds = graph.edges();

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
