package edu.velv.wikidata_universe_api.models.wikidata;

import org.wikidata.wdtk.datamodel.interfaces.EntityIdValue;
import org.wikidata.wdtk.datamodel.interfaces.GlobeCoordinatesValue;
import org.wikidata.wdtk.datamodel.interfaces.MonolingualTextValue;
import org.wikidata.wdtk.datamodel.interfaces.QuantityValue;
import org.wikidata.wdtk.datamodel.interfaces.StringValue;
import org.wikidata.wdtk.datamodel.interfaces.TimeValue;
import org.wikidata.wdtk.datamodel.interfaces.UnsupportedValue;
import org.wikidata.wdtk.datamodel.interfaces.ValueVisitor;

public class ValueData implements ValueVisitor<ValueData> {
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
      this.value = value.toString().replaceAll("\\s*\\(.*\\)", "");
      this.type = ValueType.DateTime;
    }
    return this;
  }

  //! Below value types are a required implementation for the ValueVisitor interface
  //! For this application they are uneeded and will return null

  @Override
  public ValueData visit(QuantityValue value) {
    return null;
  }

  @Override
  public ValueData visit(StringValue value) {
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

}
