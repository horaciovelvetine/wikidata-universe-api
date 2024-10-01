package edu.velv.wikidata_universe_api._TestUtils._v1.models;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import edu.velv.wikidata_universe_api._TestUtils.TestDataBuilders;
import edu.velv.wikidata_universe_api.models.FR3DLayout;
import edu.velv.wikidata_universe_api.models.Graphset;

public class FR3DLayoutTest {

  private FR3DLayout layout;
  private Graphset graph;

  // @Test
  // void initializes_simple_fetched_graphset() {
  //   // construct_testable_fetched_data();

  //   for (Vertex vert : graph.vertices()) {
  //     assertEquals(layout.apply(vert), new Point3D(), "Uninitialized Vertices should start with (0,0,0) coordinates");
  //   }

  //   layout.initialize();

  //   for (Vertex vert : graph.vertices()) {
  //     assertNotEquals(layout.apply(vert), new Point3D(),
  //         "Initialized Vertices should not still be positioned @ (0,0,0)");
  //   }

  // }

  // @Test
  // void steps_simple_fetched_graphset() {
  //   // construct_testable_fetched_data();
  //   layout.initialize();

  //   Map<Vertex, Point3D> originalPositions = new HashMap<>();
  //   for (Vertex vert : graph.vertices()) {
  //     Point3D pos = layout.getLocationData(vert);
  //     originalPositions.put(vert, new Point3D(pos.getX(), pos.getY(), pos.getZ()));
  //   }

  //   while (!layout.done()) {
  //     layout.step();
  //   }

  //   for (Entry<Vertex, Point3D> ent : originalPositions.entrySet()) {
  //     assertNotEquals(layout.apply(ent.getKey()), ent.getValue(),
  //         "Vertices should have moved from their originally initialized positions.");
  //   }
  // }

  // @Test
  // void step_ignores_locked_vertices() {
  //   // construct_testable_fetched_data();
  //   Vertex mockOrigin = graph.vertices().stream().findFirst().get();
  //   layout.lock(mockOrigin, true);

  //   layout.initialize();
  //   assertEquals(layout.apply(mockOrigin), new Point3D(),
  //       "The locked Vertex should remain @ (0,0,0) when initialized");

  //   while (!layout.done()) {
  //     layout.step();
  //   }

  //   assertEquals(layout.apply(mockOrigin), new Point3D(),
  //       "The locked Vertex should remain @ (0,0,0) when stepped forward");
  //   layout.lock(false);
  //   assertFalse(layout.isLocked(mockOrigin), "Should unlock Vertex with unlock all call");
  // }

  // private void construct_testable_fetched_data() {
  //   graph = DataBuilder.simpleFetchedGraphset();
  //   layout = new FR3DLayout(new Dimension(500, 500), graph);
  // }
}
