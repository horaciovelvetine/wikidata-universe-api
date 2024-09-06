package edu.velv.wikidata_universe_api.models;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public final class Constables {
  Constables() {
    // Default class should not be instantiated
  }

  // Wikidata Related Values...
  public static final int WD_TIMEOUT_LIMIT = 1; //? @davewtillman have a feeling these arent moveable to a .properties
  public static final TimeUnit WD_TIMEOUT_UNIT = TimeUnit.MINUTES; //?
  public static final int WD_MAX_N_FETCH_DEPTH = 2;
  public static final int WD_MAX_QUERY_SIZE = 50;
  public static final Set<String> WD_EXCLUDED_ENT_IDS = Set.of("P1343", "P143", "P935", "P8687",
      "P3744", "P18", "P373", "P856", "P1748", "P21", "P11889", "P1424", "P11527", "P1545", "P5008",
      "P1889", "P813", "P214", "P213", "P227", "P244", "P268", "P1006", "P1711", "P648", "P1315",
      "P2163", "P3430", "P1015", "P1207", "P1225", "P4823", "P269", "P322", "P1871", "P691",
      "P4342", "P5361", "P2600", "P535", "P8094", "P7293", "P8189", "P950", "P8318", "P1263",
      "P2949", "P7029", "P7699", "P10227", "P409", "P8081", "P7902", "P4619", "P7369", "P3348",
      "P1368", "P11686", "P10832", "P5034", "P1415", "P6058", "P646", "P5869", "P461", "Q109429537",
      "P7452", "Q19478619", "P4666", "P345", "P2604", "P5007", "Q59522350", "Q32351192", "P1011",
      "P8402", "P2959", "P78", "P5323", "P6104");
  public static final Set<String> WD_EXCLUDED_DATA_TYPES = Set.of("external-id", "monolingualtext",
      "commonsMedia", "url", "globe-coordinate", "geo-shape", "wikibase-lexeme");

  // Layout Related Values...
  public static final int F3D_MAX_ITER = 700;
  public static final int F3D_MAX_MVMNT_PER_ITER = 5;
  public static final int F3D_MRGN_DITH = 2;
  public static final int F3D_BRDR_FACT = 50;
  // TODO: put this number into some sort of reasonable range E-8 math is weird math (maybe Vertex radius size???)
  public static final double F3D_TGT_DENS = 0.0000000001;
  public static final double F3D_ATTR_MULT = 0.75;
  public static final double F3D_REP_MULT = 0.50;
}
