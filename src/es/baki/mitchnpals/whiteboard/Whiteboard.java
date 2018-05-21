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

public class Whiteboard extends Application {
	private Color currentColor = Color.BLUE;
	private Canvas menuCanvas;
	private Canvas borderCanvas;
	private boolean mouseDragged = false, needsRedraw = false;
	private Canvas drawingCanvas;

	public static void main(String... strings) {
		launch();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		Group root = new Group();
		Scene s = new Scene(root, 300, 300, Color.BLACK);

		drawingCanvas = new Canvas(250, 250);
		GraphicsContext gc = drawingCanvas.getGraphicsContext2D();

		makeBorderCanvas();
		makeMenuCanvas();

		redrawBorderCanvas();
		gc.setFill(Color.BLUE);

		// Resize the width of the drawable surface automatically to match the
		// size of the window
		primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> {
			System.out.printf("Width changed from %d to %d%n", oldVal.intValue(), newVal.intValue());
			drawingCanvas.setWidth(newVal.doubleValue());
			borderCanvas.setWidth(newVal.doubleValue());
			redrawBorderCanvas(); // needsRedraw = true;
		});

		// Resize the height of the drawable surface automatically to match the
		// size of the window
		primaryStage.heightProperty().addListener((obs, oldVal, newVal) -> {
			System.out.printf("Height changed from %d to %d%n", oldVal.intValue(), newVal.intValue());
			drawingCanvas.setHeight(newVal.doubleValue());
			borderCanvas.setHeight(newVal.doubleValue());
			redrawBorderCanvas(); // needsRedraw = true;
		});

		addMouseListeners(drawingCanvas);

		root.getChildren().add(borderCanvas);
		root.getChildren().add(drawingCanvas);
		root.getChildren().add(menuCanvas);

		primaryStage.setScene(s);
		primaryStage.show();
	}

	private void redrawBorderCanvas() {
		GraphicsContext gc = borderCanvas.getGraphicsContext2D();
		gc.clearRect(0, 0, borderCanvas.getWidth(), borderCanvas.getHeight());
		gc.setLineWidth(20);
		gc.setStroke(Color.WHITE);
		gc.setLineDashes(50d);
		gc.setStroke(Color.RED);
		gc.strokeLine(0, 0, borderCanvas.getWidth(), 0);
		gc.setStroke(Color.GREEN);
		gc.strokeLine(0, 0, 0, borderCanvas.getHeight());
		gc.setStroke(Color.BLUE);
		gc.strokeLine(0, borderCanvas.getHeight() - 20, borderCanvas.getWidth(), borderCanvas.getHeight() - 20);
		gc.setStroke(Color.YELLOW);
		gc.strokeLine(borderCanvas.getWidth() - 5, 0, borderCanvas.getWidth() - 5, borderCanvas.getHeight());
	}

	private Canvas makeMenuCanvas() {
		menuCanvas = new Canvas(30, 30);
		GraphicsContext gc = menuCanvas.getGraphicsContext2D();

		gc.setFill(Color.LIGHTGRAY);
		gc.fillRoundRect(10, 10, 20, 20, 10, 10);

		return menuCanvas;
	}

	private Canvas makeBorderCanvas() {
		borderCanvas = new Canvas(1000, 1000);

		return borderCanvas;
	}

	private void addMouseListeners(Canvas canvas) {
		GraphicsContext gc = canvas.getGraphicsContext2D();
		canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent event) {
			System.out.printf("Mouse Pressed Event @ %f %f%n", event.getX(), event.getY());
			gc.beginPath();
			gc.moveTo(event.getX(), event.getY());
			gc.stroke();
		}
		});
 
		canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				System.out.printf("Mouse Click Event @ %f %f%n", event.getX(), event.getY());
				if (mouseDragged) {
					// drawMenuOnCanvas(canvas);
				} else {
					System.out.println("Not a drag");
					if (needsRedraw)
						redrawBorderCanvas();

				}
				mouseDragged = false;
			}
		});

		canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				System.out.printf("Mouse Dragged Event @ %f %f%n", event.getX(), event.getY());
				gc.lineTo(event.getX(), event.getY());
				gc.setStroke(currentColor);
				gc.stroke();
				mouseDragged = true;
			}
		});
 
		canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				System.out.printf("Mouse Released Event @ %f %f%n", event.getX(), event.getY());
				gc.save();
			}
		});
	}
}
