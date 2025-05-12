package pcd.ass02.reactive.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DependenciesGraph {

    private final Map<String, Set<String>> dependencies;

    public DependenciesGraph() {
        this.dependencies = new HashMap<>();
    }

    public void addDependency(String className, String dependencyName) {
        // If the class already exists, update the dependency set
        if (dependencies.containsKey(className)) {
            dependencies.get(className).add(dependencyName);
        } else {
            // If the class does not exist, create a new entry in the map
            Set<String> dependencySet = new HashSet<>();
            dependencySet.add(dependencyName);
            dependencies.put(className, dependencySet);
        }
    }

    public void setDependencies(String className, Set<String> dependencyNames) {
        // If the class already exists, update the dependency set
        if (dependencies.containsKey(className)) {
            dependencies.get(className).clear();
            dependencies.get(className).addAll(dependencyNames);
        } else {
            // If the class does not exist, create a new entry in the map
            dependencies.put(className, new HashSet<>(dependencyNames));
        }
    }

    public void addAllDependencies(String className, Set<String> dependencyNames) {
        // If the class already exists, update the dependency set
        if (dependencies.containsKey(className)) {
            dependencies.get(className).addAll(dependencyNames);
        } else {
            // If the class does not exist, create a new entry in the map
            dependencies.put(className, new HashSet<>(dependencyNames));
        }
    }

    public void removeDependency(String className, String dependencyName) {
        dependencies.get(className).remove(dependencyName);
        // If the dependency set is empty, remove the class from the map
        if (dependencies.get(className).isEmpty()) {
            dependencies.remove(className);
        }
    }

    public void removeClass(String className) {
        dependencies.remove(className);
    }

    public Set<String> getClassDependencies(String className) {
        return Set.copyOf(dependencies.getOrDefault(className, Set.of()));
    }

    public Map<String, Set<String>> getAllDependencies() {
        return dependencies;
    }

    public void empty() {
        dependencies.clear();
    }

}
