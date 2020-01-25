
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

/**
 * Allow dragging of attractors and repellers
 */
public class MouseGestures {

    private final DragContext dragContext = new DragContext();

    void makeDraggable(final Sprite sprite) {

        sprite.setOnMousePressed(onMousePressedEventHandler);
        sprite.setOnMouseDragged(onMouseDraggedEventHandler);
        sprite.setOnMouseReleased(onMouseReleasedEventHandler);
    }

    private EventHandler<MouseEvent> onMousePressedEventHandler = event -> {

        dragContext.x = event.getSceneX();
        dragContext.y = event.getSceneY();
    };

    private EventHandler<MouseEvent> onMouseDraggedEventHandler = event -> {

        Sprite sprite = (Sprite) event.getSource();

        double offsetX = event.getSceneX() - dragContext.x;
        double offsetY = event.getSceneY() - dragContext.y;

        sprite.setLocationOffset(offsetX, offsetY);

        dragContext.x = event.getSceneX();
        dragContext.y = event.getSceneY();
    };

    private EventHandler<MouseEvent> onMouseReleasedEventHandler = event -> { };

    class DragContext {
        double x;
        double y;
    }

}