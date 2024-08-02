package edu.velv.wikidata_universe_api.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public interface Timestampable {
  public default String now() {
    LocalDateTime nDt = LocalDateTime.now();
    return nDt.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
  }
}
