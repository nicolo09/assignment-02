package pcd.ass02.common.reports;

import java.util.List;

public class AbstractReport {

    private final List<String> dependencies;

    public AbstractReport(List<String> dependencies) {
        this.dependencies = dependencies;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    @Override
    public String toString() {
        return "AbstractReport{" +
                "dependencies=" + dependencies +
                '}';
    }

}
