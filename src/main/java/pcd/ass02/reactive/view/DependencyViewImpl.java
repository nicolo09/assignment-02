package pcd.ass02.reactive.view;

import com.brunomnsilva.smartgraph.graph.Digraph;
import com.brunomnsilva.smartgraph.graphview.ForceDirectedSpringSystemLayoutStrategy;
import com.brunomnsilva.smartgraph.graphview.SmartCircularSortedPlacementStrategy;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import com.brunomnsilva.smartgraph.graphview.SmartPlacementStrategy;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import pcd.ass02.reactive.controller.DependencyController;
import pcd.ass02.reactive.model.DependenciesGraph;

public class DependencyViewImpl implements DependencyView {

    // Graph constants
    private static final double REPULSIVE_FORCE = 5;
    private static final double ATTRACTION_FORCE = 1;
    private static final double ATTRACTION_SCALE = 3;
    private static final double ACCELERATION = 0.6;
    private static final double GRAPH_WIDTH = 4000;
    private static final double GRAPH_HEIGHT = 4000;

    private static final String TITLE = "Dependency Analyzer";
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int TOP_HEIGHT_FRAC = 10;
    private static final boolean RESIZABLE = true;

    private static final String ANALYZED_CLASSES_LABEL = "Analyzed project classes (red nodes): ";
    private static final String EXTERNAL_CLASSES_LABEL = "External dependencies (blue nodes): ";
    private static final String FOUND_DEPENDENCIES_LABEL = "Total dependencies found (edges): ";

    private final Stage primaryStage;
    private BorderPane rootBorderPane;
    private SmartGraphPanel<String, String> graphView;
    private final DependenciesGraph dependenciesGraph;
    private Button startButton;
    private Button selectDirectoryButton;
    private Button stopButton;
    private Label selectedDirectoryLabel;
    private Label analyzedProjectClassCounterLabel;
    private Label foundDependencyCounterLabel;
    private Label externalClassesCounterLabel;
    private final Digraph<String, String> graph;

    private DependencyController controller;

    public DependencyViewImpl(final Stage primaryStage, final DependenciesGraph dependenciesGraph) {
        this.primaryStage = primaryStage;
        this.dependenciesGraph = dependenciesGraph;
        this.graph = new DependenciesDigraphWrapper(dependenciesGraph);
        primaryStage.setOnCloseRequest(event -> {
            controller.stopAnalysis();
        });
    }

    public void setController(final DependencyController controller) {
        this.controller = controller;
    }

    public void start() {
        this.primaryStage.setTitle(TITLE);
        this.primaryStage.setWidth(WIDTH);
        this.primaryStage.setHeight(HEIGHT);
        this.primaryStage.setResizable(RESIZABLE);
        this.primaryStage.setScene(this.getMainScene());
        this.primaryStage.show();
        this.graphView.init();
    }

