package edu.velv.wikidata_universe_api.services;

import org.springframework.beans.factory.annotation.Value;

public class FR3DConfig {
  @Value("${edu.velv.FR3D.tgt_data_density}")
  private double dataDensity;
  @Value("${edu.velv.FR3D.rep_mult}")
  private double repMult;
  @Value("${edu.velv.FR3D.attr_mult}")
  private double attrMult;
  @Value("${edu.velv.FR3D.temp_mult}")
  private Integer tempMult;
  @Value("${edu.velv.FR3D.max_iterations}")
  private Integer maxIters;
  @Value("${edu.velv.FR3D.max_iteration_movement_magnitude}")
  private Integer maxIterMvmnt;

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

  public Integer tempMult() {
    return tempMult;
  }

  public Integer maxIters() {
    return maxIters;
  }

  public Integer maxIterMvmnt() {
    return maxIterMvmnt;
  }
}
