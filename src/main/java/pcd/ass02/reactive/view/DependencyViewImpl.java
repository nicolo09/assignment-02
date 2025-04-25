package pcd.ass02.reactive.view;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import pcd.ass02.reactive.controller.DependencyController;

public class DependencyViewImpl implements DependencyView {

    private static final String TITLE = "Dependency Analyzer";
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int TOP_HEIGHT_FRAC = 10;
    private static final boolean RESIZABLE = true;
    private final Stage primaryStage;

    private DependencyController controller;

    public DependencyViewImpl(final Stage primaryStage) {
        this.primaryStage = primaryStage;
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
    }

    private Scene getMainScene() {
        // Selected directory label
        Label selectedDirectoryLabel = new Label("Selected Directory: ");

        // Select directory button
        Button selectDirectoryButton = new Button("Select Directory");
        selectDirectoryButton.setMinSize(Button.USE_PREF_SIZE, Button.USE_PREF_SIZE); // Disable resizing below elision
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        selectDirectoryButton.setOnAction(event -> {
            var selectedDirectory = directoryChooser.showDialog(null);
            if (selectedDirectory != null) {
                controller.setProjectDirectory(selectedDirectory.toPath());
            }
        });

        // Start button
        Button startButton = new Button("Start");
        startButton.setMinSize(Button.USE_PREF_SIZE, Button.USE_PREF_SIZE); // Disable resizing below elision
        startButton.setOnAction(event -> {
            controller.startAnalysis();
        });

        // Top panel
        HBox top = new HBox(selectedDirectoryLabel, selectDirectoryButton, startButton);
        top.setPadding(new Insets(15, 12, 15, 12));
        top.setSpacing(10);
        top.setAlignment(javafx.geometry.Pos.CENTER);
        top.setPrefSize(WIDTH, HEIGHT / TOP_HEIGHT_FRAC);

        // Main panel
        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(new Pane()); // TODO: Placeholder for the main content
        root.setPrefSize(WIDTH, HEIGHT);

        // Create the root node
        Scene scene = new Scene(root);

        return scene;
    }

    @Override
    public void showError(String message) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'showError'");
    }

}
