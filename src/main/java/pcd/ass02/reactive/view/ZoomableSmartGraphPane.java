package pcd.ass02.reactive.view;

import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;

public class ZoomableSmartGraphPane extends StackPane {

    private final SmartGraphPanel<String, String> graphView;
    private final ScrollPane scrollPane;
    private final Group graphGroup;

    private static final double MIN_SCALE = 0.75;
    private static final double MAX_SCALE = 3.0;

    private static final double ZOOM_FACTOR = 1.1;

    public ZoomableSmartGraphPane(SmartGraphPanel<String, String> graphView) {
        this.graphView = graphView;
        this.graphGroup = new Group(graphView);
        this.scrollPane = new ScrollPane(graphGroup);

        configureScrollPane();
        configureScrollZoom();

        this.getChildren().add(scrollPane);
    }

    private void configureScrollPane() {
        scrollPane.setPannable(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        centerGraph();
    }

    private void configureScrollZoom() {
        scrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.isControlDown()) {
                event.consume();

                double factor = (event.getDeltaY() > 0) ? ZOOM_FACTOR : 1 / ZOOM_FACTOR;

                // Mouse nella scena → coordinate locali nel grafo
                Point2D mouseInGraph = graphView.sceneToLocal(event.getSceneX(), event.getSceneY());

                zoomToPoint(factor, mouseInGraph);
            }
        });
    }

    private void zoomTowardsViewportCenter(double factor) {
        double viewportWidth = scrollPane.getViewportBounds().getWidth();
        double viewportHeight = scrollPane.getViewportBounds().getHeight();

        // Punto centrale visibile nella viewport, in coordinate del ScrollPane
        Point2D centerInScrollPane = new Point2D(viewportWidth / 2, viewportHeight / 2);

        // Converti alle coordinate del grafo
        Point2D centerInGraph = graphView.sceneToLocal(
                scrollPane.localToScene(centerInScrollPane));

        zoomToPoint(factor, centerInGraph);
    }

    private void zoomToPoint(double factor, Point2D zoomCenterInGraphCoords) {
        double oldScale = graphView.getScaleX();
        double newScale = clamp(oldScale * factor, MIN_SCALE, MAX_SCALE);

        if (newScale != oldScale) {
            graphView.setScaleX(newScale);
            graphView.setScaleY(newScale);

            Platform.runLater(() -> {
                double contentWidth = graphView.getBoundsInParent().getWidth();
                double contentHeight = graphView.getBoundsInParent().getHeight();
                double viewportWidth = scrollPane.getViewportBounds().getWidth();
                double viewportHeight = scrollPane.getViewportBounds().getHeight();

                double scrollH = (zoomCenterInGraphCoords.getX() * newScale - viewportWidth / 2)
                        / (contentWidth - viewportWidth);
                double scrollV = (zoomCenterInGraphCoords.getY() * newScale - viewportHeight / 2)
                        / (contentHeight - viewportHeight);

                scrollPane.setHvalue(clamp(scrollH, 0, 1));
                scrollPane.setVvalue(clamp(scrollV, 0, 1));
            });
        }
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Get the SmartGraphPanel associated with this ZoomableSmartGraphPane.
     * 
     * @return the SmartGraphPanel
     */
    public SmartGraphPanel<String, String> getGraphView() {
        return this.graphView;
    }

    /**
     * Set the size of the SmartGraphPanel.
     * 
     * @param width
     * @param height
     */
    public void setGraphSize(double width, double height) {
        this.graphView.setPrefSize(width, height);
    }

    /**
     * Set the width of the SmartGraphPanel.
     * 
     * @param width
     */
    public void setGraphWidth(double width) {
        this.graphView.setPrefWidth(width);
    }

    /**
     * Set the height of the SmartGraphPanel.
     * 
     * @param height
     */
    public void setGraphHeight(double height) {
        this.graphView.setPrefHeight(height);
    }

    /**
     * Center the graph in the ScrollPane.
     */
    public void centerGraph() {
        Platform.runLater(() -> {
            scrollPane.setHvalue(0.5);
            scrollPane.setVvalue(0.5);
        });
    }

    /**
     * Get the event handler for zooming in.
     * 
     * @return an event handler
     */
    public EventHandler<ActionEvent> getZoomInHandler() {
        return event -> zoomTowardsViewportCenter(ZOOM_FACTOR);
    }

    /**
     * Get the event handler for zooming out.
     * 
     * @return an event handler
     */
    public EventHandler<ActionEvent> getZoomOutHandler() {
        return event -> zoomTowardsViewportCenter(1 / ZOOM_FACTOR);
    }

    /**
     * Reset the zoom level of the graph.
     */
    public void resetZoom() {
        graphView.setScaleX(1.0);
        graphView.setScaleY(1.0);

        Platform.runLater(() -> {
            scrollPane.setHvalue(0.5);
            scrollPane.setVvalue(0.5);
        });
    }

}
