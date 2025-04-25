package pcd.ass02.reactive.controller;

import java.nio.file.Path;

import pcd.ass02.reactive.model.ReactiveDependencyFinder;
import pcd.ass02.reactive.view.DependencyView;

public class DependencyController {

    private DependencyView view;
    private Path projectDirectory;
    private final ReactiveDependencyFinder depsFinder;

    public DependencyController(final ReactiveDependencyFinder depsFinder) {
        this.depsFinder = depsFinder;
    }

    public void setProjectDirectory(final Path projectDirectory) {
        // Set the project directory for analysis
        this.projectDirectory = projectDirectory;
    }

    public void startAnalysis() {
        // Start the analysis process
        if (projectDirectory != null) {
            Flow
            depsFinder.findDependencies(projectDirectory);
        } else {
            view.showError("Please select a project directory first.");
        }
    }

}
