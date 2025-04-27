package pcd.ass02.reactive.controller;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import static java.nio.file.StandardWatchEventKinds.*;
import static com.sun.nio.file.ExtendedWatchEventModifier.FILE_TREE;

import io.reactivex.rxjava3.core.Observable;
import pcd.ass02.reactive.model.ReactiveDependencyFinder;
import pcd.ass02.reactive.view.DependencyView;

public class DependencyController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DependencyController.class);

    private DependencyView view;
    private Path projectDirectory;
    private final ReactiveDependencyFinder depsFinder;

    public DependencyController(final ReactiveDependencyFinder depsFinder) {
        this.depsFinder = depsFinder;
    }

    public void setProjectDirectory(final Path projectDirectory) {
        // Set the project directory for analysis
        this.projectDirectory = projectDirectory;
        LOGGER.info("Project directory set to: " + projectDirectory);
    }

    public void startAnalysis() {
        // Start the analysis process
        LOGGER.info("Starting analysis for project directory: " + projectDirectory);
        if (projectDirectory != null) {
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
            depsFinder.findAllClassDependencies(projectDirectory).subscribe(dependency -> {
                // Update the view with the found dependency
                LOGGER.info(dependency.getClassName() + " depends on: " + dependency.getDependencies());
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
