package edu.velv.wikidata_universe_api.models.wikidata;

import org.wikidata.wdtk.datamodel.implementation.ValueSnakImpl;
import org.wikidata.wdtk.datamodel.interfaces.NoValueSnak;
import org.wikidata.wdtk.datamodel.interfaces.SnakVisitor;
import org.wikidata.wdtk.datamodel.interfaces.SomeValueSnak;
import org.wikidata.wdtk.datamodel.interfaces.ValueSnak;

public class SnakData implements SnakVisitor<SnakData> {
  public String datatype;
  public ValueData property;
  public ValueData snakValue;

  @Override
  public SnakData visit(ValueSnak snak) {
    if (snak instanceof ValueSnakImpl) {
      ValueSnakImpl valueSnak = (ValueSnakImpl) snak;
      this.datatype = valueSnak.getDatatype();
      this.property = valueSnak.getPropertyId().accept(new ValueData());
      this.snakValue = valueSnak.getValue().accept(new ValueData());
      return this;
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
