package edu.velv.wikidata_universe_api.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class LayoutConfig {
  //Constant, Client Configureable
  private double dataDensity = 0.0001;
  private double repulsionMult = 0.55;
  private double attractionMult = 0.85;

  public LayoutConfig() {
    //DEFAULT Constructs...
  }

  public double targetDensity() {
    return dataDensity;
  }

  public double repMult() {
    return repulsionMult;
  }

  public double attrMult() {
    return attractionMult;
  }

  public void targetDensity(Double newTgt) {
    this.dataDensity = newTgt;
  }

  public void repMult(Double newMult) {
    this.repulsionMult = newMult;
  }

  public void attrMult(Double newMult) {
    this.attractionMult = newMult;
  }
}
