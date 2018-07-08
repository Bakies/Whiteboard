package es.baki.mitchnpals.whiteboard;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.awt.print.PrinterAbortException;
import java.io.File;;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Whiteboard extends Application {
    private Color currentColor = Color.BLUE;
    private Stage primaryStage;
    private Canvas drawingCanvas, borderCanvas;
    private Pane controlPane;
    private GridPane controlDrawContainer;
    private boolean mouseDragged = false, needsRedraw = false, erasing = false;
    private int btnSize = 75, borderThickness = 10, borderPadding = 5, penSize = 7, eraseSize = 30;
    private ColorPicker colorPicker;
    private Button colorPickerBtn, eraseToolBtn, penToolBtn, sizePickerBtn;
    public final static boolean debug = false;

    private BroadcastListener bl;
    private MotionListener ml;

    public static void main(String... strings) throws IOException {
        debugFile = new File("playback.txt");
        if (debug)
            debugFilePrinter = new PrintWriter(new FileWriter(debugFile));
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
        primaryStage.setOnCloseRequest((windowEvent) -> {
            bl.appClosed();
            ml.appClosed();
            try {
                bl.wait();
                ml.wait();
            } catch (InterruptedException e) {
                // Dont care
            }
            System.exit(0);
        });

        drawingCanvas = new Canvas(primaryStage.getWidth(), primaryStage.getHeight());
        GraphicsContext gc = drawingCanvas.getGraphicsContext2D();

        makeBorderCanvas();

        redrawBorderCanvas();
        gc.setFill(Color.BLUE);

        // Resize the width of the drawable surface automatically to match the size of the window
        s.widthProperty().addListener((obs, oldVal, newVal) -> {
            System.out.printf("Width changed from %d to %d%n", oldVal.intValue(), newVal.intValue());
            drawingCanvas.setWidth(newVal.doubleValue() - ((borderPadding + borderThickness) * 2));
            borderCanvas.setWidth(newVal.doubleValue());
            redrawBorderCanvas(); // needsRedraw = true;
        });

        // Resize the height of the drawable surface automatically to match the size of the window
        s.heightProperty().addListener((obs, oldVal, newVal) -> {
            System.out.printf("Height changed from %d to %d%n", oldVal.intValue(), newVal.intValue());
            drawingCanvas.setHeight(newVal.doubleValue() - ((borderPadding + borderThickness) * 2));
            borderCanvas.setHeight(newVal.doubleValue());
            redrawBorderCanvas(); // needsRedraw = true;
        });

        addMouseListeners(drawingCanvas);

        makeMenuPane();
        controlDrawContainer = new GridPane();
        controlDrawContainer.setHgap(5);
        controlDrawContainer.setVgap(5);

        controlDrawContainer.add(drawingCanvas, 0, 0, 2, 2);
        controlDrawContainer.add(controlPane, 0, 0);

        root.getChildren().addAll(borderCanvas, controlDrawContainer);
        controlDrawContainer.setTranslateX(borderPadding + borderThickness);
        controlDrawContainer.setTranslateY(borderPadding + borderThickness);

        // Starts the server listening for motion controls
        (ml = new MotionListener(this)).start();

        // Starts the multicast receiver for network discovery
        try {
            (bl = new BroadcastListener()).start();
        } catch (IOException e) {
            System.err.println("Could not make server socket, already running?");
        }

    }

    private void redrawBorderCanvas() {
        GraphicsContext gc = borderCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, borderCanvas.getWidth(), borderCanvas.getHeight());

        System.out.printf("Border Canvas Dimensions: w:%f h:%f%n", borderCanvas.getWidth(), borderCanvas.getHeight());
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

    private void makeMenuPane() {
        controlPane = new VBox(5);
        controlPane.setPadding(new Insets(10, 0, 0, 10));

        colorPicker = new ColorPicker(Color.BLUE);
        colorPicker.setPrefSize(btnSize, btnSize);
        colorPicker.getStyleClass().add("button");
        colorPicker.setStyle("-fx-background-color: #0000FF;");
        colorPicker.setOnAction(e -> {
            controlDrawContainer.getChildren().remove(colorPicker);
            colorPicker.setStyle(String.format("-fx-background-color: #%s;",
                    colorPicker.getValue().toString().substring(2, 8)));
            System.out.println(colorPicker.getValue().toString().substring(2, 8));
            currentColor = colorPicker.getValue();
        });

        penToolBtn = new Button("Pen");
        penToolBtn.setStyle("-fx-background-color: #0000FF;");
        penToolBtn.setPrefSize(btnSize, btnSize);
        penToolBtn.setDefaultButton(true);
        penToolBtn.setOnAction(e -> {
            penToolBtn.setDefaultButton(true);
            eraseToolBtn.setDefaultButton(false);

            erasing = false;
        });

        eraseToolBtn = new Button("Erase");
        eraseToolBtn.setStyle("-fx-background-color: #0000FF;");
        eraseToolBtn.setPrefSize(btnSize, btnSize);
        eraseToolBtn.setOnAction(e -> {
            eraseToolBtn.setDefaultButton(true);
            penToolBtn.setDefaultButton(false);

            erasing = true;
        });


        sizePickerBtn = new Button("Size");
        sizePickerBtn.setStyle("-fx-background-color: #0000FF;");
        sizePickerBtn.setPrefSize(btnSize, btnSize);

        controlPane.getChildren().addAll(penToolBtn, eraseToolBtn, colorPicker, sizePickerBtn);
    }

    private Canvas makeBorderCanvas() {
        borderCanvas = new Canvas(primaryStage.getWidth(), primaryStage.getHeight());

        return borderCanvas;
    }

    private void addMouseListeners(Canvas canvas) {
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            onMousePressed(event.getX(), event.getY());
        });

        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            onMouseClicked(event.getX(), event.getY());
        });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            onMouseDragged(event.getX(), event.getY());
        });

        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            onMouseReleased();
        });
    }

    public static File debugFile = new File("playback.txt");
    public static PrintWriter debugFilePrinter;
    public void onMouseReleased() {
        GraphicsContext gc = drawingCanvas.getGraphicsContext2D();
        System.out.printf("Mouse Released Event%n");
        if (debug) {
            debugFilePrinter.printf("up%n");
            debugFilePrinter.flush();
        }
        gc.save();
    }

    public void onMousePressed(double x, double y) {
        GraphicsContext gc = drawingCanvas.getGraphicsContext2D();
        System.out.printf("Mouse Pressed Event @ %f %f%n", x, y);
        if (debug)
            debugFilePrinter.printf("down,%f,%f%n", x, y);
        gc.beginPath();
        gc.moveTo(x, y);
        gc.stroke();
    }

    public void onMouseDragged(double x, double y) {
        GraphicsContext gc = drawingCanvas.getGraphicsContext2D();
        System.out.printf("Mouse Dragged Event @ %f %f%n", x, y);
        if (debug)
            debugFilePrinter.printf("%f,%f%n", x, y);
        gc.lineTo(x, y);
        if (erasing) {
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(eraseSize);
        } else {
            gc.setStroke(currentColor);
            gc.setLineWidth(penSize);
        }
        gc.stroke();
        mouseDragged = true;

    }

    public void onMouseClicked(double x, double y) {
        System.out.printf("Mouse Click Event @ %f %f%n", x, y);
        if (mouseDragged) {
            // drawMenuOnCanvas(canvas);
        } else {
            System.out.println("Not a drag");
            if (needsRedraw)
                redrawBorderCanvas();

        }
        mouseDragged = false;
    }

    public int getHeight() {
        return (int) drawingCanvas.getHeight();
    }
    public int getWidth() {
        return (int) drawingCanvas.getWidth();
    }
}

