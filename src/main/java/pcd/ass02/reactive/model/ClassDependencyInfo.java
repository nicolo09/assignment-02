package pcd.ass02.reactive.model;

import java.nio.file.Path;
import java.util.Map.Entry;
import java.util.Set;

public class ClassDependencyInfo {

    public enum DependencyChangeType {
        DISCOVER,
        CREATE,
        MODIFY,
        DELETE
    }

    private final Entry<String, Set<String>> dependency;
    private final DependencyChangeType changeType;
    private final Path classPath;
    

    public ClassDependencyInfo(Entry<String, Set<String>> dependency, DependencyChangeType changeType, Path classPath) {
        this.dependency = dependency;
        this.changeType = changeType;
        this.classPath = classPath;
    }

    public Entry<String, Set<String>> getDependency() {
        return dependency;
    }

    public DependencyChangeType getType() {
        return changeType;
    }

    public Path getClassPath() {
        return classPath;
    }

}
