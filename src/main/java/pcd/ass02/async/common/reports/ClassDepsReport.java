package pcd.ass02.async.common.reports;

import java.util.List;

public class ClassDepsReport extends AbstractReport {

    public ClassDepsReport(List<String> dependencies) {
        super(dependencies);
    }

    @Override
    public String toString() {
        return "ClassDepsReport{" +
                "dependencies=" + this.getDependencies() +
                '}';
    }
}