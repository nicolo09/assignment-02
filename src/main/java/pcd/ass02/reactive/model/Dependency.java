package pcd.ass02.reactive.model;

import java.util.Set;

public class Dependency {

    private String className;
    private Set<String> dependencies;

    public Dependency(String className, Set<String> dependencies) {
        this.className = className;
        this.dependencies = dependencies;
    }

    public String getClassName() {
        return className;
    }

    public Set<String> getDependencies() {
        return dependencies;
    }

}
