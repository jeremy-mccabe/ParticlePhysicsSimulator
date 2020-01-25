
import javafx.scene.Node;
import javafx.scene.layout.Region;

/**
 * Sprite base class:
 * Contains information necessary for the position, velocity, and acceleration of a particle.
 */
public abstract class Sprite extends Region {

    Vector2D position;
    Vector2D velocity;
    Vector2D acceleration;

    private double maxSpeed = Settings.get().getParticleMaxSpeed();
    private double radius;

    Node view;

    double width;
    double height;
    double centerX;
    double centerY;

    double angle;

    double lifeSpanMax = Settings.get().getParticleLifeSpanMax() - 1; // -1 because we want [0..255] for an amount of 256; otherwise we'd get DivByZero in the logic; will fix it later
    double lifeSpan = lifeSpanMax;

    public Sprite(Vector2D position, Vector2D velocity, Vector2D acceleration, double width, double height) {

        this.position = position;
        this.velocity = velocity;
        this.acceleration = acceleration;

        this.width = width;
        this.height = height;
        this.centerX = width / 2;
        this.centerY = height / 2;

        this.radius = width / 2;

        this.view = createView();

        setPrefSize(width, height);

        if (this.view != null) {
            getChildren().add(view);
        }
    }

    public abstract Node createView();

    public void applyForce(Vector2D force) {

        acceleration.add(force);
    }

    /**
     * Standard movement method: calculate velocity depending on accumulated acceleration force, then calculate the position.
     * Reset acceleration so that it can be recalculated in the next animation step.
     */
    public void move() {
        // set velocity depending on acceleration
        velocity.add(acceleration);
        // limit velocity to max speed
        velocity.limit(maxSpeed);
        // change position depending on velocity
        position.add(velocity);
        // angle: towards velocity (i.e., target)
        angle = velocity.angle();
        // clear acceleration
        acceleration.multiply(0);
    }

    /**
     * Update node position
     */
    public void display() {
        // position
        relocate(position.x - centerX, position.y - centerY);
        // rotation
        setRotate(Math.toDegrees(angle));
    }


    public Vector2D getVelocity() {
        return velocity;
    }

    public Vector2D getPosition() {
        return position;
    }

    // normalizes acceleration value for processing
    public double normalizedAcceleration() {
        return (acceleration.magnitude() % 10);
    }

    public void setLocation( double x, double y) {
        position.x = x;
        position.y = y;
    }

    public void setLocationOffset( double x, double y) {
        position.x += x;
        position.y += y;
    }

    public void decreaseLifeSpan() {
    }

    public boolean isDead() {
        if (lifeSpan <= 0.0) {
            return true;
        } else {
            return false;
        }
    }

    public int getLifeSpan() {
        return (int) lifeSpan;
    }

}
