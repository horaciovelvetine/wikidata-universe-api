package edu.velv.wikidata_universe_api.models;

import java.awt.Dimension;
import java.util.Optional;

import edu.velv.wikidata_universe_api.errors.Err;
import edu.velv.wikidata_universe_api.services.WikidataServiceManager;
import io.vavr.control.Either;

public class AboutRequest {
  private String message;
  protected Dimension dimensions;
  protected Graphset graph;
  protected LayoutConfig layoutConfig = new LayoutConfig();

  private final WikidataServiceManager wikidata;

  public AboutRequest(WikidataServiceManager wd) {
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

  public Either<Err, RequestResponseBody> getStage(String tgtStage) {

    switch (tgtStage) {
      case "2":
        slide2();
        break;
      case "3":
        slide3();
        break;
      case "4":
        slide4();
        break;
      case "5":
        slide5();
        break;
      case "6":
        Optional<Err> s6FetchErr = slide6();
        if (s6FetchErr.isPresent())
          return Either.left(s6FetchErr.get());
        break;
      case "7":
        slide7();
        break;
      case "8":
        slide8();
        break;
      case "9":
        slide9();
        break;
      case "10":
        Optional<Err> s10FetchErr = slide10();
        if (s10FetchErr.isPresent())
          return Either.left(s10FetchErr.get());
        break;
      case "11":
        slide11();
        break;
      default:
        slide1();
        break;
    }
    return Either.right(new RequestResponseBody(this));
  }

  /**
   * PRIVATE METHODS FOR ALL THE SLIDE DETAILS
   * @apiNote - each slide sets up the next by providing a clickable action entity, while simultaneously providing the string for the currently selected entity
   */
  private void slide1() {
    Vertex vert = createVert("Q405", "Moon",
        "Earth's only natural satellite",
        new Point3D());
    vert.setAsOrigin();
    this.graph().addVertex(vert);
    this.message = "intro::a quick intro::Wikiverse is built to explore Wikipedia data as if it existed in its own 3D world. This tutorial will introduce you to a unique way to explore topics in Wikipedia by putting you inside of your topic and giving you the freedom to move around and explore. Start by searching for anything - Wikiverse finds it and draws it in front of you and that’s only the start. Wikiverse is built to explore, and what better way is there to explore than to just look around. Around you, Wikiverse places all the things it knows related to your topic, and with just a click you can explore related ideas, things, people, places, and stuff you never knew.::A new Vertex (the cube) appeared on screen, click on it when you're ready to continue";
  }

  private void slide2() {
    Vertex vert = createVert("Q238651", "world peace",
        "ideal of freedom, peace, and happiness among and within all nations and people", new Point3D(-200, 0, 0));
    this.graph().addVertex(vert);
    this.message = "vertices::the stuff Wikidata is made of::This is a Vertex - clicking selects it displaying its name and a short description on the lower left part of your screen to give you a few details about it. Since all the data is from Wikidata clicking on the title will take you to the source to read more and find links to other Wikimedia Projects featuring this information. The Vertex you clicked on in this case is 'the Moon' - our nearest neighbor in the Solar System, but a Vertex can be anything you can find inside Wikipedia.::Click on the new Vertex (cube!) added on your left";

  }

  private void slide3() {
    Vertex vert = createVert("Q3454165", "Kevin Bacon", "American actor (born 1958)", new Point3D(-400, -100, 78));
    this.graph().addVertex(vert);
    this.message = "vertices::an idea::While this Vertex looks the exact same as the previous one, it's not, instead it's the idea of 'world peace'. Remember a Vertex could be anything you can find in Wikipedia, what makes a Vertex unique is where it's put in 3D space. This is the perfect chance to get a look around and get a feel for just what '3D space' means. Use the mouse to take control and do a little bit of exploring: When you click on a Vertex your screen will focus on it, but if you left click and drag your mouse you can move yourself around whatever is currently selected.::Try looking around, spot the new Vertex and click on it to continue (look up!)";
  }

  private void slide4() {
    this.message = "vertices::a person::Finally, this Vertex - Kevin Bacon (the actor) - is a person. Each Vertex is a separate thing on the screen, and each one has its own unique position in 3D space around you. As you explore and more things are added on screen you can use the scroll wheel to zoom in and out to get a new perspective on the topic you're exploring. Once you have the hang of zooming and using the controls to explore a bit, the tutorial will continue guiding you through your first search.::press the space bar once you're ready to continue";
  }

  private void slide5() {
    Vertex vert = kbOriginVert();
    this.graph().addVertex(vert);
    this.message = "statements::connecting the dot... cubes!::So far Vertices have existed on their own, independently floating in 3D space, but the Wikiverse is built to explore topics and their relationships. When you search for something the Wikiverse places the result directly in front of you, but then it continues the search by getting anything it knows which is related to your topic and drawing them on screen around the original topic. To keep track of and describe these relationships the Wikiverse uses the very humble statement which has a very simple convention: e.g. 'Kevin Bacon is an Actor'. Statements are always: a source 'Kevin Bacon', a property 'occupation of', and a target 'Actor'. This paradigm provides a handy way to describe relationships and is the foundational concept underpinning how vertices are connected in the Wikiverse.::click on the Vertex to see the search results";
  }

  /**
   * @apiNote - breaks the convention by serving the data we're going to explore in the same slide as the message intended to accompany it
   */
  private Optional<Err> slide6() {
    Optional<Err> kbResultsTask = getInitKevinBaconResult();

    this.message = "statements::first results::These are the full results when searching for Kevin Bacon using the Wikiverse. The original Kevin Bacon Vertex is in the middle and drawn all around are each of the related topics the Wikiverse knows about, hovering close by just waiting to be discovered. Click around and see if there's anything you weren't expecting to find - As you click on Vertices, you'll notice some new details appearing in the bottom right of your screen, you can ignore that for now and we'll circle back. Each time you select a new Vertex the Wikiverse will display all the statements currently loaded which connect to and from the currently selected Vertex. These statements are drawn on screen as lines which literally connect the corresponding Vertices in 3D space, and as these lines pull related Vertices towards one another, they naturally push unrelated Vertices away crisscrossing in 3D space to form the web of context for you to explore. To learn a bit more let's simplify things and focus on the original example 'Kevin Bacon is an Actor'.::when you're done exploring and ready to continue press the spacebar";

    return kbResultsTask.isPresent() ? Optional.of(kbResultsTask.get()) : Optional.empty();
  }

  /**
   * @apiNote - Similar to slide6 uses the data provided in the request to accompany the on-screen message instead of pre-setting vertices to click on to advance
   */
  private void slide7() {
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
    this.graph().addEdge(edge);

    Property property = new Property();
    property.id("P106");
    property.label("occupation");
    property.description(
        "occupation of a person; see also \"field of work\" (Property:P101), \"position held\" (Property:P39)");
    this.graph().addProperty(property);

    this.message = "statements::Kevin Bacon is an Actor::Statements describe all the relationship's the Wikiverse knows and show's you while you're exploring topics. Following this simple convention: a source 'Kevin Bacon', a property 'occupation of', and a target 'Actor' - using this model the Wikiverse can store a lot about the nature of how complex topics are connected. While exploring the previous full results, and now in this simplified example you'll see these statements (lines) have been drawn in 3 colors: red, blue, and purple. Each color represents the direction that statement flows, starting from the source ('Kevin Bacon'), through the property ('occupation of'), and ending at the target ('Actor'). The line will be colored red when that statement is flowing out from the currently selected vertex, colored blue when the statement flows in, and purple when 2 matching statements connect a single pair of topics.::click on the 'Actor' vertex to continue";
  }

  private void slide8() {
    this.message = "statements::Actor is not Kevin Bacon::After selecting Actor, you'll notice that the color of line connecting Kevin Bacon and Actor has changed to blue. This is because statements have a natural direction, and this direction is determined using whatever Vertex is currently selected. While Kevin Bacon may be an Actor, Actor is Kevin Bacon doesn't work the same way. Statements are also listed in the bottom right hand of your screen with their source pictured as the yellow 'currently selected' Vertex icon, the property highlighted in the directional color, an arrow to indicate this direction, and finally the title of the other Vertex defined by the Edge. Clicking inside this display on Either of the Vertex icons will move your focus to that position in the Wikiverse, and clicking the property will take you to the Wikidata source page to learn more about how this pair of Vertices is connection is described (the property).::when you're ready, press space bar to continue";
  }

  private void slide9() {
    Vertex vert = kbOriginVert();
    this.graph().addVertex(vert);
    this.message = "the Wikiverse::exploring in 3D::Using Statements and Vertices, the Wikiverse finds and places the context of your search around you. Exploring topics in their wider context, ignorant to what you might to discover along the way is central to the Wikiverse, and that leaves just one thing left to understand about how exploring topics in the Wikiverse. Each time you click on a topic related topics are found and added to your current search, placed according to their own statements (possibly) even connecting them back to already existing vertices in your search.::click on the Vertex when you're ready to start exploring the Wikiverse";
  }

  private Optional<Err> slide10() {
    Optional<Err> kbResultsTask = getInitKevinBaconResult();
    this.message = "this Wikiverse::click to explore::Back at the original results for 'Kevin Bacon' try clicking on any of the related vertices but stay zoomed out a little bit and after a moment or two new vertices (topics) are added for you to explore. Hovering your mouse over any Vertex will display its details in the upper left corner of the display, but as soon as you click, the Wikiverse will add the related topics to your search’s wider contextual universe.::press spacebar to continue";
    return kbResultsTask.isPresent() ? Optional.of(kbResultsTask.get()) : Optional.empty();
  }

  private void slide11() {
    this.message = "settings::a few final details::There are only a few more details for you to understand before jumping in to explore with both feet. In the upper right-hand corner of your screen are two icons: the magnifying glass will let you start over fresh with a new search, and the gear icon provides a few extra tools and tweaks to modify what's on screen. Options include: a data summary display - toggling this will put the number of Vertices and Statements currently on screen, as well as where you're currently looking and positioned in 3D (cartesian [X, Y, Z]) space. Bounding Box and Axis display - toggling these will draw some helpful indicators on screen to help keep you oriented in your search. The final settings are experimental features which re-run the layout process which changes the position of all the Vertices - they can be a discombobulating as they reposition everything in the current search.::press the space bar to exit back to the home page";
  }

  private Vertex kbOriginVert() {
    Vertex vert = createVert("Q3454165", "Kevin Bacon", "American actor (born 1958)", new Point3D(0, 0, 0));
    vert.setAsOrigin();
    return vert;
  }

  private Vertex createVert(String id, String label, String desc, Point3D pos) {
    Vertex vert = new Vertex();
    vert.id(id);
    vert.label(label);
    vert.description(desc);
    vert.coords(pos);
    vert.fetched(true);
    return vert;
  }

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

}
