package edu.velv.wikidata_universe_api._Utils;

public interface FailedTestMessageTemplates {
  public static final String empty = "empty ";
  public static final String unable = "unable ";
  public static final String toFind = "to find ";
  public static final String unableToFind = unable + toFind;
  public static final String able = "able";
  public static final String ableToFind = able + toFind;
  public static final String should = "should ";
  public static final String shouldNotBe = should + "not be";
  public static final String shouldBe = should + "be ";
  public static final String equal = "equal ";
  public static final String shouldBeEq = shouldBe + equal;
  public static final String shouldNotBeEq = shouldNotBe + equal;
  public static final String val = "value ";
  public static final String vert = "Vertex ";
  public static final String edge = "Edge ";
  public static final String prop = "Property ";
  public static final String relEdges = " related edges ";
  public static final String init = "initialize ";
  public static final String inited = "initialized ";
  public static final String with = "with ";
  public static final String without = "without ";
  public static final String initW = init + with;
  public static final String initedW = inited + with;
  public static final String initWO = init + without;
  public static final String initedWO = inited + without;
  public static final String beforeRemoval = "before removal ";
  public static final String afterRemoval = " after removal ";
}
