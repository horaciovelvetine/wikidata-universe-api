package edu.velv.wikidata_universe_api.models.jung_ish;

import java.awt.geom.Point2D;

import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;

public class Vertex {
  protected String id;
  protected String label;
  protected String description;
  protected LayoutCoords layoutCoords;

  public Vertex() {
    // Default constructor
  }

  public Vertex(ItemDocument itemDoc) {
    this.id = itemDoc.getEntityId().getId();
    this.label = itemDoc.findLabel("en");
    this.description = itemDoc.findDescription("en");
  }

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

  public record LayoutCoords(Point2D c2, Point3D c3) {
  }

}
