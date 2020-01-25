
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.jetbrains.annotations.Nullable;

public class StencilMarker extends Node {

    private Vector2D location;
    private Color color;

    StencilMarker(@Nullable Vector2D location, @Nullable Color color) {
        this.location = location;
        this.color = color;
    }

    public Vector2D getLocation() {
        return location;
    }

    public void setLocation(double x, double y) {
        this.location.x = x;
        this.location.y = y;
    }

    Node createView() {

        double radius = (Settings.get().getPaddleRadius() / 2) * 1.20;

        Circle circle = new Circle( radius);
        circle.setCenterX(radius);
        circle.setCenterY(radius);

        if (color != null) {
            circle.setStroke(color);
            circle.setFill(color.deriveColor(1, 1, 1, 0.3));
        } else {
            circle.setStroke(Color.BLUE);
            circle.setFill(Color.BLUE.deriveColor(1, 1, 1, 0.3));
        }

        return circle;
    }
}
