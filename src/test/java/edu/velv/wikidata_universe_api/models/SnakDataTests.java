package edu.velv.wikidata_universe_api.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SnakDataTests {

    @Test
    public void constructs_default_snak_data() {
        SnakData snakData = new SnakData();
        assertNull(snakData.datatype);
        assertNull(snakData.property);
        assertNull(snakData.snakValue);
    }

    @Test
    public void constructs_paramatarized_snak_data() {
        ValueData property = new ValueData();
        ValueData value = new ValueData();
        SnakData snakData = new SnakData("datatype", property, value);
        assertEquals("datatype", snakData.datatype());
        assertEquals(property, snakData.property());
        assertEquals(value, snakData.snakValue());
    }
}