package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;

public class HelloController {

    @FXML private Canvas canvas;
    @FXML private ColorPicker colorPicker;
    @FXML private Slider brushSizeSlider;

    private GraphicsContext gc;
    private List<Shape> shapes = new ArrayList<>();
    private List<BrushStroke> brushStrokes = new ArrayList<>();
    private Shape selectedShape = null;
    private Memento temp = null;
    private double offsetX, offsetY;
    private String selectedTool = "rectangle";
    private Color currentColor = Color.RED;
    private boolean isDragging = false;
    private boolean isBrushMode = false;
    private double brushSize = 5;

    @FXML
    public void initialize() {
        gc = canvas.getGraphicsContext2D();
        colorPicker.setValue(currentColor);
        brushSizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> brushSize = newVal.doubleValue());
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, this::onMousePressed);
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onMouseDragged);
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, this::onMouseReleased);
    }

    private void redrawCanvas() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        for (BrushStroke b : brushStrokes) {
            gc.setFill(b.color);
            gc.fillOval(b.x - b.size/2.0, b.y - b.size/2.0, b.size, b.size);
        }
        for (Shape s : shapes) s.draw(gc);
    }

    private void onMousePressed(MouseEvent event) {
        if (isBrushMode) {
            BrushStroke b = new BrushStroke(event.getX(), event.getY(), brushSize, currentColor);
            brushStrokes.add(b);
            gc.setFill(currentColor);
            gc.fillOval(event.getX() - brushSize/2.0, event.getY() - brushSize/2.0, brushSize, brushSize);
            return;
        }
        for (int i = shapes.size() - 1; i >= 0; i--) {
            Shape s = shapes.get(i);
            if (s.contains(event.getX(), event.getY())) {
                selectedShape = s;
                temp = new Memento(s);
                offsetX = event.getX() - s.getX();
                offsetY = event.getY() - s.getY();
                isDragging = true;
                return;
            }
        }
        Shape shape = ShapeFactory.createShape(selectedTool, event.getX(), event.getY(), currentColor);
        if (shape != null) {
            shapes.add(shape);
            redrawCanvas();
        }
    }

    private void onMouseDragged(MouseEvent event) {
        if (isBrushMode) {
            BrushStroke b = new BrushStroke(event.getX(), event.getY(), brushSize, currentColor);
            brushStrokes.add(b);
            gc.setFill(currentColor);
            gc.fillOval(event.getX() - brushSize/2.0, event.getY() - brushSize/2.0, brushSize, brushSize);
            return;
        }
        if (isDragging && selectedShape != null) {
            selectedShape.setPosition(event.getX() - offsetX, event.getY() - offsetY);
            redrawCanvas();
        }
    }

    private void onMouseReleased(MouseEvent event) {
        if (isDragging && selectedShape != null && temp != null) {
            temp.restoreVisual();
            temp = null;
            selectedShape = null;
        }
        isDragging = false;
    }

    @FXML private void onCircleClicked() { selectedTool = "circle"; isBrushMode = false; }
    @FXML private void onTriangleClicked() { selectedTool = "triangle"; isBrushMode = false; }
    @FXML private void onRectangleClicked() { selectedTool = "rectangle"; isBrushMode = false; }
    @FXML private void onBrushClicked() { isBrushMode = true; }
    @FXML private void onColorChanged() { currentColor = colorPicker.getValue(); }

    abstract static class Shape {
        protected Color color;
        protected double x, y;
        Shape(double x, double y, Color color) { this.color = color; this.x = x; this.y = y; }
        abstract void draw(GraphicsContext gr);
        abstract boolean contains(double x, double y);
        public void setColor(Color color) { this.color = color; }
        public void setPosition(double x, double y) { this.x = x; this.y = y; }
        public double getX() { return x; }
        public double getY() { return y; }
    }

    static class Circle extends Shape {
        private double radius = 25;
        Circle(double x, double y, Color color) { super(x, y, color); }
        void draw(GraphicsContext gr) { gr.setFill(color); gr.fillOval(x - radius, y - radius, radius * 2, radius * 2); }
        boolean contains(double mx, double my) { return Math.pow(mx - x, 2) + Math.pow(my - y, 2) <= Math.pow(radius, 2); }
    }

    static class Rectangle extends Shape {
        private double width = 60, height = 40;
        Rectangle(double x, double y, Color color) { super(x, y, color); }
        void draw(GraphicsContext gr) { gr.setFill(color); gr.fillRect(x, y, width, height); }
        boolean contains(double mx, double my) { return mx >= x && mx <= x + width && my >= y && my <= y + height; }
    }

    static class Triangle extends Shape {
        private double size = 50;
        Triangle(double x, double y, Color color) { super(x, y, color); }
        void draw(GraphicsContext gr) {
            gr.setFill(color);
            double[] xPoints = {x, x - size / 2, x + size / 2};
            double[] yPoints = {y - size / 2, y + size / 2, y + size / 2};
            gr.fillPolygon(xPoints, yPoints, 3);
        }
        boolean contains(double mx, double my) {
            return mx >= x - size / 2 && mx <= x + size / 2 && my >= y - size / 2 && my <= y + size / 2;
        }
    }

    static class ShapeFactory {
        static Shape createShape(String type, double x, double y, Color color) {
            switch (type.toLowerCase()) {
                case "circle": return new Circle(x, y, color);
                case "triangle": return new Triangle(x, y, color);
                case "rectangle": return new Rectangle(x, y, color);
                default: return null;
            }
        }
    }

    static class Memento {
        private Shape shape;
        private Color color;
        Memento(Shape state) {
            this.shape = state;
            this.color = state.color;
        }
        void restoreVisual() {
            shape.setColor(color);
        }
    }

    static class BrushStroke {
        double x, y, size;
        Color color;
        BrushStroke(double x, double y, double size, Color color) { this.x = x; this.y = y; this.size = size; this.color = color; }
    }
}
