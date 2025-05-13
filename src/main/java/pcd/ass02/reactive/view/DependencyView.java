package pcd.ass02.reactive.view;

import pcd.ass02.reactive.controller.DependencyController;

public interface DependencyView {

    void setController(DependencyController controller);

    void start();

    void showError(String message);

    void updateDependencyGraph();

    void stopAnalysis();

    void setDirectory(String directoryPath);
}
