package pcd.ass02.async.common.reports;

import java.util.List;

public class PackageDepsReport extends AbstractReport {

    public PackageDepsReport(List<String> dependencies) {
        super(dependencies);
    }

    @Override
    public String toString() {
        return "PackageDepsReport{" +
                "dependencies=" + this.getDependencies() +
                '}';
    }

}
