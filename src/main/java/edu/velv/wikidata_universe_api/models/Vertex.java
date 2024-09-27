package edu.velv.wikidata_universe_api.models;

import org.wikidata.wdtk.datamodel.implementation.ItemDocumentImpl;
import org.wikidata.wdtk.datamodel.implementation.SitesImpl;
import org.wikidata.wdtk.wikibaseapi.WbSearchEntitiesResult;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

import edu.velv.wikidata_universe_api.Constables;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Vertex {
  @JsonIgnore
  private SitesImpl sites = new SitesImpl();

  public static final Integer RADIUS = 20;

  private String id;
  private String label;
  private String description;
  private String pageUrl;
  private String siteLinkUrl;
  private boolean fetched;
  private boolean origin;
  private Point3D coords;

  public Vertex() {
    //default constructs...
    this.fetched = false;
    this.origin = false;
    this.coords = new Point3D();
  }

  public Vertex(ItemDocumentImpl itemDoc) {
    this.id = itemDoc.getEntityId().getId();
    this.label = itemDoc.findLabel(Constables.EN_LANG_WIKI_KEY);
    this.description = itemDoc.findDescription(Constables.EN_LANG_WIKI_KEY);
    // this.pageUrl = getPageUrl(itemDoc);
    // this.siteLinkUrl = getSiteLinkUrl(itemDoc);
    this.pageUrl = null;
    this.siteLinkUrl = null;
    this.fetched = true;
    this.origin = false;
    this.coords = new Point3D();
  }

  public String id() {
    return id;
  }

  public void id(String id) {
    this.id = id;
  }

  public String label() {
    return label;
  }

  public void label(String label) {
    this.label = label;
  }

  public String description() {
    return description;
  }

  public void description(String description) {
    this.description = description;
  }

  public boolean fetched() {
    return fetched;
  }

  public void fetched(boolean fetched) {
    this.fetched = fetched;
  }

  public Point3D coords() {
    return this.coords;
  }

  public void coords(Point3D point) {
    this.coords = point;
  }

  public String pageUrl() {
    return this.pageUrl;
  }

  public void pageUrl(String pageUrl) {
    this.pageUrl = pageUrl;
  }

  public String siteLinkUrl() {
    return this.siteLinkUrl;
  }

  public void siteLinkUrl(String siteLinkUrl) {
    this.siteLinkUrl = siteLinkUrl;
  }

  public boolean isFetchedOrDate() {
    return fetched || id == null;
  }

  public boolean isFetchedOrId() {
    return fetched && !id.isBlank();
  }

  public void setAsOrigin() {
    this.origin = true;
  }

  public boolean origin() {
    return this.origin;
  }

  public void updateUnfetchedValues(ItemDocumentImpl doc) {
    this.label = doc.findLabel(Constables.EN_LANG_WIKI_KEY);
    this.description = doc.findDescription(Constables.EN_LANG_WIKI_KEY);
    // this.pageUrl = getPageUrl(doc);
    // this.siteLinkUrl = getSiteLinkUrl(doc);
    this.pageUrl = null;
    this.siteLinkUrl = null;
    this.fetched = true;
  }

  public void updateUnfetchedValues(WbSearchEntitiesResult result) {
    this.label = result.getLabel();
    this.description = result.getDescription();
    this.pageUrl = null;
    this.siteLinkUrl = null;
    this.fetched = true;
  }

}
