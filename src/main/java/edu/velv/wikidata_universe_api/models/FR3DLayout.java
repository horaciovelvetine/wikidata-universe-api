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

import edu.velv.wikidata_universe_api.services.FR3DConfig;
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
  private ClientRequest request;
  // private Graphset graph;
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

  // protected Dimension size;
  protected final FR3DConfig config;
  protected final double EPSILON = 0.000001D; // prevents 0 div
  protected int curIteration;
  protected double temperature;
  protected double forceConst;
  protected double attrConst;
  protected double repConst;

  public FR3DLayout(ClientRequest request, FR3DConfig config) {
    this.request = request;
    this.config = config;
  }

  /**
   * Run prior to stepping the layout: scales the layout dimensions to the Graphset's details, 
   * initializes positions for any unlocked positions, and calculates constants used to simulate
   * the 'physical constants' of the layout itself. 
   */
  public void initialize() {
    scaleDimensionsToGraphsetSize();
    setInitialRandomPositions(new RandomPoint3D<>(request.dimensions()));
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
    return this.getLocationData(v);
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
    for (Vertex v : request.graph().vertices()) {
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
        for (Vertex v : request.graph().vertices()) {
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
        for (Edge e : request.graph().edges()) {
          calcAttraction(e);
        }
        break;
      } catch (ConcurrentModificationException cme) {
      }
    }
    while (true) {
      try {
        for (Vertex v : request.graph().vertices()) {
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
    return curIteration > config.maxIters() || temperature < 1.0 / maxDim();
  }

  //=====================================================================================================================>
  //! FORCE CALCULATIONS...
  //=====================================================================================================================>
  //=====================================================================================================================>
  //=====================================================================================================================>
  //=====================================================================================================================>
  //=====================================================================================================================>

  protected void calcRepulsion(Vertex v) {
    Point3D curOffset = getOffsetData(v);
    curOffset.setLocation(0, 0, 0); //? offset data resets on step here @ start of iteration

    for (Vertex v2 : request.graph().vertices()) {
      if (bothVerticesLocked(v, v2))
        continue;

      Point3D p1 = getLocationData(v);
      Point3D p2 = getLocationData(v2);

      double deltaLen = Math.max(EPSILON, p1.distance(p2));
      double force = (repConst * repConst) / deltaLen;

      if (Double.isNaN(force))
        throw new IllegalArgumentException("NaN in repulsion force calc.");

      double scale = isLocked(v2) ? 2 : 1;
      double xDisp = xDiff(p1, p2) * force;
      double yDisp = yDiff(p1, p2) * force;
      double zDisp = zDiff(p1, p2) * force;

      updateOffset(v, xDisp, yDisp, zDisp, scale);
    }
  }

  //=====================================================================================================================>
  //=====================================================================================================================>

  protected void calcAttraction(Edge e) {
    Optional<Tuple2<Vertex, Vertex>> endpoints = request.graph().getEndpoints(e);
    if (endpoints.isEmpty())
      return;

    Vertex v1 = endpoints.get()._1();
    Vertex v2 = endpoints.get()._2();
    if (bothVerticesLocked(v1, v2))
      return;

    Point3D p1 = getLocationData(v1);
    Point3D p2 = getLocationData(v2);

    double dl = Math.max(EPSILON, p1.distance(p2));
    double force = dl / attrConst;

    if (Double.isNaN(force))
      throw new IllegalArgumentException("NaN in repulsion force calc.");

    double xDisp = xDiff(p1, p2) * force;
    double yDisp = yDiff(p1, p2) * force;
    double zDisp = zDiff(p1, p2) * force;

    if (!isLocked(v1)) {
      int scale = isLocked(v2) ? 2 : 1;
      updateOffset(v1, xDisp, yDisp, zDisp, scale);
    }

    if (!isLocked(v2)) {
      int scale = isLocked(v1) ? 2 : 1;
      updateOffset(v2, xDisp, yDisp, zDisp, scale);
    }

  }

  //=====================================================================================================================>
  //=====================================================================================================================>

  protected void calcPosition(Vertex v) {
    Point3D loc = getLocationData(v);
    Point3D off = getOffsetData(v);

    double deltaLen = Math.max(EPSILON,
        Math.sqrt((off.getX() * off.getX()) + (off.getY() * off.getY()) + (off.getZ() * off.getZ())));
    double xDisp = off.getX() / deltaLen * Math.min(deltaLen, temperature);
    double yDisp = off.getY() / deltaLen * Math.min(deltaLen, temperature);
    double zDisp = off.getZ() / deltaLen * Math.min(deltaLen, temperature);

    if (Double.isNaN(xDisp) || Double.isNaN(yDisp) || Double.isNaN(zDisp))
      throw new IllegalArgumentException("NaN value found in update position displacement calcs");

    double cX = loc.getX() + clampToMaxIterMvmnt(xDisp);
    double cY = loc.getY() + clampToMaxIterMvmnt(yDisp);
    double cZ = loc.getZ() + clampToMaxIterMvmnt(zDisp);

    loc.setLocation(clampNewPositionsToDimensions(cX, cY, cZ));
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
  protected void setInitialRandomPositions(Function<Vertex, Point3D> initializer) {
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
  protected void scaleDimensionsToGraphsetSize() {
    if (!request.graph().isEmpty()) {
      int totalVerts = request.graph().vertexCount();
      double initWidth = request.dimensions().getWidth();
      double initHeight = request.dimensions().getHeight();
      double initDepth = Math.max(initHeight, initWidth);
      double initVol = initWidth * initHeight * initDepth;
      double curDens = (totalVerts * Math.pow(Vertex.RADIUS, 3) / initVol);
      double scale = Math.cbrt(config.targetDensity() / curDens);
      int scWidth = (int) Math.ceil(initWidth * scale);
      int scHeigt = (int) Math.ceil(initHeight * scale);

      request.dimensions(new Dimension(scWidth, scHeigt));
    }
  }

  /**
   * Sets the 'physical' constants used in each calculation for the layout, these define the overall shape
   */
  protected void initializeLayoutConstants() {
    curIteration = 0;
    temperature = request.dimensions.getWidth() / config.tempMult();
    forceConst = Math
        .sqrt(request.dimensions.getHeight() * request.dimensions.getWidth() / request.graph().vertexCount());
    attrConst = forceConst * config.attrMult();
    repConst = forceConst * config.repMult();
  }

  /**
   * Gets the offset data from the cache for the provided Vertex
   */
  protected Point3D getOffsetData(Vertex v) {
    return offsetData.getUnchecked(v);
  }

  /**
   * Gets the location data from the cache for the provided Vertex
   */
  protected Point3D getLocationData(Vertex v) {
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
    return Math.max(request.dimensions().getWidth(), request.dimensions().getHeight());
  }

  /**
   * Checks the both provided vertices are in the locked vertex
   */
  protected boolean bothVerticesLocked(Vertex v1, Vertex v2) {
    return isLocked(v1) && isLocked(v2);
  }

  /**
   * Update the current offset values for the given vertex using the displacement values provided
   * scaling in to oppose forces on an opposing locked vertex - still allowing the full intended adjustment.
   * @param Vertex
   * @param displacement of X 
   * @param displacement of Y
   * @param displacement of Z 
   */
  protected void updateOffset(Vertex v, double dx, double dy, double dz, double scale) {
    Point3D curOffset = getOffsetData(v);
    double newX = curOffset.getX() - (scale * dx);
    double newY = curOffset.getY() - (scale * dy);
    double newZ = curOffset.getZ() - (scale * dz);
    curOffset.setLocation(newX, newY, newZ);
  }

  protected double xDiff(Point3D p1, Point3D p2) {
    return p1.getX() - p2.getX();
  }

  protected double yDiff(Point3D p1, Point3D p2) {
    return p1.getY() - p2.getY();
  }

  protected double zDiff(Point3D p1, Point3D p2) {
    return p1.getZ() - p2.getZ();
  }

  /**
   * Called once per-step() this simulates the physical 'annealing' process by decreasing overall temperature as
   * the algorithim steps, allowing for less overall movement and a measure of 'completeness'
   */
  protected void cool() {
    temperature *= (1.0 - curIteration / (double) config.maxIters());
    if (curIteration % 25 == 0) {
      adjustForceConstants();
    }
  }

  /**
   * Called every 100 iterations to adjust forces based on how the overall layout is proceeding, overall leads to 
   * more consistent layout performance by tweaking each value to force constant to nudge the layout towards balance
   */
  protected void adjustForceConstants() {
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
  protected double calculateAverageRepulsionForce() {
    double totalRepulsionForce = 0;
    int count = 0;
    Set<Vertex> verts = request.graph().vertices();
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
  protected double calculateAverageAttractionForce() {
    double totalAttractionForce = 0;
    int count = 0;
    Set<Edge> eds = request.graph().edges();

    for (Edge e : eds) {
      Optional<Tuple2<Vertex, Vertex>> endpoints = request.graph().getEndpoints(e);
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

  /**
   * Clamps the magnitude of the movement allowed on any iteration to the set constant  
   */
  protected double clampToMaxIterMvmnt(double disp) {
    return Math.max(-config.maxIterMvmnt(), Math.min(config.maxIterMvmnt(), disp));
  }

  /**
  * Clamps the given value to the dimensions and return a new Point inside the dimensions boundaries
  */
  protected Point3D clampNewPositionsToDimensions(double newX, double newY, double newZ) {
    double maxX = request.dimensions().getWidth();
    double maxY = request.dimensions().getHeight();
    double maxZ = Math.max(maxX, maxY);

    newX = Math.max(-maxX, Math.min(maxX, newX));
    newY = Math.max(-maxY, Math.min(maxY, newY));
    newZ = Math.max(-maxZ, Math.min(maxZ, newZ));

    return new Point3D(newX, newY, newZ);
  }

}
