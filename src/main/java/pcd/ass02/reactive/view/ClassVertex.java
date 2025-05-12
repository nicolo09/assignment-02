package pcd.ass02.reactive.view;

import java.util.Objects;

import com.brunomnsilva.smartgraph.graph.Vertex;

public class ClassVertex implements Vertex<String> {
    private final String className;

    public ClassVertex(String className) {
        this.className = className;
    }

    @Override
    public String element() {
        return className;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ClassVertex) {
            ClassVertex other = (ClassVertex) obj;
            return className.equals(other.className);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(className);
    }

}
