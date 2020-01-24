
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Application settings
 */
public class Settings {

    // scene settings
    // -------------------------------
    private DoubleProperty sceneWidth = new SimpleDoubleProperty(1276);
    private DoubleProperty sceneHeight = new SimpleDoubleProperty(1080);
    private ObjectProperty<Color> sceneColor = new SimpleObjectProperty<>( Color.BLACK);
    private DoubleProperty toolbarWidth = new SimpleDoubleProperty(276);
    private DoubleProperty canvasWidth = new SimpleDoubleProperty(sceneWidth.doubleValue()-toolbarWidth.doubleValue());
    private DoubleProperty canvasHeight = new SimpleDoubleProperty(sceneHeight.doubleValue() - 400);
    // forces
    // -------------------------------
    // number of forces
    private IntegerProperty attractorCount = new SimpleIntegerProperty(1);
    private IntegerProperty repellerCount = new SimpleIntegerProperty(1);
    // just some artificial strength value that matches our needs
    private DoubleProperty repellerStrength = new SimpleDoubleProperty( getInitialRepellerStrengthValue());
    private DoubleProperty attractorStrength = new SimpleDoubleProperty( getInitialAttractorStrengthValue());
    // repeller and attractor radius
    private DoubleProperty paddleRadius = new SimpleDoubleProperty(75);
    // just some artificial strength value that matches our needs.
    private ObjectProperty<Vector2D> forceGravity = new SimpleObjectProperty<>(new Vector2D(0,0));
    private DoubleProperty gravityX = new SimpleDoubleProperty( forceGravity.getValue().x);
    private DoubleProperty gravityY = new SimpleDoubleProperty( forceGravity.getValue().y);
    // emitter
    // -------------------------------
    private IntegerProperty emitterFrequency = new SimpleIntegerProperty((int)getInitialEmitterFrequencyValue()); // particles per frame
    private DoubleProperty emitterWidth = new SimpleDoubleProperty(canvasWidth.doubleValue());
    private DoubleProperty emitterLocationY = new SimpleDoubleProperty(getInitialEmitterLocationYValue());
    // particles
    // -------------------------------
    private DoubleProperty particleWidth = new SimpleDoubleProperty(getInitialParticleSizeValue());
    private DoubleProperty particleHeight = new SimpleDoubleProperty(getInitialParticleSizeValue());
    private DoubleProperty particleLifeSpanMax = new SimpleDoubleProperty( 256);
    private DoubleProperty particleMaxSpeed = new SimpleDoubleProperty( getInitialParticleMaxSpeedValue());
    // initial properties
    private final double initialEmitterFrequencyValue = 100;
    private final double initialEmitterLocationYValue = 340;
    private final double initialParticleSizeValue = 1.75;
    private final double initialParticleMaxSpeedValue = 4;
    private final double initialAttractorStrengthValue = 500;
    private final double initialRepellerStrengthValue = 500;
    private final double initialForceGravityXValue = 0;
    private final double initialForceGravityYValue = 0;
    // Node reference variables
    Slider emitterFrequencySlider = new Slider();
    Slider emitterLocationYSlider = new Slider();
    Slider particleSizeSlider = new Slider();
    Slider particleMaxSpeedSlider = new Slider();
    Slider attractorStrengthSlider = new Slider();
    Slider repellerStrengthSlider = new Slider();
    Slider forceGravityXSlider = new Slider();
    Slider forceGravityYSlider = new Slider();
    // test execution
    Button experimentButton1 = new Button("    Experiment 1    ");
    Label progressLabel1 = new Label("Progress:   ");
    Label percentageLabel1 = new Label("0.0%");
    Button experimentButton2 = new Button("    Experiment 2    ");
    Label progressLabel2 = new Label("Progress:   ");
    Label percentageLabel2 = new Label("0.0%");
    Button experimentButton3 = new Button("    Experiment 3    ");
    Label progressLabel3 = new Label("Progress:   ");
    Label percentageLabel3 = new Label("0.0%");
    // reference vars
    Button stencilButton1 = new Button("Show");
    Button stencilButton2 = new Button("Show");
    Button stencilButton3 = new Button("Show");
    // instance handling
    // ----------------------------------------
    private static Settings settings = new Settings();
    private Settings() { }
    //Return the one instance of this class
    public static Settings get() {
        return settings;
    }
    // User Interface
    // ----------------------------------------
    public Node createToolbar() {

        GridPane gp = new GridPane();

        // gridpane layout
        gp.setPrefWidth( Settings.get().getToolbarWidth());
        gp.setHgap(1);
        gp.setVgap(1);
        gp.setPadding(new Insets(8));

        // set column size in percent
        ColumnConstraints column = new ColumnConstraints();
        column.setPercentWidth(30);
        gp.getColumnConstraints().add(column);
        column = new ColumnConstraints();
        column.setPercentWidth(70);
        gp.getColumnConstraints().add(column);

        // add components for settings to gridpane
        int rowIndex = 0;

        // emitter
        gp.addRow(rowIndex++, createSeparator( "Emitter"));

        emitterFrequencySlider = createNumberSlider( emitterFrequency, 1, 150);
        gp.addRow(rowIndex++, new Label("Frequency"), emitterFrequencySlider);

        /*
        slider = createNumberSlider( emitterWidth, 0, getCanvasWidth());
        gp.addRow(rowIndex++, new Label("Width"), slider);
        */

        emitterLocationYSlider = createNumberSlider( emitterLocationY, 0, getCanvasHeight());
        gp.addRow(rowIndex++, new Label("Location Y"), emitterLocationYSlider);

        // particles
        gp.addRow(rowIndex++, createSeparator( "Particles"));

        particleSizeSlider = createNumberSlider( particleWidth, 1, 60);
        gp.addRow(rowIndex++, new Label("Size"), particleSizeSlider);

        particleMaxSpeedSlider = createNumberSlider( particleMaxSpeed, 0, 10);
        gp.addRow(rowIndex++, new Label("Max Speed"), particleMaxSpeedSlider);

        // attractors
        gp.addRow(rowIndex++, createSeparator( "Attractors"));

        attractorStrengthSlider = createNumberSlider( attractorStrength, 0, 2000);
        gp.addRow(rowIndex++, new Label("Strength"), attractorStrengthSlider);

        // repellers
        gp.addRow(rowIndex++, createSeparator( "Repellers"));

        repellerStrengthSlider = createNumberSlider( repellerStrength, 0, 2000);
        gp.addRow(rowIndex++, new Label("Strength"), repellerStrengthSlider);

        // forces
        gp.addRow(rowIndex++, createSeparator( "Forces"));

        // gravity
        // update gravity vector value when gravity value changes
        gravityX.addListener( (ChangeListener<Number>) (observable, oldValue, newValue) -> {
            forceGravity.getValue().set(newValue.doubleValue(),gravityY.doubleValue());
        });
        gravityY.addListener( (ChangeListener<Number>) (observable, oldValue, newValue) -> {
            forceGravity.getValue().set(gravityX.doubleValue(), newValue.doubleValue());
        });
        forceGravityXSlider = createNumberSlider( gravityX, -0.5, 0.5);
        gp.addRow(rowIndex++, new Label("Gravity X"), forceGravityXSlider);
        forceGravityYSlider = createNumberSlider( gravityY, -0.5, 0.5);
        gp.addRow(rowIndex++, new Label("Gravity Y"), forceGravityYSlider);
        // run test button
        gp.addRow(rowIndex++, createSeparator( "Run Test"));
        gp.addRow(rowIndex++, createExecutePanel(experimentButton1, progressLabel1, percentageLabel1));
        gp.addRow(rowIndex++, createExecutePanel(experimentButton2, progressLabel2, percentageLabel2));
        gp.addRow(rowIndex++, createExecutePanel(experimentButton3, progressLabel3, percentageLabel3));
        // stencil
        gp.addRow(rowIndex++, createSeparator("Stencils"));
        gp.addRow(rowIndex++, createStencilButtons());

        return gp;
    }

