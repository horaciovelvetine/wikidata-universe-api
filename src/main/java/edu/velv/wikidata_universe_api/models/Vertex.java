package edu.velv.wikidata_universe_api.models;

import java.util.Map;

import org.wikidata.wdtk.datamodel.implementation.ItemDocumentImpl;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument;
import org.wikidata.wdtk.datamodel.interfaces.SiteLink;
import org.wikidata.wdtk.wikibaseapi.WbSearchEntitiesResult;

import com.fasterxml.jackson.annotation.JsonIgnore;

import edu.velv.wikidata_universe_api.Constables;

public class Vertex {
  @JsonIgnore
  public static final Integer RADIUS = 20;
  @JsonIgnore
  private boolean fetched;

  private String id;
  private String label;
  private String description;
  private Map<String, SiteLink> siteLinks; //todo check site links might be helpful for cx
  private Point3D coords;

  public Vertex() {
    //default constructs...
    this.fetched = false;
    this.coords = new Point3D();
  }

  public Vertex(ItemDocumentImpl itemDoc) {
    this.id = itemDoc.getEntityId().getId();
    this.label = itemDoc.findLabel(Constables.EN_LANG_WIKI_KEY);
    this.description = itemDoc.findDescription(Constables.EN_LANG_WIKI_KEY);
    this.siteLinks = itemDoc.getSiteLinks();
    this.fetched = true;
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
    return fetched && id == null;
  }

  public boolean isFetchedOrId() {
    return fetched && id != null;
  }

  public void updateUnfetchedValues(ItemDocumentImpl doc) {
    this.label = doc.findLabel(Constables.EN_LANG_WIKI_KEY);
    this.description = doc.findDescription(Constables.EN_LANG_WIKI_KEY);
    this.siteLinks = doc.getSiteLinks();
    this.fetched = true;
  }

  public void updateUnfetchedValues(WbSearchEntitiesResult result) {
    this.label = result.getLabel();
    this.description = result.getDescription();
    this.siteLinks = null;
    this.fetched = true;
  }

}
