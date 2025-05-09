package pcd.ass02;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javafx.util.Pair;
import pcd.ass02.reactive.model.DependenciesGraph;
import pcd.ass02.reactive.view.ClassVertex;
import pcd.ass02.reactive.view.DependenciesDigraphWrapper;
import pcd.ass02.reactive.view.DependencyEdge;

public class DependenciesDigraphWrapperTest {

    DependenciesGraph dependenciesGraph = new DependenciesGraph();
    DependenciesDigraphWrapper dependenciesDigraphWrapper;

    @BeforeEach
    void setUp() {
        dependenciesGraph.addDependency("A", "B");
        dependenciesGraph.addDependency("A", "C");
        dependenciesGraph.addDependency("B", "C");
        dependenciesGraph.addDependency("C", "A");

        dependenciesDigraphWrapper = new DependenciesDigraphWrapper(dependenciesGraph);
    }

    // Test the number of vertices in the graph
    @Test
    void testNumVertices() {
        assertEquals(3, dependenciesDigraphWrapper.numVertices());
    }

    // Test the number of edges in the graph
    @Test
    void testNumEdges() {
        assertEquals(4, dependenciesDigraphWrapper.numEdges());
    }

    // Test the vertices in the graph
    @Test
    void testVertices() {
        assertEquals(3, dependenciesDigraphWrapper.vertices().size());
        assertTrue(dependenciesDigraphWrapper.vertices().stream().map(a -> a.element()).collect(Collectors.toSet())
                .containsAll(Set.of("A", "B", "C")));
    }

    // Test the edges in the graph
    @Test
    void testEdges() {
        assertEquals(4, dependenciesDigraphWrapper.edges().size());
        assertTrue(dependenciesDigraphWrapper.edges().stream().map(a -> new Pair<String, String>(a.vertices()[0].element(), a.vertices()[1].element())).collect(Collectors.toSet())
                .containsAll(Set.of(
                        new Pair<String, String>("A", "B"),
                        new Pair<String, String>("A", "C"),
                        new Pair<String, String>("B", "C"),
                        new Pair<String, String>("C", "A")
                )));
    }

    @Test
    void testOpposite() {
        assertEquals(dependenciesDigraphWrapper.opposite(new ClassVertex("A"), new DependencyEdge("A","B")), new ClassVertex("B"));
        assertNull(dependenciesDigraphWrapper.opposite(new ClassVertex("B"), new DependencyEdge("A","B")));
    }

}
