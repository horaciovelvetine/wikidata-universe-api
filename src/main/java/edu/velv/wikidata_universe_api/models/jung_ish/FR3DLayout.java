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
import edu.velv.wikidata_universe_api.errors.Err.LayoutDebug;
import edu.velv.wikidata_universe_api.models.ClientSession;

public class FR3DLayout {
  protected Graphset graph;

  // PHYSICAL CONSTANTS
  protected double attrMult = 0.75; // biasses tendency of vertices to move towards each other
  protected double repMult = 0.75; // biasses tendency of vertices to move away from each other
  protected double forceConst;
  protected double temperature;
  protected double attrConst;
  protected double repConst;
  protected double maxDimension; // a-directional maximum of the layout
  protected Dimension size;
  protected double EPSILON = 0.000001; // avoid division by zero
  // ITERATIVE CONTEXT
  protected boolean initialized = false;
  protected int currentIteration;
  protected int maxIterations = 700;
  protected int iterMvmntMax = 5; // limit on 'unit' movement per iteration
  protected int dithMagMult = 2; // randomize position jitter magnitude 
  protected double borderWidth;
  // 'STATE'
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

  public FR3DLayout(ClientSession session) {
    this.graph = session.graphset();
  }

  public FR3DLayout(Graphset graph, Dimension size) {
    if (graph == null || size == null) {
      throw new IllegalArgumentException("Graph and size must be non-null");
    }
    this.graph = graph;
    this.size = size;
    setAndInitPositions(new RandomLocation3D<Vertex>(size));
    doInit();
  }

  /**
   * Main function of the layout. Given a vertex, returns a set of 3D coordinates.
   */
  public Point3D apply(Vertex v) {
    return getLocationData(v);
  }

  //------------------------------------------------------------------------------------------------------------
  //
  //* GETTERS AND SETTERS * GETTERS AND SETTERS * GETTERS AND SETTERS * GETTERS AND SETTERS * GETTERS AND SETTERS
  //
  //------------------------------------------------------------------------------------------------------------

  public void setGraph(Graphset graph) {
    this.graph = graph;
    if (size != null && graph != null) {
      initialize();
    }
  }

  public Graphset getGraph() {
    return graph;
  }

  public void setSize(Dimension size) {
    if (initialized == false) {
      setAndInitPositions(new RandomLocation3D<Vertex>(size));
    }
    if (size != null && graph != null) {

      Dimension oldSize = getSize();
      this.size = size;
      doInit();

      if (!oldSize.equals(size) && oldSize != null) {
        adjustLocations(oldSize, size);
        maxDimension = Math.max(size.width, size.height);
      }
    }
  }

  public Dimension getSize() {
    return size;
  }

  public void setLocation(Vertex v, Point3D location) {
    Point3D coords = getLocationData(v);
    coords.setLocation(location);
  }

  public void setLocation(Vertex v, double x, double y, double z) {
    Point3D coords = getLocationData(v);
    coords.setLocation(x, y, z);
  }

  public void lock(Vertex v, boolean state) {
    if (state) {
      lockedVertices.add(v);
    } else {
      lockedVertices.remove(v);
    }
  }

  public void lock(boolean lock) {
    for (Vertex v : graph.vertices()) {
      lock(v, lock);
    }
  }

  public boolean isLocked(Vertex v) {
    return lockedVertices.contains(v);
  }

  //------------------------------------------------------------------------------------------------------------
  //
  //!    LAYOUT METHODS * LAYOUT METHODS * LAYOUT METHODS * LAYOUT METHODS * LAYOUT METHODS * LAYOUT METHODS 
  //
  //------------------------------------------------------------------------------------------------------------

  public Optional<Err> initialize() {
    try {
      doInit();
      return Optional.empty();
    } catch (Exception e) {
      return Optional.of(new LayoutDebug(e));
    }
  }

  public void reset() {
    doInit();
  }

  protected void doInit() {
    Graphset graph = getGraph();
    Dimension d = getSize();
    if (graph != null && d != null) {
      currentIteration = 0;
      temperature = d.getWidth() / 10;
      forceConst = Math.sqrt(d.getHeight() * d.getWidth() / graph.getVertexCount());
      attrConst = forceConst * attrMult;
      repConst = forceConst * repMult;
      maxDimension = Math.max(d.width, d.height);
      borderWidth = Math.min(d.width, d.height) / 50;
    }
  }

  public void step() {
    currentIteration++;
    Collection<Vertex> vLoc = getGraph().vertices();
    // REPULSION
    while (true) {
      try {
        for (Vertex v : vLoc) {
          calcRepulsion(v);
        }
        break;
      } catch (ConcurrentModificationException cme) {
        // ignore and retry
      }
    }
    // ATTRACTION
    while (true) {
      try {
        for (Edge e : getGraph().edges()) {
          calcAttraction(e);
        }
        break;
      } catch (ConcurrentModificationException cme) {
        // ignore and retry
      }
    }
    // POSITION
    while (true) {
      try {
        for (Vertex v : vLoc) {
          if (isLocked(v))
            continue;
          calcPositions(v);
        }
        break;
      } catch (ConcurrentModificationException cme) {
        // ignore and retry
      }
    }
    cool();
  }

