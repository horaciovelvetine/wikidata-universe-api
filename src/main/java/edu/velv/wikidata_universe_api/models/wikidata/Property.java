package edu.velv.wikidata_universe_api.models.wikidata;

import org.wikidata.wdtk.datamodel.interfaces.PropertyDocument;

public class Property {
  private String id;
  private String label;
  private String description;

  public Property() {
    // Default constructor
  }

  public Property(PropertyDocument propertyDoc) {
    this.id = propertyDoc.getEntityId().getId();
    this.label = propertyDoc.findLabel("en");
    this.description = propertyDoc.findDescription("en");
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

  public void setId(String id) {
    this.id = id;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public String toString() {
    return "Property{" + "id='" + id + '\'' + ", label='" + label + '\'' + ", description='"
        + description.replace("\n", "") + '\'' + '}';
  }
}
