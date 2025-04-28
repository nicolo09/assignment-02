package pcd.ass02.reactive.controller;

import java.nio.file.Path;

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

    public DependencyController(final ReactiveDependencyFinder depsFinder, final DependenciesGraph dependenciesGraph) {
        this.depsFinder = depsFinder;
        this.dependenciesGraph = dependenciesGraph;
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
            //TODO: Remove this
            /*
            Observable<String> observable = Observable.create(emitter -> {
                new Thread(() -> {
                    FileSystem fs = FileSystems.getDefault();
                    try (WatchService ws = fs.newWatchService()) {
                        projectDirectory.register(ws,
                                new WatchEvent.Kind[] { ENTRY_MODIFY, ENTRY_CREATE, ENTRY_DELETE },
                                FILE_TREE);
                        LOGGER.info("Watching directory: " + projectDirectory);
                        while (true) {
                            WatchKey k = ws.take();
                            for (WatchEvent<?> e : k.pollEvents()) {
                                Object c = e.context();
                                LOGGER.info(e.kind().toString() + " " + e.count() + " " + c);
                            }
                            k.reset();
                        }
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                }).start();
            });
            observable.subscribe(path -> {
                LOGGER.info("File changed: " + path);
            });
            */
            // Set the dependencies graph in the view
            depsFinder.findAllClassesDependencies(projectDirectory).subscribe(dependency -> {
                // For each dependency found, add it to the dependencies graph
                dependenciesGraph.addAllDependency(dependency.getKey(), dependency.getValue());
                // Update the view with the found dependency
                LOGGER.info(dependency.getKey() + " depends on: " + dependency.getValue());
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
