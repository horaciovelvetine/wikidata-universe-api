package edu.velv.wikidata_universe_api.models.wikidata;

import java.text.SimpleDateFormat;

import org.wikidata.wdtk.datamodel.interfaces.EntityIdValue;
import org.wikidata.wdtk.datamodel.interfaces.GlobeCoordinatesValue;
import org.wikidata.wdtk.datamodel.interfaces.MonolingualTextValue;
import org.wikidata.wdtk.datamodel.interfaces.QuantityValue;
import org.wikidata.wdtk.datamodel.interfaces.StringValue;
import org.wikidata.wdtk.datamodel.interfaces.TimeValue;
import org.wikidata.wdtk.datamodel.interfaces.UnsupportedValue;
import org.wikidata.wdtk.datamodel.interfaces.ValueVisitor;

import edu.velv.wikidata_universe_api.utils.Loggable;

public class ValueData implements ValueVisitor<ValueData>, Loggable {
  // Format the Wikidata specific implementation of TimeValue to a format which
  // can be used to find the correlated EntityDocument data from the Wikidata API 
  // e.g. "2021-01-01T00:00:00Z" => "2024-01-01"
  public static final String WDATA_PUNC_FORMATTING = "\\s*\\(.*\\)";
  public String value;
  public ValueType type;

  public enum ValueType {
    String, DateTime, EntityId, Quantity
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
    // if (value != null) {
    //   this.value = value.getString();
    //   this.type = ValueType.String;
    // }
    // return this;
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

  private String convertToWikidataSearchableDate(TimeValue time) {
    //TODO: double check this formatting is the correct conversion, that MMMM has me a bit worried
    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM d, yyyy");
    String punc = value.toString().replaceAll(WDATA_PUNC_FORMATTING, "");
    try {
      return outputFormat.format(inputFormat.parse(punc));
    } catch (Exception e) {
      //TODO: BACK HERE YOU JABRONI
      return null;
    }
  }

}
