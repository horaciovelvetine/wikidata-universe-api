package edu.velv.wikidata_universe_api.models;

import org.wikidata.wdtk.datamodel.implementation.ItemDocumentImpl;
import org.wikidata.wdtk.wikibaseapi.WbSearchEntitiesResult;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Vertex {

  private String id;
  private String label;
  private String description;
  private boolean fetched;
  private boolean origin;
  private final Integer radius = 20;
  private boolean locked = false;
  private Point3D coords;

  public Vertex() {
    //default constructs...
    this.fetched = false;
    this.origin = false;
    this.coords = new Point3D();
  }

  public Vertex(ItemDocumentImpl itemDoc, String enLangKey) {
    this.id = itemDoc.getEntityId().getId();
    this.label = itemDoc.findLabel(enLangKey);
    this.description = itemDoc.findDescription(enLangKey);
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

  @JsonIgnore
  public boolean isFetchedOrDate() {
    return fetched || id == null;
  }

  @JsonIgnore
  public boolean isFetchedOrId() {
    return fetched && !id.isBlank();
  }

  public void setAsOrigin() {
    this.origin = true;
    this.lock();
  }

  public boolean origin() {
    return this.origin;
  }

  public boolean locked() {
    return this.locked;
  }

  public Integer radius() {
    return this.radius;
  }

  /**
   * Sets this vertices locked attribute to true, preventing the FR3D layout from moving this vertices position when calculating a layout
   */
  @JsonIgnore
  public void lock() {
    this.locked = true;
  }

  /**
   * Sets this vertices locked attribute to false, allowing the FR3D layout from moving this vertices position when calculating a layout
   */
  @JsonIgnore
  public void unlock() {
    this.locked = false;
  }

  public void updateUnfetchedValues(ItemDocumentImpl doc, String enLangKey) {
    this.label = doc.findLabel(enLangKey);
    this.description = doc.findDescription(enLangKey);
    this.fetched = true;
  }

  public void updateUnfetchedValues(WbSearchEntitiesResult result) {
    this.label = result.getLabel();
    this.description = result.getDescription();
    this.fetched = true;
  }

}
