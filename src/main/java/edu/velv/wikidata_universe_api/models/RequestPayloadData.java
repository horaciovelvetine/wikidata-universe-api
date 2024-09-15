package edu.velv.wikidata_universe_api.models;

import java.awt.Dimension;

import java.util.Collection;

/**
 * Record stores the details of an incoming Client Request as they are originally recieved 
 */
public record RequestPayloadData(String query, Dimension dimensions, Collection<Vertex> vertices,
    Collection<Edge> edges, Collection<Property> properties) {
}