    private Node createExecutePanel(Button button, Label progressLabel, Label percentageLabel) {

        HBox box = new HBox();
        HBox progressBox = new HBox();
        progressBox.getChildren().addAll(progressLabel, percentageLabel);
        progressBox.setPadding(new Insets(3));
        box.getChildren().addAll(button, progressBox);
        box.spacingProperty().setValue(20);
        box.setPadding(new Insets(7));
        // Sets width of control board from the right edge of the window.
        GridPane.setColumnSpan(box, 2);
        GridPane.setFillWidth(box, true);
        GridPane.setHgrow(box, Priority.ALWAYS);

        return box;
    }

    private Node createStencilButtons() {

        HBox box = new HBox();
        final int stencilButtonWidth = 70;
        final String bottomPadding = "-fx-padding: 0 0 5 0;";

        VBox experimentBox1 = new VBox();
        Label label1 = new Label("    Exp # 1");
        stencilButton1.setPrefWidth(stencilButtonWidth);
        experimentBox1.getChildren().addAll(label1, stencilButton1);
        label1.setStyle(bottomPadding);

        VBox experimentBox2 = new VBox();
        Label label2 = new Label("    Exp # 2");
        stencilButton2.setPrefWidth(stencilButtonWidth);
        experimentBox2.getChildren().addAll(label2, stencilButton2);
        label2.setStyle(bottomPadding);

        VBox experimentBox3 = new VBox();
        Label label3 = new Label("    Exp # 3");
        stencilButton3.setPrefWidth(stencilButtonWidth);
        experimentBox3.getChildren().addAll(label3, stencilButton3);
        label3.setStyle(bottomPadding);

        box.getChildren().addAll(experimentBox1, experimentBox2, experimentBox3);
        box.setPadding(new Insets(6));
        box.spacingProperty().setValue(20);
        GridPane.setColumnSpan(box, 2);
        GridPane.setFillWidth(box, true);
        GridPane.setHgrow(box, Priority.ALWAYS);

        return box;
    }

