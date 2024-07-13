package edu.velv.wikidata_universe_api.models;

import java.awt.geom.Point2D;

import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;

import edu.velv.wikidata_universe_api.models.jung.Point3D;

public class Vertex {
  private String id;
  private String label;
  private String description;
  private LayoutCoords layoutCoords;

  public Vertex() {
    // Default constructor
  }

  public Vertex(ItemDocument itemDoc) {
    this.id = itemDoc.getEntityId().getId();
    this.label = itemDoc.findLabel("en");
    this.description = itemDoc.findDescription("en");
  }

  // public Vertex(WbSearchEntitiesResult result) {
  //   this.id = result.getEntityId();
  //   this.label = result.getLabel();
  //   this.description = result.getDescription();
  // }

  public String id() {
    return id;
  }

  public String label() {
    return label;
  }

  public String description() {
    return description;
  }

  public LayoutCoords layoutCoords() {
    return layoutCoords;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setLayoutCoords(Point2D coords2D, Point3D coords3D) {
    this.layoutCoords = new LayoutCoords(coords2D, coords3D);
  }

  protected record LayoutCoords(Point2D coords2D, Point3D coords3D) {
  }

}
