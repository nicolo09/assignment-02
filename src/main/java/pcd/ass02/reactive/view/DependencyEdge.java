package pcd.ass02.reactive.view;

import java.util.Objects;

import com.brunomnsilva.smartgraph.graph.Edge;

public class DependencyEdge implements Edge<String, String> {

    private final String className;
    private final String dependencyName;
    private final String label;

    public DependencyEdge(final String className, final String dependencyName, final String label) {
        this.className = className;
        this.dependencyName = dependencyName;
        this.label = label;
    }

    public DependencyEdge(final String className, final String dependencyName) {
        this(className, dependencyName, "");
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

    @Override
    public int hashCode() {
        return Objects.hash(className.hashCode(), dependencyName.hashCode());
    }

}