    private Node createSeparator( String text) {

        VBox box = new VBox();

        Label label = new Label( text);
        label.setFont(Font.font(null, FontWeight.BOLD, 14));

        Separator separator = new Separator();

        box.getChildren().addAll(separator, label);
        box.setFillWidth(true);

        // Sets width of control board from the right edge of the window.
        GridPane.setColumnSpan(box, 2);
        GridPane.setFillWidth(box, true);
        GridPane.setHgrow(box, Priority.ALWAYS);

        return box;
    }

    private Slider createNumberSlider( Property<Number> observable, double min, double max) {

        Slider slider = new Slider( min, max, observable.getValue().doubleValue());
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.valueProperty().bindBidirectional(observable);

        return slider;
    }

    // getters to reset default values from main
    public double getInitialEmitterFrequencyValue() {
        return initialEmitterFrequencyValue;
    }
    public double getInitialEmitterLocationYValue() {
        return initialEmitterLocationYValue;
    }
    public double getInitialParticleSizeValue() {
        return initialParticleSizeValue;
    }
    public double getInitialParticleMaxSpeedValue() {
        return initialParticleMaxSpeedValue;
    }
    public double getInitialAttractorStrengthValue() {
        return initialAttractorStrengthValue;
    }
    public double getInitialRepellerStrengthValue() {
        return initialRepellerStrengthValue;
    }
    public double getInitialForceGravityXValue() {
        return initialForceGravityXValue;
    }
    public double getInitialForceGravityYValue() {
        return initialForceGravityYValue;
    }
    // -------------------------------
    // auto-generated begin
    // -------------------------------
    public final DoubleProperty sceneWidthProperty() {
        return this.sceneWidth;
    }
    public final double getSceneWidth() {
        return this.sceneWidthProperty().get();
    }
    public final void setSceneWidth(final double sceneWidth) {
        this.sceneWidthProperty().set(sceneWidth);
    }
    public final DoubleProperty sceneHeightProperty() {
        return this.sceneHeight;
    }
    public final double getSceneHeight() {
        return this.sceneHeightProperty().get();
    }
    public final void setSceneHeight(final double sceneHeight) {
        this.sceneHeightProperty().set(sceneHeight);
    }
    public final ObjectProperty<Color> sceneColorProperty() {
        return this.sceneColor;
    }
    public final javafx.scene.paint.Color getSceneColor() {
        return this.sceneColorProperty().get();
    }
    public final void setSceneColor(final javafx.scene.paint.Color sceneColor) {
        this.sceneColorProperty().set(sceneColor);
    }
    public final IntegerProperty attractorCountProperty() {
        return this.attractorCount;
    }
    public final int getAttractorCount() {
        return this.attractorCountProperty().get();
    }
    public final void setAttractorCount(final int attractorCount) {
        this.attractorCountProperty().set(attractorCount);
    }
    public final IntegerProperty repellerCountProperty() {
        return this.repellerCount;
    }
    public final int getRepellerCount() {
        return this.repellerCountProperty().get();
    }
    public final void setRepellerCount(final int repellerCount) {
        this.repellerCountProperty().set(repellerCount);
    }
    public final DoubleProperty repellerStrengthProperty() {
        return this.repellerStrength;
    }
    public final double getRepellerStrength() {
        return this.repellerStrengthProperty().get();
    }
    public final void setRepellerStrength(final double repellerStrength) {
        this.repellerStrengthProperty().set(repellerStrength);
    }
    public final double getPaddleRadius() { return this.paddleRadius.doubleValue(); }
    public final ObjectProperty<Vector2D> forceGravityProperty() {
        return this.forceGravity;
    }
    public final Vector2D getForceGravity() {
        return this.forceGravityProperty().get();
    }
    public final void setForceGravity(final Vector2D forceGravity) {
        this.forceGravityProperty().set(forceGravity);
    }
    public final IntegerProperty emitterFrequencyProperty() {
        return this.emitterFrequency;
    }
    public final int getEmitterFrequency() {
        return this.emitterFrequencyProperty().get();
    }
    public final void setEmitterFrequency(final int emitterFrequency) {
        this.emitterFrequencyProperty().set(emitterFrequency);
    }
    public final DoubleProperty emitterWidthProperty() {
        return this.emitterWidth;
    }
    public final double getEmitterWidth() {
        return this.emitterWidthProperty().get();
    }
    public final void setEmitterWidth(final double emitterWidth) {
        this.emitterWidthProperty().set(emitterWidth);
    }
    public final DoubleProperty emitterLocationYProperty() {
        return this.emitterLocationY;
    }
    public final double getEmitterLocationY() {
        return this.emitterLocationYProperty().get();
    }
    public final void setEmitterLocationY(final double emitterLocationY) {
        this.emitterLocationYProperty().set(emitterLocationY);
    }
    public final DoubleProperty particleWidthProperty() {
        return this.particleWidth;
    }
    public final double getParticleWidth() {
        return this.particleWidthProperty().get();
    }
    public final void setParticleWidth(final double particleWidth) {
        this.particleWidthProperty().set(particleWidth);
    }
    public final DoubleProperty particleHeightProperty() {
        return this.particleHeight;
    }
    public final double getParticleHeight() {
        return this.particleHeightProperty().get();
    }
    public final void setParticleHeight(final double particleHeight) {
        this.particleHeightProperty().set(particleHeight);
    }
    public final DoubleProperty particleLifeSpanMaxProperty() {
        return this.particleLifeSpanMax;
    }
    public final double getParticleLifeSpanMax() {
        return this.particleLifeSpanMaxProperty().get();
    }
    public final void setParticleLifeSpanMax(final double particleLifeSpanMax) {
        this.particleLifeSpanMaxProperty().set(particleLifeSpanMax);
    }
    public final DoubleProperty particleMaxSpeedProperty() {
        return this.particleMaxSpeed;
    }
    public final double getParticleMaxSpeed() {
        return this.particleMaxSpeedProperty().get();
    }
    public final void setParticleMaxSpeed(final double particleMaxSpeed) {
        this.particleMaxSpeedProperty().set(particleMaxSpeed);
    }
    public final DoubleProperty toolbarWidthProperty() {
        return this.toolbarWidth;
    }
    public final double getToolbarWidth() {
        return this.toolbarWidthProperty().get();
    }
    public final void setToolbarWidth(final double toolbarWidth) {
        this.toolbarWidthProperty().set(toolbarWidth);
    }
    public final DoubleProperty canvasWidthProperty() {
        return this.canvasWidth;
    }
    public final double getCanvasWidth() {
        return this.canvasWidthProperty().get();
    }
    public final void setCanvasWidth(final double canvasWidth) {
        this.canvasWidthProperty().set(canvasWidth);
    }
    public final DoubleProperty canvasHeightProperty() {
        return this.canvasHeight;
    }
    public final double getCanvasHeight() {
        return this.canvasHeightProperty().get();
    }
    public final void setCanvasHeight(final double canvasHeight) {
        this.canvasHeightProperty().set(canvasHeight);
    }
    public final DoubleProperty attractorStrengthProperty() {
        return this.attractorStrength;
    }
    public final double getAttractorStrength() {
        return this.attractorStrengthProperty().get();
    }
    public final void setAttractorStrength(final double attractorStrength) {
        this.attractorStrengthProperty().set(attractorStrength);
    }
    // -------------------------------
    // auto-generated end
    // -------------------------------
}
