package edu.velv.wikidata_universe_api.models;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import edu.velv.wikidata_universe_api._Utils.FailedTestMessageTemplates;
import edu.velv.wikidata_universe_api._Utils.TestDataBuilders;
import edu.velv.wikidata_universe_api.services.FR3DConfig;
import edu.velv.wikidata_universe_api.services.WikidataServiceManager;

@SpringBootTest
public class FR3DLayoutTests implements FailedTestMessageTemplates, TestDataBuilders {
  private final String src_ = "@FR3DLayoutTests:: ";

  @Autowired
  FR3DConfig config;

  @Autowired
  WikidataServiceManager wikidataSrvc;
  /**
   * @apiNote The ClientRequest/Layout relationship is to allow continued use of the more performant Guava.LoadingCache inside the otherwise @Service Layout algo 
   */
  ClientRequest genReqData;

  @BeforeEach
  void initializeGenericClientRequestData() {
    genReqData = new ClientRequest(wikidataSrvc, config, buildGenericRequestPayload());
    allVerticesPositionedAtOrigin();
  }

  @Test
  void constructs_with_default_graphset_using_spring_context() {
    assertNotNull(genReqData.layout(), src_ + initedWO + "a Layout()");
  }

  @Test
  void scaleDimensionsToGraphsetSize_scales_dimensions_to_config_value() {
    genReqData.layout().scaleDimensionsToGraphsetSize();

    assertNotEquals(
        buildGenericDimensions(), genReqData.dimensions(),
        src_ + "Layout.dimensions()" + shouldNotBeEq + "to the generic starting Dimensions");
    assertEquals(genReqData.dimensions().getHeight(), genReqData.dimensions().getWidth(),
        src_ + "Scaled Layout.dimensions()" + shouldBeEq + "for 1:1 (generic) aspect dimensions");
  }

  @Test
  void setInitialRandomPositions_initializes_unlocked_vertices() {
    genReqData.layout().setInitialRandomPositions(new RandomPoint3D<>(genReqData.dimensions()));
    genReqData.graph().updateVertexCoordinatesFromLayout(genReqData.layout());

    for (Vertex gnVert : genReqData.graph().vertices()) {
      assertNotEquals(new Point3D(), gnVert.coords(),
          src_ + vert + "coordinates " + shouldNotBeEq + "to (0,0,0) after" + inited);
    }

  }

  @Test
  void setInitialRandomPositions_ignores_locked_vertex() {
    Optional<Vertex> lockVert = genReqData.graph().getVertexById("Q1");
    if (lockVert.isPresent()) {
      genReqData.layout().lock(lockVert.get(), true);

      genReqData.layout().setInitialRandomPositions(new RandomPoint3D<>(genReqData.dimensions()));
      genReqData.graph().updateVertexCoordinatesFromLayout(genReqData.layout());

      for (Vertex gnVert : genReqData.graph().vertices()) {
        if (lockVert.get().equals(gnVert)) {
          assertEquals(new Point3D(), gnVert.coords(), src_ + vert + shouldBe + "locked to (0,0,0)");
        } else {
          assertNotEquals(new Point3D(), gnVert.coords(),
              src_ + vert + "coordinates " + shouldNotBeEq + "to (0,0,0) after" + inited);
        }
      }

      genReqData.layout().lock(lockVert.get(), false);
      genReqData.layout().setInitialRandomPositions(new RandomPoint3D<>(genReqData.dimensions()));
      genReqData.graph().updateVertexCoordinatesFromLayout(genReqData.layout());
      assertNotEquals(new Point3D(), lockVert.get().coords(), src_ + vert + "coordinates " + shouldBe + "changed");
    }

  }

  @Test
  void setInitialRandomPositions_ignores_all_vertices_locked() {
    genReqData.layout().lock(true);
    genReqData.layout().setInitialRandomPositions(new RandomPoint3D<>(genReqData.dimensions()));
    genReqData.graph().updateVertexCoordinatesFromLayout(genReqData.layout());
    allVerticesPositionedAtOrigin();
  }

  @Test
  void initializeLayoutConstants_creates_sim_force_values() {
    genReqData.layout().initialize();

    assertNotNull(genReqData.layout().forceConst, src_ + "force" + shouldNotBe + empty);
    assertNotNull(genReqData.layout().attrConst, src_ + "attractive force" + shouldNotBe + empty);
    assertNotNull(genReqData.layout().repConst, src_ + "repulsion force" + shouldNotBe + empty);
    assertNotNull(genReqData.layout().temperature, src_ + "temperature" + shouldNotBe + empty);
    assertEquals(0, genReqData.layout().curIteration, src_ + should + "start on iteration 0");
  }

  @Test
  void stepping_layout_changes_calcs_new_location_for_unlocked_vertices() {
    allVerticesPositionedAtOrigin();
    genReqData.layout().initialize();

    Map<Vertex, Point3D> initialPositions = new HashMap<>();
    for (Vertex v : genReqData.graph().vertices()) {
      Point3D initPos = genReqData.layout().apply(v);
      double iX = initPos.getX();
      double iY = initPos.getY();
      double iZ = initPos.getZ();

      initialPositions.put(v, new Point3D(iX, iY, iZ));
    }

    genReqData.layout().step();

    for (Vertex v : genReqData.graph().vertices()) {
      if (!genReqData.layout().isLocked(v)) {
        Point3D nP = genReqData.layout().apply(v);
        Point3D iP = initialPositions.get(v);

        assertNotEquals(iP, nP, src_ + vert + "coordinates " + shouldNotBeEq + "after .step()");
      }
    }
  }

  @Test
  void running_layout_maintains_locked_vertices_positions() {
    allVerticesPositionedAtOrigin();
    Vertex vert = genReqData.graph().getVertexById("Q1").get();
    genReqData.layout().lock(vert, true); // lock vert as origin
    genReqData.layout().initialize();

    assertEquals(vert.coords(), genReqData.layout().apply(vert),
        src_ + "locked" + vert + should + "remain at (0,0,0)");

    while (!genReqData.layout().done()) {
      genReqData.layout().step();
    }

    assertEquals(vert.coords(), genReqData.layout().apply(vert),
        src_ + "locked" + vert + should + "remain at (0,0,0)");

  }

  private void allVerticesPositionedAtOrigin() {
    genReqData.graph.vertices.forEach(gnVert -> {
      assertEquals(new Point3D(), gnVert.coords(),
          src_ + vert + "coordinates " + shouldBeEq + " to (0,0,0) pre-" + init);
    });
  }
}
