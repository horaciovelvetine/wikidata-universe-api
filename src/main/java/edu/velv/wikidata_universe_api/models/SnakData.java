package edu.velv.wikidata_universe_api.models;

import org.wikidata.wdtk.datamodel.implementation.ValueSnakImpl;
import org.wikidata.wdtk.datamodel.interfaces.NoValueSnak;
import org.wikidata.wdtk.datamodel.interfaces.SnakVisitor;
import org.wikidata.wdtk.datamodel.interfaces.SomeValueSnak;
import org.wikidata.wdtk.datamodel.interfaces.ValueSnak;

public class SnakData implements SnakVisitor<SnakData> {
  public String datatype;
  public ValueData property;
  public ValueData snakValue;

  public SnakData() {
  }

  public SnakData(String type, ValueData property, ValueData value) {
    this.datatype = type;
    this.property = property;
    this.snakValue = value;
  }

  @Override
  public SnakData visit(ValueSnak snak) {
    if (snak instanceof ValueSnakImpl) {
      ValueSnakImpl valueSnak = (ValueSnakImpl) snak;
      return new SnakData(valueSnak.getDatatype(), valueSnak.getPropertyId().accept(new ValueData()),
          valueSnak.getValue().accept(new ValueData()));
    }
    return null;
  }

  @Override
  public SnakData visit(SomeValueSnak snak) {
    // Irrelevant for this project
    return null;
  }

  @Override
  public SnakData visit(NoValueSnak snak) {
    // Irrelevant for this project
    return null;
  }
}
