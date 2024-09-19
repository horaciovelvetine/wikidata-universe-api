package edu.velv.wikidata_universe_api.models;

import java.awt.Dimension;

import java.util.List;

/**
 * Record stores the details of an incoming Client Request as they are originally recieved 
 */
public record RequestPayloadData(String query, Dimension dimensions, List<Vertex> vertices,
        List<Edge> edges, List<Property> properties) {
}
