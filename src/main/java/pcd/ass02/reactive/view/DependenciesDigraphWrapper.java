package pcd.ass02.reactive.view;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.brunomnsilva.smartgraph.graph.Digraph;
import com.brunomnsilva.smartgraph.graph.Edge;
import com.brunomnsilva.smartgraph.graph.InvalidEdgeException;
import com.brunomnsilva.smartgraph.graph.InvalidVertexException;
import com.brunomnsilva.smartgraph.graph.Vertex;

import pcd.ass02.reactive.model.DependenciesGraph;

public class DependenciesDigraphWrapper implements Digraph<String, String> {

    private final DependenciesGraph dependenciesGraph;

    public DependenciesDigraphWrapper(DependenciesGraph dependenciesGraph) {
        this.dependenciesGraph = dependenciesGraph;
    }

    @Override
    public int numVertices() {
        // The number of vertices is the number of unique dependencies and classes in
        // the graph.
        return Stream.concat(
                dependenciesGraph.getAllDependencies().keySet().stream(),
                dependenciesGraph.getAllDependencies().values().stream().flatMap(s -> s.stream()))
                .distinct()
                .collect(Collectors.counting())
                .intValue();
    }

    @Override
    public int numEdges() {
        // The number of edges is the number of total dependencies in the graph.
        return dependenciesGraph.getAllDependencies().values().stream().reduce(0,
                (a, b) -> a + b.size(), Integer::sum);
    }

    @Override
    public Collection<Vertex<String>> vertices() {
        // The vertices are the unique dependencies and classes in the graph.
        return Stream.concat(
                dependenciesGraph.getAllDependencies().keySet().stream(),
                dependenciesGraph.getAllDependencies().values().stream().flatMap(s -> s.stream()))
                .distinct()
                .map(ClassVertex::new)
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<Edge<String, String>> edges() {
        // The edges are the dependencies in the graph.
        return dependenciesGraph.getAllDependencies().entrySet().stream()
                .flatMap(d -> d.getValue().stream()
                        // TODO: Add a label to the edge
                        .map(dependencyName -> new DependencyEdge(d.getKey(), dependencyName)))
                .collect(Collectors.toSet());
    }

    @Override
    public Vertex<String> opposite(Vertex<String> v, Edge<String, String> e)
            throws InvalidVertexException, InvalidEdgeException {
        if (dependenciesGraph.getClassDependencies(v.element()).contains(e.vertices()[1].element())) {
            return e.vertices()[1];
        } else {
            return null;
        }
    }

    @Override
    public Vertex<String> insertVertex(String vElement) throws InvalidVertexException {
        // This method is not implemented because you should not edit the graph from
        // GUI.
        throw new UnsupportedOperationException("Unimplemented method 'insertVertex'");
    }

    @Override
    public String removeVertex(Vertex<String> v) throws InvalidVertexException {
        // This method is not implemented because you should not edit the graph from
        // GUI.
        throw new UnsupportedOperationException("Unimplemented method 'removeVertex'");
    }

    @Override
    public String removeEdge(Edge<String, String> e) throws InvalidEdgeException {
        // This method is not implemented because you should not edit the graph from
        // GUI.
        throw new UnsupportedOperationException("Unimplemented method 'removeEdge'");
    }

    @Override
    public String replace(Vertex<String> v, String newElement) throws InvalidVertexException {
        // This method is not implemented because you should not edit the graph from
        // GUI.
        throw new UnsupportedOperationException("Unimplemented method 'replace'");
    }

    @Override
    public String replace(Edge<String, String> e, String newElement) throws InvalidEdgeException {
        // This method is not implemented because you should not edit the graph from
        // GUI.
        throw new UnsupportedOperationException("Unimplemented method 'replace'");
    }

    @Override
    public Collection<Edge<String, String>> incidentEdges(Vertex<String> inbound) throws InvalidVertexException {
        return dependenciesGraph.getAllDependencies().entrySet().stream()
                .filter(entry -> entry.getValue().contains(inbound.element()))
                .map(entry -> new DependencyEdge(entry.getKey(), inbound.element()))
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<Edge<String, String>> outboundEdges(Vertex<String> outbound) throws InvalidVertexException {
        return dependenciesGraph.getClassDependencies(outbound.element()).stream()
                .map(dependencyName -> new DependencyEdge(outbound.element(), dependencyName))
                .collect(Collectors.toSet());
    }

    @Override
    public boolean areAdjacent(Vertex<String> outbound, Vertex<String> inbound) throws InvalidVertexException {
        // Check if the outbound vertex has an edge to the inbound vertex.
        return dependenciesGraph.getClassDependencies(outbound.element()).contains(inbound.element());
    }

    @Override
    public Edge<String, String> insertEdge(Vertex<String> outbound, Vertex<String> inbound, String edgeElement)
            throws InvalidVertexException, InvalidEdgeException {
        // This method is not implemented because you should not edit the graph from
        // GUI.
        throw new UnsupportedOperationException("Unimplemented method 'insertEdge'");
    }

    @Override
    public Edge<String, String> insertEdge(String outboundElement, String inboundElement, String edgeElement)
            throws InvalidVertexException, InvalidEdgeException {
        // This method is not implemented because you should not edit the graph from
        // GUI.
        throw new UnsupportedOperationException("Unimplemented method 'insertEdge'");
    }

}
