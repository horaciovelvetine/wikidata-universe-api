package edu.velv.wikidata_universe_api.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Dimension;
import java.io.File;
import java.util.List;

import org.wikidata.wdtk.datamodel.implementation.ItemDocumentImpl;
import org.wikidata.wdtk.datamodel.implementation.PropertyDocumentImpl;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue;
import org.wikidata.wdtk.datamodel.interfaces.Snak;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.wikibaseapi.WikibaseDataFetcher;

import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.velv.wikidata_universe_api.interfaces.Loggable;
import edu.velv.wikidata_universe_api.models.ClientRequest;
import edu.velv.wikidata_universe_api.models.Edge;
import edu.velv.wikidata_universe_api.models.LayoutConfig;
import edu.velv.wikidata_universe_api.models.Graphset;
import edu.velv.wikidata_universe_api.models.Vertex;
import edu.velv.wikidata_universe_api.models.ValueData.ValueType;
import edu.velv.wikidata_universe_api.models.Property;
import edu.velv.wikidata_universe_api.models.RequestPayloadData;
import edu.velv.wikidata_universe_api.models.RequestResponseBody;
import edu.velv.wikidata_universe_api.models.SnakData;
import edu.velv.wikidata_universe_api.models.ValueData;

public interface WikidataTestDataBuilders extends Loggable {
  // dir for sampled test data which has been stored
  final String testDataDir = "src/test/java/edu/velv/wikidata_universe_api/data/";
  final String json = ".json";
  final String kbTestItemDocData = "Q3454165-item-doc" + json;
  final String kbTestUnfDataCxRequest = "kevin-bacon-init-query-task-fin-request-response" + json;

  //default for @WikidataToolkit Items
  final String QID = "Q123";

  //default for @WikidataToolkit Properties
  final String PID = "P123";

  //default for @WikidataToolkit enLangKey used to fetch details (#see application.properties)
  final String en = "en";

  //default type-str for entities fetched from @WikidataToolkit 
  final String entStrType = "entity-id";
  final String la = "label";
  final String de = "description";

  /**
   * @return a Vertex with the provided ID & default fetched values
   */
  default Vertex buildVertex(Integer id) {
    Vertex v = new Vertex();
    String qid = "Q" + id;
    v.id(qid);
    v.label(qid + " " + la);
    v.description(de);
    v.fetched(true);
    return v;
  }

  /**
   * @return a Property with the provided ID & default fetched values
   */
  default Property buildProperty(Integer id) {
    Property p = new Property();
    String pid = "P" + id;
    p.id(pid);
    p.label(pid + " " + la);
    p.description(de);
    p.fetched(true);
    return p;
  }

  /**
   * @return an Edge with the provided src, tgt, and property ID values.
   */
  default Edge buildEdge(Integer sId, Integer tId, Integer pId) {
    Edge e = new Edge();
    e.srcId("Q" + sId);
    e.tgtId("Q" + tId);
    e.propertyId("P" + pId);
    e.fetched(true);
    return e;
  }

  /**
   * @return a 300 by 300 unit Dimension obj
   */
  default Dimension buildDimensions_generic() {
    return buildDimension(300, 300);
  }

  /**
   * @return a Dimension obj of the provided width and height 
   */
  default Dimension buildDimension(int width, int height) {
    return new Dimension(width, height);
  }

  /**
   * @return a ClientRequest with the generic 5 Vertex, 3 Property, 9 Edge Graphset used in the body of the request. Dimensions are set to 300x300.
   */
  default RequestPayloadData buildGenericRequestPayload() {
    Graphset gs = buildGraphset_generic();
    List<Vertex> verts = gs.vertices().stream().toList();
    List<Edge> edges = gs.edges().stream().toList();
    List<Property> props = gs.properties().stream().toList();
    return new RequestPayloadData("generic_graphset_test", buildDimensions_generic(), verts, edges,
        props, new LayoutConfig());
  }

  /**
   * Creates a testable graphset of 5 vertices, 3 properties, and 9 edges. Each entity is fetched and considered complete withd default values applied for labels and descriptions.
   */
  default Graphset buildGraphset_generic() {
    List<Vertex> verts = List.of(
        buildVertex(1),
        buildVertex(2),
        buildVertex(3),
        buildVertex(4),
        buildVertex(5));
    List<Property> props = List.of(
        buildProperty(1),
        buildProperty(2),
        buildProperty(3));
    List<Edge> edges = List.of(
        buildEdge(1, 2, 1),
        buildEdge(3, 5, 1),
        buildEdge(1, 5, 1),
        buildEdge(1, 3, 2),
        buildEdge(4, 5, 2),
        buildEdge(2, 3, 2),
        buildEdge(2, 4, 3),
        buildEdge(2, 5, 3),
        buildEdge(5, 2, 3));
    return new Graphset(verts, edges, props);
  }

