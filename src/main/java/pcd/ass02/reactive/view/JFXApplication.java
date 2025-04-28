package pcd.ass02.reactive.view;

import javafx.application.Application;
import javafx.stage.Stage;
import pcd.ass02.reactive.controller.DependencyController;
import pcd.ass02.reactive.model.DependenciesGraph;
import pcd.ass02.reactive.model.ReactiveDependencyFinder;

public class JFXApplication extends Application {

    @Override
    public void start(final Stage primaryStage) {
        var depsFinder = new ReactiveDependencyFinder();
        var dependenciesGraph = new DependenciesGraph();
        var controller = new DependencyController(depsFinder, dependenciesGraph);
        DependencyView view = new DependencyViewImpl(primaryStage, dependenciesGraph);
        view.setController(controller);
        controller.setView(view);
        view.start();
    }    
}
