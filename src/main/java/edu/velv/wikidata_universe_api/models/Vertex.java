package edu.velv.wikidata_universe_api.models;

import org.wikidata.wdtk.datamodel.implementation.ItemDocumentImpl;
import org.wikidata.wdtk.wikibaseapi.WbSearchEntitiesResult;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Vertex {
  public static final Integer RADIUS = 20;

  private String id;
  private String label;
  private String description;
  private boolean fetched;
  private boolean origin;
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