  /**
   * Creates a mock @WikidataToolkit PropertyDocument with the provided PID and default values 
   */
  default PropertyDocumentImpl mockPropertyDoc(Integer id) {
    PropertyIdValue mId = mockPropertyIdVal(id);
    PropertyDocumentImpl mocDoc = mock(PropertyDocumentImpl.class);

    when(mocDoc.getEntityId()).thenReturn(mId);
    when(mocDoc.findLabel(en)).thenReturn("P" + id + " " + la);
    when(mocDoc.findDescription(en)).thenReturn(de);
    return mocDoc;
  }

  /**
   * Creates a mock @WikidataToolkit PropertyIdValue with the provided PID
   */
  default PropertyIdValue mockPropertyIdVal(Integer id) {
    PropertyIdValue mId = mock(PropertyIdValue.class);
    when(mId.getId()).thenReturn("P" + id);
    return mId;
  }

  /**
   * Creates a mock @WikidataToolkit ItemIdValue with the provided QID
   */
  default ItemIdValue mockItemIDVal(Integer id) {
    ItemIdValue mId = mock(ItemIdValue.class);
    when(mId.getId()).thenReturn("Q" + id);
    return mId;
  }

  /**
   * Creates a mock @WikidataToolkit ItemDocument(impl) with the provided QID and default values
   */
  default ItemDocumentImpl mockItemDoc(Integer id) {
    ItemIdValue mId = mockItemIDVal(id);
    ItemDocumentImpl mocDoc = mock(ItemDocumentImpl.class);

    when(mocDoc.getEntityId()).thenReturn(mId);
    when(mocDoc.findLabel(en)).thenReturn("Q" + id + " " + la);
    when(mocDoc.findDescription(en)).thenReturn(de);
    return mocDoc;
  }

  /**
   * Uses default values to create a Statement which should be evaluated as relevant for ingest
   */
  default Statement mockStatement_valid_generic() {
    ValueData prop = buildEntityValueData(PID);
    ValueData val = buildEntityValueData(QID);
    SnakData snakData = new SnakData(entStrType, prop, val);
    return mockStmtFromData(snakData);
  }

  /**
   * Uses default values and a randomly selected date to create a Statement which should be evaluated as relevant for ingest
   */
  default Statement mockStatement_valid_date() {
    ValueData prop = buildEntityValueData(PID);
    ValueData val = buildDateValueData("1945-12-22");
    SnakData snakData = new SnakData("time-value", prop, val);

    return mockStmtFromData(snakData);
  }

  /**
   * Uses "external-id" an excluded data type to create a Statement which should be evaluated as irrelevant for ingest
   */
  default Statement mockStatement_invalid_externalIdType() {
    ValueData prop = buildEntityValueData(PID);
    ValueData val = buildEntityValueData(QID);
    SnakData snakData = new SnakData("external-id", prop, val); //! invalid
    return mockStmtFromData(snakData);
  }

  /**
   * Uses a Property string from the excluded list to create a Statement which should be evaluated as irrelevant for ingest
   */
  default Statement mockStatement_invalid_property() {
    ValueData prop = buildEntityValueData("P213"); //! invalid
    ValueData val = buildEntityValueData(QID);
    SnakData snakData = new SnakData(entStrType, prop, val);
    return mockStmtFromData(snakData);
  }

  /**
   * Uses an Entity string from the excluded list to create a Statement which should be evaludated as irrelevant for ingest
   */
  default Statement mockStatment_invalid_entity() {
    ValueData prop = buildEntityValueData(PID);
    ValueData val = buildEntityValueData("Q32351192"); //! invalid
    SnakData snakData = new SnakData(entStrType, prop, val);
    return mockStmtFromData(snakData);
  }

  /**
  * Uses an Entity string indicating this Statement should target a Property Entity which should be evaludated as irrelevant for ingest
  */
  default Statement mockStatement_invalid_propertyTarget() {
    ValueData prop = buildEntityValueData(PID);
    ValueData val = buildEntityValueData("P456"); //!invalid
    SnakData snakData = new SnakData(entStrType, prop, val);
    return mockStmtFromData(snakData);
  }

  /**
   * Uses a ValueData of null for the property to create a Statement which should be evaludated as irrelevant for ingest
   */
  default Statement mockStatement_invalid_nullProp() {
    ValueData val = buildEntityValueData(QID);
    SnakData snakData = new SnakData(entStrType, null, val);
    return mockStmtFromData(snakData);
  }

  /**
   * Uses a ValueData of null for the value to create a Statement which should be evaludated as irrelevant for ingest
   */
  default Statement mockStatement_invalid_nullValue() {
    ValueData prop = buildEntityValueData(PID);
    SnakData snakData = new SnakData(entStrType, prop, null);
    return mockStmtFromData(snakData);
  }

  /**
   * Uses null to create a Statement which should be evaludated as irrelevant for ingest
   */
  default Statement mockStatement_invalid_null() {
    return mockStmtFromData(null);
  }

  /**
   *  Wraps the additional needed mock calls to create a Statement which can be run against the EntDocProc 
   */
  private Statement mockStmtFromData(SnakData data) {
    Snak mSnak = mock(Snak.class);
    when(mSnak.accept(any())).thenReturn(data);

    Statement mStatement = mock(Statement.class);
    when(mStatement.getMainSnak()).thenReturn(mSnak);
    return mStatement;
  }

