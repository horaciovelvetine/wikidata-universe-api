package edu.velv.wikidata_universe_api.err;

public sealed interface Err permits WikidataServiceError, Err.OnlyJabronisError {
  record OnlyJabronisError(String message) implements Err {
  }
}
