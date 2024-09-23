package edu.velv.wikidata_universe_api;

import java.util.Set;

public class Constables {
  public static final String EN_WIKI_IRI = "enwiki";
  public static final String EN_LANG_WIKI_KEY = "en";
  public static final Set<String> EXCLUDED_ENT_IDS = Set.of("P1343", "P143", "P935", "P8687",
      "P3744", "P18", "P373", "P856", "P1748", "P21", "P11889", "P1424", "P11527", "P1545", "P5008",
      "P1889", "P813", "P214", "P213", "P227", "P244", "P268", "P1006", "P1711", "P648", "P1315",
      "P2163", "P3430", "P1015", "P1207", "P1225", "P4823", "P269", "P322", "P1871", "P691",
      "P4342", "P5361", "P2600", "P535", "P8094", "P7293", "P8189", "P950", "P8318", "P1263",
      "P2949", "P7029", "P7699", "P10227", "P409", "P8081", "P7902", "P4619", "P7369", "P3348",
      "P1368", "P11686", "P10832", "P5034", "P1415", "P6058", "P646", "P5869", "P461", "Q109429537",
      "P7452", "Q19478619", "P4666", "P345", "P2604", "P5007", "Q59522350", "Q32351192", "P1011",
      "P8402", "P2959", "P78", "P5323", "P6104");
  public static final Set<String> EXCLUDED_DATA_TYPES = Set.of("external-id", "monolingualtext",
      "commonsMedia", "url", "globe-coordinate", "geo-shape", "wikibase-lexeme");
  // FR3D LAYOUT SPECIFIC STUFF
  public static final double TARGET_DATA_DENSITY = 0.005;
  public static final double REP_MULT = 0.5;
  public static final double ATTR_MULT = 0.75;
  public static final double TEMP_MULT = 10;
  public static final int MAX_ITERS = 700;
  public static final double MAX_ITER_MVMNT = 5;

  private Constables() {
    // Prevent instantiating for constant variables usage
  }
}