  public boolean done() {
    if (currentIteration > maxIterations || temperature < 1.0 / maxDimension) {
      return true;
    }
    return false;
  }

  //------------------------------------------------------------------------------------------------------------
  //
  //? FORCE CALCS * FORCE CALCS * FORCE CALCS * FORCE CALCS * FORCE CALCS * FORCE CALCS * FORCE CALCS * FORCE
  //
  //------------------------------------------------------------------------------------------------------------

  protected void calcRepulsion(Vertex v1) {
    Point3D offset = getOffset(v1);
    if (offset == null)
      return;
    offset.setLocation(0, 0, 0); // initialize offset on ea iteration

    try {
      for (Vertex v2 : getGraph().vertices()) {
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

        updateOffset(v1, xDisp, yDisp, zDisp, isLocked(v2)); //if v2 is locked, offset v1 2x
      }
    } catch (ConcurrentModificationException cme) {
      calcRepulsion(v1); // retry
    }
  }

  protected void calcAttraction(Edge e) {
    Optional<Tuple2<Vertex, Vertex>> endpoints = getGraph().getEndpoints(e);
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

    updateOffset(v1, -xDisp, -yDisp, -zDisp, isLocked(v2)); // opposite direction(s)
    updateOffset(v2, xDisp, yDisp, zDisp, isLocked(v1));
  }

  protected void calcPositions(Vertex v) {
    Point3D p = getLocationData(v);
    if (p == null)
      return;
    Point3D offset = getOffset(v);

    Double dl = Math.max(EPSILON, offset.distanceSq(offset));

    double xDisp = offset.getX() / dl * Math.min(dl, temperature);
    double yDisp = offset.getY() / dl * Math.min(dl, temperature);
    double zDisp = offset.getZ() / dl * Math.min(dl, temperature);

    if (Double.isNaN(xDisp) || Double.isNaN(yDisp) || Double.isNaN(zDisp))
      throw new IllegalArgumentException("NaN in position calculation");

    double newX = p.getX() + Math.max(-iterMvmntMax, Math.min(iterMvmntMax, xDisp));
    double newY = p.getY() + Math.max(-iterMvmntMax, Math.min(iterMvmntMax, yDisp));
    double newZ = p.getZ() + Math.max(-iterMvmntMax, Math.min(iterMvmntMax, zDisp));

    double width = getSize().getWidth();
    double height = getSize().getHeight();

    newX = adjustPositionToBorderBox(newX, width);
    newY = adjustPositionToBorderBox(newY, height);
    newZ = adjustPositionToBorderBox(newZ, Math.min(width, height)); // z min of x, y

    p.setLocation(newX, newY, newZ);
  }

  //------------------------------------------------------------------------------------------------------------
  //
  //! PRIVATE METHODS * PRIVATE METHODS * PRIVATE METHODS * PRIVATE METHODS * PRIVATE METHODS * PRIVATE METHODS
  //
  //------------------------------------------------------------------------------------------------------------

  protected Point3D getLocationData(Vertex V) {
    return locationData.getUnchecked(V);
  }

  protected void setAndInitPositions(Function<Vertex, Point3D> initializer) {
    Function<Vertex, Point3D> chain = Functions.<Vertex, Point3D, Point3D>compose(new Function<Point3D, Point3D>() {
      public Point3D apply(Point3D input) {
        return (Point3D) input.clone();
      }
    }, initializer); //build func chain 
    this.locationData = CacheBuilder.newBuilder().build(CacheLoader.from(chain));
    this.offsetData = CacheBuilder.newBuilder().build(CacheLoader.from(chain));
    initialized = true; // set flag
  }

  protected void adjustLocations(Dimension oldSize, Dimension size) {
    int xOff = (size.width - oldSize.width) / 2;
    int yOff = (size.height - oldSize.height) / 2;
    int zOff = (size.height - oldSize.height) / 2;

    while (true) {
      try {
        for (Vertex v : getGraph().vertices()) {
          offsetVertexLocation(v, xOff, yOff, zOff);
        }
        break;
      } catch (ConcurrentModificationException cme) {
        // ignore and retry
      }
    }
  }

  protected Point3D getOffset(Vertex v) {
    return offsetData.getUnchecked(v);
  }

  protected void offsetVertexLocation(Vertex v, double dx, double dy, double dz) {
    Point3D p = getLocationData(v);
    double ox = p.getX() + dx;
    double oy = p.getY() + dy;
    double oz = p.getZ() + dz;
    p.setLocation(ox, oy, oz);
  }

  protected void cool() {
    temperature *= (1.0 - currentIteration / (double) maxIterations);
  }

  protected void updateOffset(Vertex v, double xDisp, double yDisp, double zDisp,
      boolean opposingIsLocked) {
    Point3D offset = getOffset(v);
    int factor = opposingIsLocked ? 2 : 1;
    offset.setLocation(offset.getX() + factor * xDisp, offset.getY() + factor * yDisp,
        offset.getZ() + factor * zDisp);
  }

  protected double adjustPositionToBorderBox(double coordPos, double dimMax) {
    if (coordPos < borderWidth) {
      return borderWidth + Math.random() * dithMagMult * borderWidth;
    } else if (coordPos > dimMax - borderWidth) {
      return dimMax - borderWidth - Math.random() * dithMagMult * borderWidth;
    }
    return coordPos;
  }

}
