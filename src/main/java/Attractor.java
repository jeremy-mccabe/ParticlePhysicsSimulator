

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;

/**
 * A node which calculates a attraction force for particles
 */
public class Attractor extends Sprite {

    double factor = 1.0; // similar to repeller, but with +1 factor

    public Attractor( Vector2D location, Vector2D velocity, Vector2D acceleration, double width, double height) {

        super( location, velocity, acceleration, width, height);

    }

    /**
     * Circle with a label
     */
    @Override
    public Node createView() {

        Group group = new Group();

        double radius = width / 2;

        Circle circle = new Circle( radius);

        circle.setCenterX(radius);
        circle.setCenterY(radius);

        circle.setStroke(Color.RED);
        circle.setFill(Color.RED.deriveColor(1, 1, 1, 0.3));

        group.getChildren().add( circle);

        Text text = new Text( "Attractor");
        text.setStroke(Color.RED);
        text.setFill(Color.RED);
        text.setBoundsType(TextBoundsType.VISUAL);

        text.relocate(radius - text.getLayoutBounds().getWidth() / 2, radius - text.getLayoutBounds().getHeight() / 2);

        group.getChildren().add( text);

        return group;
    }

    /**
     * Attraction force
     */
    public Vector2D getForce(Particle particle) {

        // calculate direction of force
        Vector2D dir = Vector2D.subtract(position, particle.position);

        // get distance (constrain distance)
        double distance = dir.magnitude(); // distance between objects
        dir.normalize(); // normalize vector (distance doesn't matter here, we just want this vector for direction)
        distance = Utils.clamp(distance, 5, 1000); // keep distance within a reasonable range

        // calculate magnitude
        double force = factor * Settings.get().getAttractorStrength() / (distance * distance); // repelling force is inversely proportional to distance

        // make a vector out of direction and magnitude
        dir.multiply(force); // get force vector => magnitude * direction

        return dir;

    }

}
