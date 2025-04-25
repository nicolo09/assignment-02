package pcd.ass02.async.common.reports;

import java.util.List;

public class ProjectDepsReport extends AbstractReport {

    public ProjectDepsReport(List<String> dependencies) {
        super(dependencies);
    }

    @Override
    public String toString() {
        return "ProjectDepsReport{" +
                "dependencies=" + this.getDependencies() +
                '}';
    }

}
