package edu.velv.wikidata_universe_api.models;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import edu.velv.wikidata_universe_api.services.FailedTestMsgTemplates;
import edu.velv.wikidata_universe_api.services.WikidataServiceManager;
import edu.velv.wikidata_universe_api.services.WikidataTestDataBuilders;

@SpringBootTest
public class FR3DLayoutTests implements FailedTestMsgTemplates, WikidataTestDataBuilders {
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
  // void initializeGenericClientRequestData() {
  //   genReqData = new ClientRequest(wikidataSrvc, config, buildGenericRequestPayload());
  //   allVerticesPositionedAtOrigin();
  // }

  // @Test
  // void constructs_with_default_graphset_using_spring_context() {
  //   assertNotNull(genReqData.layout(), src_ + initedWO + "a Layout()");
  // }

  // @Test
  // void scaleDimensionsToGraphsetSize_scales_dimensions_to_config_value() {
  //   genReqData.layout().scaleDimensionsToGraphsetSize();

  //   assertNotEquals(
  //       buildDimensions_generic(), genReqData.dimensions(),
  //       src_ + "Layout.dimensions()" + shouldNotBeEq + "to the generic starting Dimensions");
  //   assertEquals(genReqData.dimensions().getHeight(), genReqData.dimensions().getWidth(),
  //       src_ + "Scaled Layout.dimensions()" + shouldBeEq + "for 1:1 (generic) aspect dimensions");
  // }

  // @Test
  // void setInitialRandomPositions_initializes_unlocked_vertices() {
  //   assertFalse(genReqData.graph().vertexCoordsUniqueForEach(), src_ + expected + "each Vertex to start at (0,0,0)");
  //   genReqData.layout().initialize();
  //   genReqData.graph().updateVertexCoordinatesFromLayout(genReqData.layout());

  //   assertTrue(genReqData.graph.vertexCoordsUniqueForEach(),
  //       src_ + expected + "each Vertex to have unique Coordinates");
  // }

  // @Test
  // void setInitialRandomPositions_ignores_locked_vertex() {
  //   Optional<Vertex> lockVert = genReqData.graph().getVertexById("Q1");
  //   if (lockVert.isPresent()) {
  //     genReqData.layout().lock(lockVert.get(), true);
  //     genReqData.layout().initialize(); //==> calls setInitialRandomPositions...
  //     genReqData.graph().updateVertexCoordinatesFromLayout(genReqData.layout());

  //     assertTrue(genReqData.graph().vertexCoordsUniqueForEach(),
  //         src_ + expected + "each Vertex to have unique Coordinates");
  //     assertTrue(vertexCoordsAreZeroes(lockVert.get()), src_ + expected + "the locked Vertex to be located @ (0,0,0)");
  //   }

  // }

  // @Test
  // void setInitialRandomPositions_ignores_all_vertices_locked() {
  //   genReqData.layout().lock(true);
  //   genReqData.layout().setInitialRandomPositions(new RandomPoint3D<>(genReqData.dimensions()));
  //   genReqData.graph().updateVertexCoordinatesFromLayout(genReqData.layout());
  //   allVerticesPositionedAtOrigin();
  // }

  // @Test
  // void initializeLayoutConstants_creates_sim_force_values() {
  //   genReqData.layout().initialize();

  //   assertNotNull(genReqData.layout().forceConst, src_ + "force" + shouldNotBe + empty);
  //   assertNotNull(genReqData.layout().attrConst, src_ + "attractive force" + shouldNotBe + empty);
  //   assertNotNull(genReqData.layout().repConst, src_ + "repulsion force" + shouldNotBe + empty);
  //   assertNotNull(genReqData.layout().temperature, src_ + "temperature" + shouldNotBe + empty);
  //   assertEquals(0, genReqData.layout().curIteration, src_ + should + "start on iteration 0");
  // }

  // @Test
  // void stepping_layout_changes_calcs_new_location_for_unlocked_vertices() {
  //   genReqData.layout().initialize();

  //   Map<Vertex, Point3D> initPositions = new HashMap<>();
  //   for (Vertex v : genReqData.graph().vertices()) {
  //     Point3D initPos = genReqData.layout().apply(v);
  //     double iX = initPos.getX();
  //     double iY = initPos.getY();
  //     double iZ = initPos.getZ();

  //     initPositions.put(v, new Point3D(iX, iY, iZ));
  //   }

  //   genReqData.layout().step();

  //   for (Vertex v : genReqData.graph().vertices()) {
  //     if (!genReqData.layout().isLocked(v)) {
  //       Point3D nP = genReqData.layout().apply(v);
  //       Point3D iP = initPositions.get(v);

  //       assertNotEquals(iP, nP, src_ + vert + "coordinates " + shouldNotBeEq + "after .step()");
  //     }
  //   }
  // }

  // @Test
  // void running_layout_maintains_locked_vertices_positions() {

  //   Vertex lockVert = genReqData.graph().getVertexById("Q1").get();
  //   genReqData.layout().lock(lockVert, true); // lock vert at (0,0,0);
  //   genReqData.layout().initialize();
  //   genReqData.graph().updateVertexCoordinatesFromLayout(genReqData.layout());

  //   assertTrue(genReqData.graph().vertexCoordsUniqueForEach(),
  //       src_ + expected + "each Vertex to have unique Coordinates");
  //   assertTrue(vertexCoordsAreZeroes(lockVert), src_ + expected + "the locked Vertex to be located @ (0,0,0)");

  //   while (!genReqData.layout().done()) {
  //     genReqData.layout().step();
  //   }

  //   assertTrue(genReqData.graph().vertexCoordsUniqueForEach(),
  //       src_ + expected + "each Vertex to have unique Coordinates");
  //   assertTrue(vertexCoordsAreZeroes(lockVert), src_ + expected + "the locked Vertex to be located @ (0,0,0)");
  // }

  /**
   * Iterates over all vertices in the genRequestData and checks they are all at (0,0,0)
   */
  private void allVerticesPositionedAtOrigin() {
    genReqData.graph.vertices.forEach(gnVert -> {
      assertEquals(new Point3D(), gnVert.coords(),
          src_ + vert + "coordinates " + shouldBeEq + " to (0,0,0) pre-" + init);
    });
  }

  /**
   * @return true if the provided Vertex's coordinates are at (0,0,0)
   */
  private boolean vertexCoordsAreZeroes(Vertex vert) {
    return vert.coords().equals(new Point3D());
  }
}
