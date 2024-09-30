package edu.velv.wikidata_universe_api._v1.models;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.wikidata.wdtk.datamodel.interfaces.TimeValue;

import edu.velv.wikidata_universe_api.models.ValueData;

public class ValueDataTests {

  private ValueData valueData;

  @BeforeEach
  public void setUp() {
    valueData = new ValueData();
  }

  @Test
  public void convertToWikidataSearchableDate_formats_time_as_expected() throws Exception {
    TimeValue mockTimeValue = mock(TimeValue.class);
    when(mockTimeValue.toString()).thenReturn("2021-01-01T00:00:00Z");

    String expectedDate = "January 1, 2021";
    String actualDate = valueData.convertToWikidataSearchableDate(mockTimeValue);

    assertEquals(expectedDate, actualDate);
  }

  @Test
  public void convertToWikidataSearchableData_returns_original_unformattable() throws Exception {
    TimeValue mockTimeValue = mock(TimeValue.class);
    when(mockTimeValue.toString()).thenReturn("invalid-date");

    String expectedDate = "invalid-date";
    String actualDate = valueData.convertToWikidataSearchableDate(mockTimeValue);

    assertEquals(expectedDate, actualDate);
  }

  @Test
  public void testConvertToWikidataSearchableDate_handles_null() {
    TimeValue mockTimeValue = mock(TimeValue.class);
    when(mockTimeValue.toString()).thenReturn(null);

    String expectedDate = null;
    String actualDate = valueData.convertToWikidataSearchableDate(mockTimeValue);

    assertEquals(expectedDate, actualDate);
  }
}