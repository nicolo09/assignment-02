package pcd.ass02.reactive.controller;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;

import org.slf4j.Logger;

import pcd.ass02.reactive.model.DependenciesGraph;
import pcd.ass02.reactive.model.ReactiveDependencyFinder;
import pcd.ass02.reactive.view.DependencyView;

public class DependencyController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DependencyController.class);

    private DependencyView view;
    private Path projectDirectory;
    private final ReactiveDependencyFinder depsFinder;
    private final DependenciesGraph dependenciesGraph;
    private final Map<Path, String> classMap;

    public DependencyController(final ReactiveDependencyFinder depsFinder, final DependenciesGraph dependenciesGraph) {
        this.depsFinder = depsFinder;
        this.dependenciesGraph = dependenciesGraph;
        this.classMap = new HashMap<>();
    }

    public void setProjectDirectory(final Path projectDirectory) {
        // Set the project directory for analysis
        this.projectDirectory = projectDirectory;
        LOGGER.info("Project directory set to: " + projectDirectory);
    }

    public void setView(final DependencyView view) {
        // Set the view for the controller
        this.view = view;
    }

    public void startAnalysis() {
        // Start the analysis process
        LOGGER.info("Starting analysis for project directory: " + projectDirectory);
        if (projectDirectory != null) {
            // Set the dependencies graph in the view
            depsFinder.findAllClassesDependencies(projectDirectory).subscribe(dependencyInfo -> {
                var dependency = dependencyInfo.getDependency();
                switch (dependencyInfo.getType()) {
                    // For each dependency found, add it to the dependencies graph
                    case DISCOVER, CREATE, MODIFY -> {
                        // Checks the name mapped to the analyzed file
                        var oldClassName = classMap.get(dependencyInfo.getClassPath());
                        if(oldClassName == null) {
                            // If the class is not already in the map, add it
                            classMap.put(dependencyInfo.getClassPath(), dependency.getKey());
                        } else if (!oldClassName.equals(dependency.getKey())) {
                            // If the class is already in the map but with a different name, 
                            // remove the old class name from the graph and add the new one
                            dependenciesGraph.removeClass(oldClassName);
                            classMap.put(dependencyInfo.getClassPath(), dependency.getKey());
                        }
                        dependenciesGraph.addAllDependency(dependency.getKey(), dependency.getValue());
                        LOGGER.info(dependency.getKey() + " depends on: " + dependency.getValue());
                    }
                    case DELETE -> {
                        // the file has been deleted, remove the class from the graph
                        var removedClass = classMap.get(dependencyInfo.getClassPath());
                        dependenciesGraph.removeClass(removedClass);
                        classMap.remove(dependencyInfo.getClassPath());
                        LOGGER.info("Removed class: " + removedClass);
                    }
                }
                // Update the view with the found dependency
                view.updateDependencyGraph();
            }, error -> {
                // Handle any errors that occur during the analysis
                LOGGER.error("Error during analysis: ", error);
                view.showError("Error during analysis: " + error.getMessage());
            }, () -> {
                // Analysis completed
                LOGGER.info("Analysis completed.");
            });
        } else {
            view.showError("Please select a project directory first.");
        }
    }

}
