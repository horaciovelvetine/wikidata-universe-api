package edu.velv.wikidata_universe_api.models;

import org.wikidata.wdtk.datamodel.interfaces.PropertyDocument;

import edu.velv.wikidata_universe_api.Constables;

public class Property {
  private String id;
  private String label;
  private String description;
  private boolean fetched;

  public Property() {
    //Default constructor does construct
    this.fetched = false;
  }

  public Property(PropertyDocument propDoc) {
    this.id = propDoc.getEntityId().getId();
    this.label = propDoc.findLabel(Constables.EN_LANG_WIKI_KEY);
    this.description = propDoc.findDescription(Constables.EN_LANG_WIKI_KEY);
    this.fetched = true;
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

  public void updateUnfetchedValues(PropertyDocument doc) {
    this.description = doc.findDescription(Constables.EN_LANG_WIKI_KEY);
    this.label = doc.findLabel(Constables.EN_LANG_WIKI_KEY);
    this.fetched = true;
  }
}
