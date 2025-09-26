package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;

public class HelloController {

    @FXML
    private Canvas canvas;

    @FXML
    private ColorPicker colorPicker;

    private String selected = "rectangle";
    private Color currentColor = Color.RED;

    @FXML
    public void initialize() {
        colorPicker.setValue(currentColor);

        canvas.setOnMouseClicked(event -> {
            double x = event.getX();
            double y = event.getY();
            Shape shape;

            switch (selected) {
                case "circle":
                    shape = new Circle(x, y, 25, currentColor);
                    break;
                case "triangle":
                    shape = new Triangle(x, y, 40, currentColor);
                    break;
                case "rectangle":
                default:
                    shape = new Rectangle(x, y, 50, 30, currentColor);
                    break;
            }

            shape.draw(canvas.getGraphicsContext2D());
        });
    }

    @FXML
    private void onCircleClicked() {
        selected = "circle";
    }

    @FXML
    private void onTriangleClicked() {
        selected = "triangle";
    }

    @FXML
    private void onRectangleClicked() {
        selected = "rectangle";
    }

    @FXML
    private void onColorChanged() {
        currentColor = colorPicker.getValue();
    }
}

abstract class Shape {
    protected Color color;
    protected double x, y;
    abstract void draw(GraphicsContext gr);

    public Shape(Color color) {
        System.out.println("Shape constructor called");
        this.color = color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}

class Rectangle extends Shape {
    private double width, height;

    public Rectangle(double x, double y, double width, double height, Color color) {
        super(color);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    void draw(GraphicsContext gr) {
        gr.setFill(color);
        gr.fillRect(x, y, width, height);
    }
}

class Circle extends Shape {
    private double radius;
    public Circle(double x, double y, double radius, Color color) {
        super(color);
        this.x = x;
        this.y = y;
        this.radius = radius;
    }
    @Override
    void draw(GraphicsContext gr) {
        gr.setFill(color);
        gr.fillOval(x - radius, y - radius, radius * 2, radius * 2);
    }
}

class Triangle extends Shape {
    private double size;
    public Triangle(double x, double y, double size, Color color) {
        super(color);
        this.x = x;
        this.y = y;
        this.size = size;
    }
    @Override
    void draw(GraphicsContext gr) {
        gr.setFill(color);
        double[] xPoints = {x, x - size/2, x + size/2};
        double[] yPoints = {y - size/2, y + size/2, y + size/2};

        gr.fillPolygon(xPoints, yPoints, 3);
    }
}