    private Scene getMainScene() {

        // Graph view
        SmartPlacementStrategy initialPlacement = new SmartCircularSortedPlacementStrategy();
        this.graphView = new SmartGraphPanel<>(graph, initialPlacement);
        this.graphView.setAutomaticLayoutStrategy(new ForceDirectedSpringSystemLayoutStrategy<String>(REPULSIVE_FORCE,
                ATTRACTION_FORCE, ATTRACTION_SCALE, ACCELERATION));
        this.graphView.setAutomaticLayout(true);

        var zoomableGraph = new ZoomableSmartGraphPane(graphView);
        zoomableGraph.setGraphSize(GRAPH_WIDTH, GRAPH_HEIGHT);

        // Selected directory label
        selectedDirectoryLabel = new Label("Selected Directory: ");

        // Select directory button
        this.selectDirectoryButton = new Button("Select Directory");
        selectDirectoryButton.setMinSize(Button.USE_PREF_SIZE, Button.USE_PREF_SIZE); // Disable resizing below elision
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        selectDirectoryButton.setOnAction(event -> {
            var selectedDirectory = directoryChooser.showDialog(null);
            if (selectedDirectory != null) {
                controller.setProjectDirectory(selectedDirectory.toPath());
            }
        });

        // Stop button
        this.stopButton = new Button("Stop");
        this.stopButton.setDisable(true);
        this.stopButton.setMinSize(Button.USE_PREF_SIZE, Button.USE_PREF_SIZE); // Disable resizing below elision
        this.stopButton.setOnAction(event -> {
            controller.stopAnalysis();
        });

        // Start button
        this.startButton = new Button("Start");
        this.startButton.setMinSize(Button.USE_PREF_SIZE, Button.USE_PREF_SIZE); // Disable resizing below elision
        this.startButton.setOnAction(event -> {
            try {
                controller.startAnalysis();
                zoomableGraph.centerGraph();
                zoomableGraph.resetZoom();
                this.startButton.setDisable(true);
                this.selectDirectoryButton.setDisable(true);
                this.stopButton.setDisable(false);
            } catch (Exception e) {
                this.showError("Error starting analysis: " + e.getMessage());
            }
        });

        VBox buttonsBox = new VBox(startButton, stopButton);
        buttonsBox.setSpacing(5);
        buttonsBox.setAlignment(javafx.geometry.Pos.CENTER);

        // Zoom in button
        Button zoomInButton = new Button("Zoom In");
        zoomInButton.setOnAction(zoomableGraph.getZoomInHandler());
        
        // Zoom out button
        Button zoomOutButton = new Button("Zoom Out");
        zoomOutButton.setOnAction(zoomableGraph.getZoomOutHandler());

        VBox zoomButtonsBox = new VBox(zoomInButton, zoomOutButton);
        zoomButtonsBox.setSpacing(5);
        zoomButtonsBox.setAlignment(javafx.geometry.Pos.CENTER);

        CheckBox autoLayoutCheckBox = new CheckBox("Auto Layout");
        autoLayoutCheckBox.setSelected(true);
        autoLayoutCheckBox.setOnAction(event -> {
            Platform.runLater(() -> {
                if (autoLayoutCheckBox.isSelected()) {
                    graphView.setAutomaticLayout(true);
                } else {
                    graphView.setAutomaticLayout(false);
                }
            });
        });

        // Analyzed class counter label
        this.analyzedProjectClassCounterLabel = new Label(ANALYZED_CLASSES_LABEL);

        // External class counter label
        this.externalClassesCounterLabel = new Label(EXTERNAL_CLASSES_LABEL);

        // Found dependency counter label
        this.foundDependencyCounterLabel = new Label(FOUND_DEPENDENCIES_LABEL);

        VBox labelsBox = new VBox(this.analyzedProjectClassCounterLabel, this.externalClassesCounterLabel,
                this.foundDependencyCounterLabel);
        labelsBox.setPadding(new Insets(10, 12, 10, 12));

        // Top panel
        HBox top = new HBox(selectedDirectoryLabel, selectDirectoryButton, buttonsBox, zoomButtonsBox,
                autoLayoutCheckBox,
                labelsBox);
        top.setPadding(new Insets(15, 12, 15, 12));
        top.setSpacing(10);
        top.setAlignment(javafx.geometry.Pos.CENTER);
        top.setPrefSize(WIDTH, HEIGHT / TOP_HEIGHT_FRAC);

        // Main border pane
        rootBorderPane = new BorderPane();
        rootBorderPane.setPadding(new Insets(10, 10, 10, 10));
        rootBorderPane.setPrefSize(WIDTH, HEIGHT);
        rootBorderPane.setTop(top);
        rootBorderPane.setCenter(zoomableGraph);

        // Create the root node
        Scene scene = new Scene(rootBorderPane);

        return scene;
    }

    @Override
    public void showError(String message) {
        Platform.runLater(() -> {
            Alert errorAlert = new Alert(AlertType.ERROR);
            errorAlert.setHeaderText("Error");
            errorAlert.setContentText(message);
            errorAlert.showAndWait();
        });
    }

    @Override
    public void updateDependencyGraph() {
        Platform.runLater(() -> {
            graphView.update();
            dependenciesGraph.getAllDependencies().keySet().forEach(v -> {
                var vertex = graphView.getStylableVertex(v);
                if (vertex != null) {
                    vertex.setStyleClass("projectVertex");
                }
            });
            this.analyzedProjectClassCounterLabel
                    .setText(ANALYZED_CLASSES_LABEL + dependenciesGraph.getAllDependencies().keySet().size());
            this.externalClassesCounterLabel.setText(EXTERNAL_CLASSES_LABEL
                    + (this.graph.numVertices() - dependenciesGraph.getAllDependencies().keySet().size()));
            this.foundDependencyCounterLabel.setText(FOUND_DEPENDENCIES_LABEL + this.graph.numEdges());
        });
    }

    @Override
    public void stopAnalysis() {
        Platform.runLater(() -> {
            graphView.update();
            startButton.setDisable(false);
            selectDirectoryButton.setDisable(false);
            stopButton.setDisable(true);
            this.analyzedProjectClassCounterLabel.setText(ANALYZED_CLASSES_LABEL);
            this.externalClassesCounterLabel.setText(EXTERNAL_CLASSES_LABEL);
            this.foundDependencyCounterLabel.setText(FOUND_DEPENDENCIES_LABEL);
        });
    }

    @Override
    public void setDirectory(String directoryPath) {
        Platform.runLater(() -> {
            selectedDirectoryLabel.setText("Selected Directory: " + directoryPath);
        });
    }
}
