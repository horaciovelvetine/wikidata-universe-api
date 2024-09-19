package edu.velv.wikidata_universe_api.services;

import org.wikidata.wdtk.datamodel.implementation.ItemDocumentImpl;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;
import org.wikidata.wdtk.datamodel.interfaces.PropertyDocument;

import edu.velv.wikidata_universe_api.Constables;
import edu.velv.wikidata_universe_api.models.Vertex;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

public class EntDocProcTests {
  EntDocProc docProc = new EntDocProc();

  @Test
  void attemptVertexCreateUnkownTypeEntDoc_creates_vertex_when_expected() throws Exception {
    ItemDocumentImpl mockItemDoc = mock(ItemDocumentImpl.class);
    ItemIdValue mockIdValue = mock(ItemIdValue.class);
    when(mockIdValue.getId()).thenReturn("Q42");
    when(mockItemDoc.getEntityId()).thenReturn(mockIdValue);
    when(mockItemDoc.findLabel(Constables.EN_LANG_WIKI_KEY)).thenReturn("Douglas Adams");
    when(mockItemDoc.findDescription(Constables.EN_LANG_WIKI_KEY))
        .thenReturn("English science fiction writer and humorist (1952–2001)");

    Optional<Vertex> result = docProc.attemptVertexCreateUnkownTypeEntDoc(mockItemDoc);

    assertNotNull(result);
    assertEquals("Q42", result.get().id());
    assertEquals("Douglas Adams", result.get().label());
    assertEquals("English science fiction writer and humorist (1952–2001)", result.get().description());
  }

  @Test
  void attemptVertexCreateUnkownTypeEntDoc_is_empty_when_expected() {
    PropertyDocument mockPropDoc = mock(PropertyDocument.class);
    Optional<Vertex> result = docProc.attemptVertexCreateUnkownTypeEntDoc(mockPropDoc);
    assertTrue(result.isEmpty());
  }
}
