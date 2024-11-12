package edu.velv.wikidata_universe_api.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Used to represent large chunks of text information displayed on screen for the client when they are using the tutorial to guide them through using the Wikiverse
 */
public class AboutSlide {

  @JsonProperty("title")
  private String title;

  @JsonProperty("body")
  private String body;

  @JsonProperty("inst")
  private String inst;

  @JsonProperty("nav")
  private String nav;

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public String getInst() {
    return inst;
  }

  public void setInst(String inst) {
    this.inst = inst;
  }

  public String getNav() {
    return nav;
  }

  public void setNav(String nav) {
    this.nav = nav;
  }

  /**
   * Uses an easy to parse toString() to accumulate each slide to a single string splittable on "::" for needed details.
   */
  @Override
  public String toString() {
    String delim = "::";
    return getNav() + delim + getTitle() + delim + getBody() + delim + getInst();
  }
}