  /**
   * Creates a ValueData where .type() == EntityId with the provided either QID or PID
   * @apiNote the first letter of the string from the EntityIdValue indicates the underlying entity sub-type
   */
  default ValueData buildEntityValueData(String QPid) {
    ValueData vd = new ValueData();
    vd.type = ValueType.EntityId;
    vd.value = QPid;
    return vd;
  }

  /**
   * Creates a ValueData where .type() == DateTime with the provided date string
   * @apiNote the date strings actual value is essentially meaningless, but does need to be something...
   */
  default ValueData buildDateValueData(String date) {
    ValueData vd = new ValueData();
    vd.type = ValueType.DateTime;
    vd.value = date;
    return vd;
  }

  /**
   * Creates an ItemDocument from stored test data (Item Q3454165 (Kevin Bacon)) from the Wikidata API. If the file
   * to recreate the Document is missing, a request is made to get the Doc using the Wikidata API, which then stores the
   * file in the appropriate dir for later tests. 
   */
  default ItemDocumentImpl buildKevinBaconDocFromData() {
    ItemDocumentImpl doc = null;
    try {
      doc = readItemDocFromStorage(kbTestItemDocData);

      if (doc == null) {
        EntityDocument fetchedDoc = fetchKevinBaconData();
        captureWikidataEntityDoc(fetchedDoc);
        doc = readItemDocFromStorage(kbTestItemDocData);
      }
    } catch (Exception e) {
      print(e);
    }

    return doc;
  }

  /**
   * Provided any of the EntityDocument subtypes applicable to this application, records that document to a .json file
   * for later use as sample test data to run against this application.
   */
  default void captureWikidataEntityDoc(EntityDocument entDoc) {
    try {
      String fileName = entDoc.getEntityId().getId();
      switch (entDoc) {
        case ItemDocumentImpl cDoc -> {
          fileName += "-item-doc";
        }
        case PropertyDocumentImpl cDoc -> {
          fileName += "-property-doc";
        }
        default -> {
          fileName += "-entity-doc";
        }
      }
      fileName += json;
      mapper.writeValue(new File(testDataDir + fileName), entDoc);
    } catch (Exception e) {
      print(e);
    }
  }

  /**
   * Captures the provided ClientRequest inside of a new .json File() using the provided
   */
  default void captureClientRequest(ClientRequest request, String dataState) {
    String hy = "-";
    String hyphDataState = dataState.replace(" ", "-") + hy;
    String hyphQueryStr = request.query().toLowerCase().replace(" ", "-") + hy;
    String reqDataType = "request-response";
    try {
      String fileName = hyphQueryStr + hyphDataState + reqDataType + json;
      mapper.writeValue(new File(testDataDir + fileName), new RequestResponseBody(request));
    } catch (Exception e) {
      print(e);
    }
  }

  /**
   * Looks for existing test data with the provided fileName string in the resources/data directory 
   */
  default EntityDocument readEntityDocFromStorage(String fileName) {
    try {
      return mapper.readValue(new File(testDataDir + fileName), EntityDocument.class);
    } catch (Exception e) {
      print(e);
      return null;
    }
  }

  /**
   * Looks for existing test data with the provided fileName string in the resources/data directory
   *
   * @apiNote siteIri values are hardcoded because they are unsued and require data from a seperate
   * part of the Wikidata Toolkit's API 
   */
  default ItemDocumentImpl readItemDocFromStorage(String fileName) {
    try {
      InjectableValues.Std injectableValues = new InjectableValues.Std();
      injectableValues.addValue("siteIri", "demoiri");

      mapper.setInjectableValues(injectableValues);
      return mapper.readValue(new File(testDataDir + fileName), ItemDocumentImpl.class);
    } catch (Exception e) {
      print(e);
      return null;
    }
  }

  /**
   * Looks for existing test data with the provided fileName string in the resources/data directory 
   */
  default PropertyDocumentImpl readPropertyDocFromStorage(String fileName) {
    try {
      return mapper.readValue(new File(testDataDir + fileName), PropertyDocumentImpl.class);
    } catch (Exception e) {
      print(e);
      return null;
    }
  }

  /**
   * Looks for existing test data with the provided fileName string in the resources/data directory 
   */
  default RequestPayloadData readClientRequestPayloadFromStorage(String fileName) {
    try {
      ObjectMapper mapperWithIgnore = mapper.copy();
      mapperWithIgnore.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
          false);
      return mapperWithIgnore.readValue(new File(testDataDir + kbTestUnfDataCxRequest),
          RequestPayloadData.class);
    } catch (Exception e) {
      print(e);
      return null;
    }
  }

  /**
   * Requests a copy of the en-wiki ItemDocument Kevin Bacon for ref, and use and testing.
   */
  private EntityDocument fetchKevinBaconData() throws Exception {
    return WikibaseDataFetcher.getWikidataDataFetcher().getEntityDocumentByTitle("enwiki", "Kevin Bacon");
  }

}
