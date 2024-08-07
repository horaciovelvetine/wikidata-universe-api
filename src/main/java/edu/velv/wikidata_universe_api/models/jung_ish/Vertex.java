package edu.velv.wikidata_universe_api.models.jung_ish;

import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.wikibaseapi.WbSearchEntitiesResult;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
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
    this.coords = new Point3D();
  }

  public Vertex(WbSearchEntitiesResult result) {
    this.id = result.getEntityId();
    this.label = result.getLabel();
    this.description = result.getDescription();
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

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    Vertex other = (Vertex) obj;
    return id.equals(other.id) &&
        label.equals(other.label) &&
        description.equals(other.description);
  }

}
