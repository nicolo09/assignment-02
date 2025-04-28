package pcd.ass02.reactive.view;

import java.util.List;

import com.brunomnsilva.smartgraph.graph.Edge;
import com.brunomnsilva.smartgraph.graph.Vertex;

public class DependencyEdge implements Edge<String, String> {

    private final String className;
    private final String dependencyName;
    private final String label;

    public DependencyEdge(String className, String dependencyName, String label) {
        this.className = className;
        this.dependencyName = dependencyName;
        this.label = label;
    }

    public String getClassName() {
        return className;
    }

    public String getDependencyName() {
        return dependencyName;
    }

    @Override
    public String element() {
        return label;
    }

    @Override
    public ClassVertex[] vertices() {
        // This is a workaround to explicit casting and avoid the warning about the
        // generic array creation.
        // Vertex<String>[] vertices = null;
        return new ClassVertex[] { new ClassVertex(className), new ClassVertex(dependencyName) };
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof DependencyEdge) {
            DependencyEdge other = (DependencyEdge) obj;
            return className.equals(other.className) && dependencyName.equals(other.dependencyName);
        }
        return false;
    }

}
