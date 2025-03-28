package edu.velv.wikidata_universe_api.models;

import java.text.SimpleDateFormat;

import org.wikidata.wdtk.datamodel.interfaces.EntityIdValue;
import org.wikidata.wdtk.datamodel.interfaces.GlobeCoordinatesValue;
import org.wikidata.wdtk.datamodel.interfaces.MonolingualTextValue;
import org.wikidata.wdtk.datamodel.interfaces.QuantityValue;
import org.wikidata.wdtk.datamodel.interfaces.StringValue;
import org.wikidata.wdtk.datamodel.interfaces.TimeValue;
import org.wikidata.wdtk.datamodel.interfaces.UnsupportedValue;
import org.wikidata.wdtk.datamodel.interfaces.ValueVisitor;

public class ValueData implements ValueVisitor<ValueData> {
  // Format the Wikidata specific implementation of TimeValue to a format which
  // can be used to find the correlated EntityDocument data from the Wikidata API 
  // e.g. "2021-01-01T00:00:00Z" => "2024-01-01"
  public static final String WDATA_PUNC_FORMATTING = "\\s*\\(.*\\)";
  public String value;
  public ValueType type;

  public enum ValueType {
    String, DateTime, EntityId, Quantity
  }

  public String value() {
    return value;
  }

  public ValueType type() {
    return type;
  }

  @Override
  public ValueData visit(EntityIdValue value) {
    if (value != null) {
      this.value = value.getId();
      this.type = ValueType.EntityId;
    }
    return this;
  }

  @Override
  public ValueData visit(TimeValue value) {
    if (value != null) {
      this.value = convertToWikidataSearchableDate(value);
      this.type = ValueType.DateTime;
    }
    return this;
  }

  @Override
  public ValueData visit(StringValue value) {
    return null;
  }

  @Override
  public ValueData visit(QuantityValue value) {
    return null;
  }

  @Override
  public ValueData visit(GlobeCoordinatesValue value) {
    return null;
  }

  @Override
  public ValueData visit(MonolingualTextValue value) {
    return null;
  }

  @Override
  public ValueData visit(UnsupportedValue value) {
    return null;
  }

  protected String convertToWikidataSearchableDate(TimeValue time) {
    if (time.toString() == null)
      return null;
    String punc = time.toString().replaceAll(WDATA_PUNC_FORMATTING, "");

    try {
      SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
      SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM d, yyyy");
      return outputFormat.format(inputFormat.parse(punc));
    } catch (Exception e) {
      return punc;
    }
  }

}
