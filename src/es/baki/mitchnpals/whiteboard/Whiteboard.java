package es.baki.mitchnpals.whiteboard;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.*;

public class Whiteboard extends Application {
    private Color currentColor = Color.BLUE;
    private Stage primaryStage;
    private Canvas drawingCanvas, menuCanvas, borderCanvas;
    private boolean mouseDragged = false, needsRedraw = false;

    public static void main(String... strings) {
        // Starts the multicast receiver for network discovery
        try {
            new BroadcastListener().start();
        } catch (IOException e) {
            System.err.println("Could not start multicast listener");
            e.printStackTrace();
        }
        // Starts the GUI
        launch();
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        Group root = new Group();
        Scene s = new Scene(root, 300, 300, Color.BLACK);
        primaryStage.setScene(s);
        primaryStage.show();

        drawingCanvas = new Canvas(primaryStage.getWidth(), primaryStage.getHeight());
        GraphicsContext gc = drawingCanvas.getGraphicsContext2D();

        makeBorderCanvas();
        makeMenuCanvas();

        redrawBorderCanvas();
        gc.setFill(Color.BLUE);

        // Resize the width of the drawable surface automatically to match the size of the window
        s.widthProperty().addListener((obs, oldVal, newVal) -> {
            System.out.printf("Width changed from %d to %d%n", oldVal.intValue(), newVal.intValue());
            drawingCanvas.setWidth(newVal.doubleValue());
            borderCanvas.setWidth(newVal.doubleValue());
            redrawBorderCanvas(); // needsRedraw = true;
        });

        // Resize the height of the drawable surface automatically to match the size of the window
        s.heightProperty().addListener((obs, oldVal, newVal) -> {
            System.out.printf("Height changed from %d to %d%n", oldVal.intValue(), newVal.intValue());
            drawingCanvas.setHeight(newVal.doubleValue());
            borderCanvas.setHeight(newVal.doubleValue());
            redrawBorderCanvas(); // needsRedraw = true;
        });

        addMouseListeners(drawingCanvas);

        root.getChildren().add(borderCanvas);
        root.getChildren().add(drawingCanvas);
        root.getChildren().add(menuCanvas);

    }

    private void redrawBorderCanvas() {
        GraphicsContext gc = borderCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, borderCanvas.getWidth(), borderCanvas.getHeight());

        System.out.printf("Border Canvas Dimensions: w:%f h:%f%n", borderCanvas.getWidth(), borderCanvas.getHeight());
        int borderThickness = 5, borderPadding = 5;
        double width = borderCanvas.getWidth(), height = borderCanvas.getHeight();
        gc.setFill(Color.WHITE);
        // Top Border
        gc.fillRect(borderPadding, borderPadding, width - borderPadding * 2, borderThickness);
        // Left Border
        gc.fillRect(borderPadding, borderPadding, borderThickness, height - borderPadding * 2);
        // Bottom Border
        gc.fillRect(borderPadding, height - borderThickness - borderPadding, width - borderPadding * 2, borderThickness);
        // Right Border
        gc.fillRect(width - borderThickness - borderPadding, borderPadding, borderThickness, height - borderPadding * 2);
    }

    private Canvas makeMenuCanvas() {
        menuCanvas = new Canvas(100, 100);
        GraphicsContext gc = menuCanvas.getGraphicsContext2D();

        gc.setFill(Color.LIGHTGRAY);
        // gc.fillRoundRect(10, 10, 20, 20, 10, 10);

        return menuCanvas;
    }

    private Canvas makeBorderCanvas() {
        borderCanvas = new Canvas(primaryStage.getWidth(), primaryStage.getHeight());

        return borderCanvas;
    }

    private void addMouseListeners(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            System.out.printf("Mouse Pressed Event @ %f %f%n", event.getX(), event.getY());
            gc.beginPath();
            gc.moveTo(event.getX(), event.getY());
            gc.stroke();
        });
 
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, event ->{
            System.out.printf("Mouse Click Event @ %f %f%n", event.getX(), event.getY());
            if (mouseDragged) {
                // drawMenuOnCanvas(canvas);
            } else {
                System.out.println("Not a drag");
                if (needsRedraw)
                    redrawBorderCanvas();

            }
            mouseDragged = false;
        });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            System.out.printf("Mouse Dragged Event @ %f %f%n", event.getX(), event.getY());
            gc.lineTo(event.getX(), event.getY());
            gc.setStroke(currentColor);
            gc.stroke();
            mouseDragged = true;
        });
 
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, event ->  {
            System.out.printf("Mouse Released Event @ %f %f%n", event.getX(), event.getY());
            gc.save();
        });
    }
}
