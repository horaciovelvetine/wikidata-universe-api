package edu.velv.wikidata_universe_api.models.jung_ish;

import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;

public class Vertex {
  protected String id;
  protected String label;
  protected String description;
  protected Point3D coords;

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

  public Point3D coords() {
    return coords;
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

  public void setCoords(Point3D point) {
    this.coords = point;
  }

}
