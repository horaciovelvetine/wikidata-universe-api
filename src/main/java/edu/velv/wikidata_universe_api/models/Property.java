package edu.velv.wikidata_universe_api.models;

import org.wikidata.wdtk.datamodel.interfaces.PropertyDocument;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Property {
  private String id;
  private String label;
  private String description;
  private boolean fetched;

  public Property() {
    //Default constructor does construct
    this.fetched = false;
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

  public void fetched(boolean isFetched) {
    this.fetched = isFetched;
  }

  public void updateUnfetchedValues(PropertyDocument doc, String enLangKey) {
    this.description = doc.findDescription(enLangKey);
    this.label = doc.findLabel(enLangKey);
    this.fetched = true;
  }
}
