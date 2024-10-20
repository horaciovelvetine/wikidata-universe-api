package edu.velv.wikidata_universe_api.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class FR3DConfig {
  //Constant, Client Configureable
  private double dataDensity = 0.0001;
  private double repMult = 0.55;
  private double attrMult = 1.75;

  public FR3DConfig() {
    //DEFAULT Constructs...
  }

  public double targetDensity() {
    return dataDensity;
  }

  public double repMult() {
    return repMult;
  }

  public double attrMult() {
    return attrMult;
  }

  public void targetDensity(Double newTgt) {
    this.dataDensity = newTgt;
  }

  public void repMult(Double newMult) {
    this.repMult = newMult;
  }

  public void attrMult(Double newMult) {
    this.attrMult = newMult;
  }
}
