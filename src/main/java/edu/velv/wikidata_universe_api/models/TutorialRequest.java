package edu.velv.wikidata_universe_api.models;

import edu.velv.wikidata_universe_api.errors.Err;
import edu.velv.wikidata_universe_api.errors.Err.WikiverseServiceError.TutorialSlideDataUnavailableError;
import edu.velv.wikidata_universe_api.services.WikidataServiceManager;
import edu.velv.wikidata_universe_api.interfaces.Loggable;

import io.vavr.control.Either;

import java.awt.Dimension;
import java.util.Optional;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;

public class TutorialRequest implements Loggable {
  private String message;
  protected Dimension dimensions;
  protected Graphset graph;
  protected LayoutConfig layoutConfig = new LayoutConfig();

  private final WikidataServiceManager wikidata;
  private Map<String, AboutSlide> slideData;

  private static final String SLIDE_DATA_PATH = "src/main/java/edu/velv/wikidata_universe_api/models/tutorial_slide_data.json";

  public TutorialRequest(WikidataServiceManager wd) {
    this.message = null;
    this.dimensions = new Dimension(1600, 1200);
    this.graph = new Graphset();
    this.wikidata = wd;
  }

  public String message() {
    return message;
  }

  public Dimension dimensions() {
    return dimensions;
  }

  public Graphset graph() {
    return graph;
  }

  public LayoutConfig layoutConfig() {
    return layoutConfig;
  }

  public Either<Err, RequestResponseBody> getSlide(String tgtSlide) {
    try {
      slideData = loadSlides(SLIDE_DATA_PATH);
      int stageNumber = Integer.parseInt(tgtSlide);
      Method method = this.getClass().getDeclaredMethod("slide" + stageNumber);
      method.setAccessible(true);
      method.invoke(this);
    } catch (Exception e) {
      return Either.left(new TutorialSlideDataUnavailableError("Unable to get slide data for AboutSketch", e));
    }
    // set unique message response per slide //
    this.message = slideData.get(tgtSlide).toString();
    return Either.right(new RequestResponseBody(this));
  }

  private void slide1() {
    Vertex vert = createVert("Q405", "Moon",
        "Earth's only natural satellite",
        new Point3D());
    vert.setAsOrigin();
    this.graph().addVertex(vert);
  }

  private void slide2() {
    Vertex vert = createVert("Q106428", "Apollo 13",
        "1985 film by Ron Howard", new Point3D(-200, 0, 0));
    this.graph().addVertex(vert);
  }

  private void slide3() {
    Vertex vert = createVert("Q3454165", "Kevin Bacon", "American actor (born 1958)", new Point3D(-400, -100, 78));
    this.graph().addVertex(vert);
  }

  private void slide4() {
    // no data creation needed
  }

  private void slide5() {
    Vertex vert = kbOriginVert();
    this.graph().addVertex(vert);
  }

  private Optional<Err> slide6() {
    Optional<Err> kbResultsTask = getInitKevinBaconResult();
    return kbResultsTask.isPresent() ? Optional.of(kbResultsTask.get()) : Optional.empty();
  }

  private void slide7() {
    // no data creation needed
  }

  private void slide8() {
    Vertex vert = kbOriginVert();
    this.graph().addVertex(vert);

    Vertex actor = createVert("Q33999", "actor",
        "person who acts in a dramatic or comic production and works in film, television, theatre, or radio",
        new Point3D(-150, 25, 25));
    this.graph().addVertex(actor);

    Edge edge = new Edge();
    edge.srcId("Q3454165");
    edge.tgtId("Q33999");
    edge.propertyId("P106");
    edge.fetched(true);
    this.graph().addEdge(edge);

    Property property = new Property();
    property.id("P106");
    property.label("occupation");
    property.description(
        "occupation of a person; see also \"field of work\" (Property:P101), \"position held\" (Property:P39)");
    property.fetched(true);
    this.graph().addProperty(property);
  }

  private void slide9() {
    // no data creation needed
  }

  private void slide10() {
    Vertex vert = kbOriginVert();
    this.graph().addVertex(vert);
  }

  private Optional<Err> slide11() {
    Optional<Err> kbResultsTask = getInitKevinBaconResult();
    return kbResultsTask.isPresent() ? Optional.of(kbResultsTask.get()) : Optional.empty();
  }

  private void slide12() {
    // no data creation needed
  }

  private void slide13() {
    // no data creation needed
  }

  private Vertex kbOriginVert() {
    Vertex vert = createVert("Q3454165", "Kevin Bacon", "American actor (born 1958)", new Point3D(0, 0, 0));
    vert.setAsOrigin();
    return vert;
  }

  /**
   * Helper to create a vertex using the provided details and position
   */
  private Vertex createVert(String id, String label, String desc, Point3D pos) {
    Vertex vert = new Vertex();
    vert.id(id);
    vert.label(label);
    vert.description(desc);
    vert.coords(pos);
    vert.fetched(true);
    return vert;
  }

  /**
   * Helper to call the complete cycle needed to build the example graphset used 'Kevin Bacon'
   */
  private Optional<Err> getInitKevinBaconResult() {
    ClientRequest request = new ClientRequest(wikidata, "Kevin Bacon");

    Optional<Err> curTask = wikidata.fetchInitialQueryData(request);
    if (curTask.isPresent()) {
      return Optional.of(curTask.get());
    }
    request.graph().getOriginVertex().lock(); // sticks KB @ (0,0,0)

    curTask = wikidata.fetchIncompleteData(request);
    if (curTask.isPresent()) {
      return Optional.of(curTask.get());
    }

    curTask = request.runFR3DLayoutProcess();
    if (curTask.isPresent()) {
      return Optional.of(curTask.get());
    }

    this.graph = request.graph();
    this.layoutConfig = request.layoutConfig();
    this.dimensions = request.dimensions();
    return Optional.empty();
  }

  /**
   * @method loadSlide() - called to load the data and slides for the tutorial to construct the response message strings
   */
  private Map<String, AboutSlide> loadSlides(String filePath) throws IOException {
    JsonNode rootNode = mapper.readTree(new File(filePath));
    JsonNode slidesNode = rootNode.path("slides");

    Map<String, AboutSlide> slides = new HashMap<>();
    Iterator<Map.Entry<String, JsonNode>> fields = slidesNode.fields();
    while (fields.hasNext()) {
      Map.Entry<String, JsonNode> field = fields.next();
      String key = field.getKey();
      JsonNode value = field.getValue();
      AboutSlide slide = mapper.treeToValue(value, AboutSlide.class);
      slides.put(key, slide);
    }
    return slides;
  }

}