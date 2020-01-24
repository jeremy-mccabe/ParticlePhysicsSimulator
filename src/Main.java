
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {

    private static Random random = new Random();

    Canvas canvas;
    GraphicsContext graphicsContext;
    Pane layerPane;

    List<Attractor> allAttractors = new ArrayList<>();
    List<Repeller> allRepellers = new ArrayList<>();
    List<Particle> allParticles = new ArrayList<>();

    AnimationTimer animationLoop;

    Scene scene;

    MouseGestures mouseGestures = new MouseGestures();

    private Image[] images;

    private double primaryStageWidth;
    private double primaryStageHeight;
    private double canvasWidth;
    private double canvasHeight;
    private double sceneWidth;
    private double sceneHeight;

    // number of data points gathered before processing. 1 sample = 1 list element added.
    // Adjusts dataset size, will affect CPU performance when increased.
    final int sampleSize = 50; // DEFAULT: 50
    // frame size for initialization
    final int initializationWindow = 250; // DEFAULT: 250
    // desired frame sampling. Tracked by numberOfFramesCaptured.
    // Adjusts the "smoothness" of the line.
    final int frameSamplingWindow = 20; // DEFAULT: 20
    // tracks frames captured.
    // e.g., every N frames captured => 1 element added to each list
    int numberOfFramesCaptured = 0;
    // processing lists:
    private List<Double> collisionsList = new ArrayList<>();
    private List<Double> avgVelocitiesList = new ArrayList<>();
    private List<Double> avgAccelerationsList = new ArrayList<>();
    // object used for data aggregation:
    private DataGroup dataGroup = new DataGroup();
    // object used to run statistics on the datasets, and populate the visualizations
    private TestingUnit testingUnit = new TestingUnit();
    private BorderPane root = new BorderPane();
    // mutex booleans used for experiments
    private boolean firstExperimentInProgress = false;
    private boolean secondExperimentInProgress = false;
    private boolean thirdExperimentInProgress = false;
    private boolean panelInitialized = false;
    // formatting object
    private DecimalFormat df = new DecimalFormat("###.##");
    // css slider track colors
    private String defaultTrackColor = "-fx-control-inner-background: white";
    private String initializationTrackColor = "-fx-control-inner-background: cyan;";
    private String inProgressTrackColor = "-fx-control-inner-background: rgb(204, 255, 21);";

    @Override
    public void start(Stage primaryStage) {

        BorderPane borderPane = new BorderPane();

        canvas = new Canvas(Settings.get().getCanvasWidth(), Settings.get().getCanvasHeight());
        canvasWidth = Settings.get().getCanvasWidth();
        canvasHeight = Settings.get().getCanvasHeight();

        graphicsContext = canvas.getGraphicsContext2D();

        layerPane = new Pane();
        layerPane.getChildren().addAll(canvas);

        canvas.widthProperty().bind(layerPane.widthProperty());
        borderPane.setCenter(layerPane);

        Node toolbar = Settings.get().createToolbar();
        borderPane.setRight(toolbar);

        root.setTop(borderPane);
        root.setRight(testingUnit.generateOutputConsole(null, null, null));

        scene = new Scene(root, Settings.get().getSceneWidth(), Settings.get().getSceneHeight(), Settings.get().getSceneColor());
        sceneWidth = Settings.get().getSceneWidth();
        sceneHeight = Settings.get().getSceneHeight();

        primaryStage.setScene(scene);
        primaryStage.setTitle("Particle Physics Simulator");
        primaryStage.show();
        primaryStageWidth = primaryStage.getWidth();
        primaryStageHeight = primaryStage.getHeight();
        primaryStage.setResizable(false);
        primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> {
            primaryStageWidth = primaryStage.getWidth();

        });
        primaryStage.heightProperty().addListener((obs, oldVal, newVal) -> {
            primaryStageHeight = primaryStage.getHeight();
        });

        // initialize content:
        preCreateImages();
        // add content:
        preparePaddlesAndStencils();
        // listeners for settings:
        addSettingsListeners();
        // add mouse position listener:
        addInputListeners();
        // add context menus:
        addContextMenu(canvas);
        // run animation loop:
        startAnimation();
    }

    public static void main(String[] args) { launch(args); }

    private void preCreateImages() {
        this.images = Utils.preCreateImages();
    }

    private void preparePaddlesAndStencils() {
        // add stencils
        createStencilMarkers();
        configureStencilMarkers();
        // add attractors
        for (int i = 0; i < Settings.get().getAttractorCount(); i++) {
            addAttractor();
        }
        // add repellers
        for (int i = 0; i < Settings.get().getRepellerCount(); i++) {
            addRepeller();
        }
    }

    private void startAnimation() {
        // start loop
        animationLoop = new AnimationTimer() {

            FpsCounter fpsCounter = new FpsCounter();
            int initializationCounter = 0;
            boolean experiementExecutionTriggered = false;

            // called every frame:
            @Override
            public void handle(long now) {
                // update fps
                fpsCounter.update(now);
                // add new particles
                for (int i = 0; i < Settings.get().getEmitterFrequency(); i++) {
                    addParticle();
                }
                // apply force: gravity
                Vector2D forceGravity = Settings.get().getForceGravity();
                allParticles.forEach(sprite -> {
                    sprite.applyForce(forceGravity);
                });
                // apply force: attractor
                for (Attractor attractor : allAttractors) {
                    allParticles.stream().parallel().forEach(sprite -> {
                        Vector2D force = attractor.getForce(sprite);
                        sprite.applyForce(force);
                    });
                }
                // apply force: repeller
                for (Repeller repeller : allRepellers) {
                    allParticles.stream().parallel().forEach(sprite -> {
                        Vector2D force = repeller.getForce(sprite);
                        sprite.applyForce(force);
                    });
                }
                /////////////////////////////////////////
                // INTEGRATION FOR EXPERIMENTS
                ///////////////////////////////////////
                // Track all data if experiment is in progress and testing is ready
                if ((firstExperimentInProgress || secondExperimentInProgress || thirdExperimentInProgress) && panelInitialized) {
                    // Data collection and logging:
                    retrieveData(detectCollisions(), reportAverageVelocity(), reportAverageAcceleration());
                    numberOfFramesCaptured++;
                }
                // CONTROL EXPERIMENTS:
                // Experiment 1
                if (firstExperimentInProgress) {
                    if (!panelInitialized) {
                        Settings.get().progressLabel1.textProperty().setValue("    Initializing ....");
                        Settings.get().percentageLabel1.textProperty().setValue("");
                        initializePanelSettings();
                        panelInitialized = true;
                    }
                    // Initialization check:
                    if (initializationCounter == initializationWindow) {
                        Settings.get().progressLabel1.textProperty().setValue("Progress:   ");
                        // set color on track and zero out lists
                        if (!experiementExecutionTriggered) { executeFirstExperiment(); experiementExecutionTriggered = true; }
                        // update progress label
                        updateProgressLabel();
                        // perform automated parameter controls
                        operatePanelSettings();
                        // Test Completed:
                        if (collisionsList.size() == sampleSize) {
                            // retrieve and set new Node for the output console
                            root.setRight(testingUnit.generateOutputConsole(collisionsList,
                                    avgVelocitiesList, avgAccelerationsList));
                            firstExperimentInProgress = false;
                            Settings.get().repellerStrengthSlider.setStyle(defaultTrackColor);
                            logDataToConsole();
                            resetPanelSettings();
                            resetArrayLists();
                            initializationCounter = 0;
                            panelInitialized = false;
                            experiementExecutionTriggered = false;
                        }
                    } else { initializationCounter++; }
                }
                // Experiment 2
                else if (secondExperimentInProgress) {
                    if (!panelInitialized) {
                        Settings.get().progressLabel2.textProperty().setValue("    Initializing ....");
                        Settings.get().percentageLabel2.textProperty().setValue("");
                        initializePanelSettings();
                        panelInitialized = true;
                    }
                    // Initialization check:
                    if (initializationCounter == initializationWindow) {
                        Settings.get().progressLabel2.textProperty().setValue("Progress:   ");
                        // set color on track and zero out lists
                        if (!experiementExecutionTriggered) { executeSecondExperiment(); experiementExecutionTriggered = true; }
                        // update progress label
                        updateProgressLabel();
                        // perform automated parameter controls
                        operatePanelSettings();
                        // Test Completed:
                        if (collisionsList.size() == sampleSize) {
                            root.setRight(testingUnit.generateOutputConsole(collisionsList,
                                    avgVelocitiesList, avgAccelerationsList));
                            secondExperimentInProgress = false;
                            Settings.get().forceGravityXSlider.setStyle(defaultTrackColor);
                            logDataToConsole();
                            resetPanelSettings();
                            resetArrayLists();
                            initializationCounter = 0;
                            panelInitialized = false;
                            experiementExecutionTriggered = false;
                        }
                    } else { initializationCounter++; };
                }
                // Experiment 3
                else if (thirdExperimentInProgress) {
                    if (!panelInitialized) {
                        Settings.get().progressLabel3.textProperty().setValue("    Initializing ....");
                        Settings.get().percentageLabel3.textProperty().setValue("");
                        initializePanelSettings();
                        panelInitialized = true;
                    }
                    // Initialization check:
                    if (initializationCounter == initializationWindow) {
                        Settings.get().progressLabel3.textProperty().setValue("Progress:   ");
                        // set color on track and zero out lists
                        if (!experiementExecutionTriggered) { executeThirdExperiment(); experiementExecutionTriggered = true; }
                        // update progress label
                        updateProgressLabel();
                        // perform automated parameter controls
                        operatePanelSettings();
                        // Test Completed:
                        if (collisionsList.size() == sampleSize) {
                            root.setRight(testingUnit.generateOutputConsole(collisionsList,
                                    avgVelocitiesList, avgAccelerationsList));
                            thirdExperimentInProgress = false;
                            Settings.get().particleMaxSpeedSlider.setStyle(defaultTrackColor);
                            logDataToConsole();
                            resetPanelSettings();
                            resetArrayLists();
                            initializationCounter = 0;
                            panelInitialized = false;
                            experiementExecutionTriggered = false;
                        }
                    } else { initializationCounter++; }
                }
                ////////////////////////////////////////////////////
                // END INTEGRATION FOR EXPERIMENTS
                ////////////////////////////////////////////////////

                // move sprite: apply acceleration, calculate velocity and position
                allParticles.stream().parallel().forEach(Sprite::move);
                // update in fx scene
                allAttractors.forEach(Sprite::display);
                allRepellers.forEach(Sprite::display);
                // draw all particles on canvas
                // -----------------------------------------
                // graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                graphicsContext.setFill(Color.BLACK);
                graphicsContext.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
                // TODOforLater: parallel?
                double particleSizeHalf = Settings.get().getParticleWidth() / 2;
                allParticles.stream().forEach(particle -> {
                    Image img = images[particle.getLifeSpan()];
                    graphicsContext.drawImage(img, particle.getPosition().x - particleSizeHalf, particle.getPosition().y - particleSizeHalf);

                });
                // life span of particle
                allParticles.stream().parallel().forEach(Sprite::decreaseLifeSpan);
                // remove all particles that aren't visible anymore
                removeDeadParticles();
                // show number of particles
                graphicsContext.setFill(Color.WHITE);
                graphicsContext.fillText("Particles: " + allParticles.size()
                                + "   FPS: " + fpsCounter.getFrameRate()
                                + "\nStage:\t" + (int) primaryStageWidth + " w\t" + (int) primaryStageHeight + " h"
                                + "\nCanvas:\t" + (int) canvasWidth + " w\t" + (int) canvasHeight + " h"
                                + "\nScene:\t" + (int) sceneWidth + " w\t" + (int) sceneHeight + " h"
                        , 1, 10
                );
            } // End @handle method.
        };
        // start loop
        animationLoop.start();
    }

    private void initializePanelSettings() {
        if (firstExperimentInProgress) {
            // adjusts: REPELLER STRENGTH: START=0, END=2000
            // controls: attractor strength (MAX=2000)
            double value = 0;
            double attractorStrengthControl = 2000;
            // SLIDERS:
            Settings.get().repellerStrengthSlider.setValue(value);
            Settings.get().repellerStrengthSlider.setStyle(initializationTrackColor);
            Settings.get().attractorStrengthSlider.setValue(attractorStrengthControl);
            // lock other sliders
            Settings.get().emitterFrequencySlider.setValue(Settings.get().getInitialEmitterFrequencyValue());
            Settings.get().emitterLocationYSlider.setValue(Settings.get().getInitialEmitterLocationYValue());
            Settings.get().particleSizeSlider.setValue(Settings.get().getInitialParticleSizeValue());
            Settings.get().particleMaxSpeedSlider.setValue(Settings.get().getInitialParticleMaxSpeedValue());
            Settings.get().forceGravityXSlider.setValue(Settings.get().getInitialForceGravityXValue());
            Settings.get().forceGravityYSlider.setValue(Settings.get().getInitialForceGravityYValue());
        } else if (secondExperimentInProgress) {
            // adjusts GRAVITY X: START=-0.5, MID=0.5, END=-0.5
            // controls: emitter frequency (MAX=150), particle max speed (MAX=10)
            double result = -0.5;
            // SLIDERS:
            Settings.get().forceGravityXSlider.setValue(result);
            Settings.get().forceGravityXSlider.setStyle(initializationTrackColor);
            Settings.get().emitterFrequencySlider.setValue(150);
            Settings.get().particleMaxSpeedSlider.setValue(10);
            // lock other sliders
            Settings.get().emitterLocationYSlider.setValue(Settings.get().getInitialEmitterLocationYValue());
            Settings.get().particleSizeSlider.setValue(Settings.get().getInitialParticleSizeValue());
            Settings.get().attractorStrengthSlider.setValue(Settings.get().getInitialAttractorStrengthValue());
            Settings.get().repellerStrengthSlider.setValue(Settings.get().getInitialRepellerStrengthValue());
            Settings.get().forceGravityYSlider.setValue(Settings.get().getInitialForceGravityYValue());
        } else if (thirdExperimentInProgress) {
            // adjusts MAX PARTICLE SPEED: START=0, END=10
            // controls: emitter frequency (VALUE=75), emitter location y (MAX=680), gravity y (MIN=-0.5), paddle strength (MAX=2000)
            double value = 10;
            // SLIDERS:
            Settings.get().particleMaxSpeedSlider.setValue(value);
            Settings.get().particleMaxSpeedSlider.setStyle(initializationTrackColor);
            Settings.get().emitterFrequencySlider.setValue(150);
            Settings.get().emitterLocationYSlider.setValue(680);
            Settings.get().forceGravityYSlider.setValue(-0.5);
            Settings.get().attractorStrengthSlider.setValue(2000);
            Settings.get().repellerStrengthSlider.setValue(2000);
            // lock other sliders
            Settings.get().particleSizeSlider.setValue(Settings.get().getInitialParticleSizeValue());
            Settings.get().forceGravityXSlider.setValue(Settings.get().getInitialForceGravityXValue());
        }
    }

    private void operatePanelSettings() {
        // adjust settings specific to experiment in progress
        double progressComplete = (double) collisionsList.size() / (double) sampleSize;
        if (firstExperimentInProgress) {
            // adjusts: REPELLER STRENGTH: START=0, END=2000
            // controls: attractor strength (MAX=2000)
            double endValue = 2000;
            double result = (endValue * progressComplete);
            double attractorStrengthControl = 2000;
            // SLIDERS:
            Settings.get().repellerStrengthSlider.setValue(result);
            Settings.get().attractorStrengthSlider.setValue(attractorStrengthControl);
            // lock other sliders
            Settings.get().emitterFrequencySlider.setValue(Settings.get().getInitialEmitterFrequencyValue());
            Settings.get().emitterLocationYSlider.setValue(Settings.get().getInitialEmitterLocationYValue());
            Settings.get().particleSizeSlider.setValue(Settings.get().getInitialParticleSizeValue());
            Settings.get().particleMaxSpeedSlider.setValue(Settings.get().getInitialParticleMaxSpeedValue());
            Settings.get().forceGravityXSlider.setValue(Settings.get().getInitialForceGravityXValue());
            Settings.get().forceGravityYSlider.setValue(Settings.get().getInitialForceGravityYValue());
        } else if (secondExperimentInProgress) {
            // adjusts GRAVITY X: START=-0.5, MID=0.5, END=-0.5
            // controls: emitter frequency (MAX=150), particle max speed (MAX=10)
            double result;
            // logic for gravity x slider value
            if (progressComplete <= 0.2) {
                //result = ((double) collisionsList.size() / (double) sampleSize);
                result = -0.5;
            } else if (progressComplete <= 0.4) {
                result = 0.5;
            } else if (progressComplete <= 0.6) {
                result = -0.5;
            } else if (progressComplete <= 0.8) {
                result = 0.5;
            } else {
                result = -0.5;
            }
            // SLIDERS:
            Settings.get().forceGravityXSlider.setValue(result);
            Settings.get().emitterFrequencySlider.setValue(150);
            Settings.get().particleMaxSpeedSlider.setValue(10);
            // lock other sliders
            Settings.get().emitterLocationYSlider.setValue(Settings.get().getInitialEmitterLocationYValue());
            Settings.get().particleSizeSlider.setValue(Settings.get().getInitialParticleSizeValue());
            Settings.get().attractorStrengthSlider.setValue(Settings.get().getInitialAttractorStrengthValue());
            Settings.get().repellerStrengthSlider.setValue(Settings.get().getInitialRepellerStrengthValue());
            Settings.get().forceGravityYSlider.setValue(Settings.get().getInitialForceGravityYValue());
        } else if (thirdExperimentInProgress) {
            // adjusts MAX PARTICLE SPEED: START=0, END=10
            // controls: emitter frequency (VALUE=75), emitter location y (MAX=680), gravity y (MIN=-0.5), paddle strength (MAX=2000)
            double endValue = 10;
            double result = endValue - (endValue * (double) collisionsList.size() / (double) sampleSize);
            // SLIDERS:
            Settings.get().particleMaxSpeedSlider.setValue(result);
            Settings.get().emitterFrequencySlider.setValue(150);
            Settings.get().emitterLocationYSlider.setValue(680);
            Settings.get().forceGravityYSlider.setValue(-0.5);
            Settings.get().attractorStrengthSlider.setValue(2000);
            Settings.get().repellerStrengthSlider.setValue(2000);
            // lock other sliders
            Settings.get().particleSizeSlider.setValue(Settings.get().getInitialParticleSizeValue());
            Settings.get().forceGravityXSlider.setValue(Settings.get().getInitialForceGravityXValue());
        }
    }

    private void resetPanelSettings() {
        // reset default panel settings
        Settings.get().emitterFrequencySlider.setValue(Settings.get().getInitialEmitterFrequencyValue());
        Settings.get().emitterLocationYSlider.setValue(Settings.get().getInitialEmitterLocationYValue());
        Settings.get().particleSizeSlider.setValue(Settings.get().getInitialParticleSizeValue());
        Settings.get().particleMaxSpeedSlider.setValue(Settings.get().getInitialParticleMaxSpeedValue());
        Settings.get().attractorStrengthSlider.setValue(Settings.get().getInitialAttractorStrengthValue());
        Settings.get().repellerStrengthSlider.setValue(Settings.get().getInitialRepellerStrengthValue());
        Settings.get().forceGravityXSlider.setValue(Settings.get().getInitialForceGravityXValue());
        Settings.get().forceGravityYSlider.setValue(Settings.get().getInitialForceGravityYValue());
    }

    private void initializeFirstExperiment() {
        if (!secondExperimentInProgress && !thirdExperimentInProgress) {
            // switch experiment on
            firstExperimentInProgress = true;
        }
    }

    private void initializeSecondExperiment() {
        if (!firstExperimentInProgress && !thirdExperimentInProgress) {
            // switch experiment on
            secondExperimentInProgress = true;
        }
    }

    private void initializeThirdExperiment() {
        if (!firstExperimentInProgress && !secondExperimentInProgress) {
            // switch experiment on
            thirdExperimentInProgress = true;
        }
    }

    private void executeFirstExperiment() {
        // zero out the lists
        resetArrayLists();
        // change slider track color
        Settings.get().repellerStrengthSlider.setStyle(inProgressTrackColor);
    }

    private void executeSecondExperiment() {
        // zero out the lists
        resetArrayLists();
        // change slider track color
        Settings.get().forceGravityXSlider.setStyle(inProgressTrackColor);
    }

    private void executeThirdExperiment() {
        // zero out the lists
        resetArrayLists();
        // change slider track color
        Settings.get().particleMaxSpeedSlider.setStyle(inProgressTrackColor);
    }

    private void updateProgressLabel() {
        // first if statement checks which experiment is in progress, if-else checks if 0 or not and formats appropriately.
        if (firstExperimentInProgress) {
            if (!df.format((double) collisionsList.size() / (double) sampleSize * 100).equals("0")) {
                Settings.get().percentageLabel1.textProperty().setValue(df.format((double) collisionsList.size() / (double) sampleSize * 100) + "%");
            } else {
                Settings.get().percentageLabel1.textProperty().setValue("0.0%");
            }
        } else if (secondExperimentInProgress) {
            if (!df.format((double) collisionsList.size() / (double) sampleSize * 100).equals("0")) {
                Settings.get().percentageLabel2.textProperty().setValue(df.format((double) collisionsList.size() / (double) sampleSize * 100) + "%");
            } else {
                Settings.get().percentageLabel2.textProperty().setValue("0.0%");
            }
        } else if (thirdExperimentInProgress) {
            if (!df.format((double) collisionsList.size() / (double) sampleSize * 100).equals("0")) {
                Settings.get().percentageLabel3.textProperty().setValue(df.format((double) collisionsList.size() / (double) sampleSize * 100) + "%");
            } else {
                Settings.get().percentageLabel3.textProperty().setValue("0.0%");
            }
        }
    }

    private void addInputListeners() { }

    private void addSettingsListeners() {
        // particle size
        Settings.get().particleWidthProperty().addListener((observable, oldValue, newValue) -> preCreateImages());
        // call to execute experiment method when button clicked
        Settings.get().experimentButton1.setOnAction(event -> initializeFirstExperiment());
        Settings.get().experimentButton2.setOnAction(event -> initializeSecondExperiment());
        Settings.get().experimentButton3.setOnAction(event -> initializeThirdExperiment());
        // button 1
        Settings.get().stencilButton1.setOnAction(event -> {
            // switch stencil button text (ON - OFF)
            if (Settings.get().stencilButton1.textProperty().get().toString().equals("Hide")) {
                Settings.get().stencilButton1.textProperty().setValue("Show");
                // switch stencil visibility
                layerPane.getChildren().get(1).setVisible(false);
                layerPane.getChildren().get(2).setVisible(false);
            } else {
                Settings.get().stencilButton1.textProperty().setValue("Hide");
                // switch stencil visibility
                layerPane.getChildren().get(1).setVisible(true);
                layerPane.getChildren().get(2).setVisible(true);
            }
        });
        // button 2
        Settings.get().stencilButton2.setOnAction(event -> {
            // switch stencil button text (ON - OFF)
            if (Settings.get().stencilButton2.textProperty().get().toString().equals("Hide")) {
                Settings.get().stencilButton2.textProperty().setValue("Show");
                // switch stencil visibility
                layerPane.getChildren().get(3).setVisible(false);
                layerPane.getChildren().get(4).setVisible(false);
                layerPane.getChildren().get(5).setVisible(false);
            } else {
                Settings.get().stencilButton2.textProperty().setValue("Hide");
                // switch stencil visibility
                layerPane.getChildren().get(3).setVisible(true);
                layerPane.getChildren().get(4).setVisible(true);
                layerPane.getChildren().get(5).setVisible(true);
            }
        });
        // button 3
        Settings.get().stencilButton3.setOnAction(event -> {
            // switch stencil button text (ON - OFF)
            if (Settings.get().stencilButton3.textProperty().get().toString().equals("Hide")) {
                Settings.get().stencilButton3.textProperty().setValue("Show");
                // switch stencil visibility
                layerPane.getChildren().get(6).setVisible(false);
                layerPane.getChildren().get(7).setVisible(false);
                layerPane.getChildren().get(8).setVisible(false);
                layerPane.getChildren().get(9).setVisible(false);
            } else {
                Settings.get().stencilButton3.textProperty().setValue("Hide");
                // switch stencil visibility
                layerPane.getChildren().get(6).setVisible(true);
                layerPane.getChildren().get(7).setVisible(true);
                layerPane.getChildren().get(8).setVisible(true);
                layerPane.getChildren().get(9).setVisible(true);
            }
        });
    }

    private void removeDeadParticles() {
        Iterator<Particle> iter = allParticles.iterator();
        while (iter.hasNext()) {
            Particle particle = iter.next();
            if (particle.isDead()) {
                // remove from particle list
                iter.remove();
            }
        }
    }

    private void addParticle() {
        // random position
        double x = Settings.get().getCanvasWidth() / 2 + random.nextDouble() * Settings.get().getEmitterWidth() - Settings.get().getEmitterWidth() / 2;
        double y = Settings.get().getEmitterLocationY();
        // dimensions
        double width = Settings.get().getParticleWidth();
        double height = Settings.get().getParticleHeight();
        // create motion data
        Vector2D location = new Vector2D(x, y);
        double vx = random.nextGaussian() * 0.3;
        double vy = random.nextGaussian() * 0.3 - 1.0;
        Vector2D velocity = new Vector2D(vx, vy);
        Vector2D acceleration = new Vector2D(0, 0);
        // create sprite and add to layer
        Particle sprite = new Particle(location, velocity, acceleration, width, height);
        // register sprite
        allParticles.add(sprite);
    }

    private void addAttractor() {
        // center node
        double x = Settings.get().getCanvasWidth() / 2;
        double y = Settings.get().getCanvasHeight() - Settings.get().getCanvasHeight() / 4;
        // dimensions
        double width = Settings.get().getPaddleRadius();
        double height = Settings.get().getPaddleRadius();
        // create motion data
        Vector2D location = new Vector2D(x, y);
        Vector2D velocity = new Vector2D(0, 0);
        Vector2D acceleration = new Vector2D(0, 0);
        // create sprite and add to layer
        Attractor attractor = new Attractor(location, velocity, acceleration, width, height);
        // register sprite
        allAttractors.add(attractor);
        layerPane.getChildren().add(attractor);
        // allow moving via mouse
        mouseGestures.makeDraggable(attractor);
    }

    private void addRepeller() {
        // center node
        double x = Settings.get().getCanvasWidth() / 2;
        double y = Settings.get().getCanvasHeight() - Settings.get().getCanvasHeight() / 4 + 110;
        // dimensions
        double width = Settings.get().getPaddleRadius();
        double height = Settings.get().getPaddleRadius();
        // create motion data
        Vector2D location = new Vector2D(x, y);
        Vector2D velocity = new Vector2D(0, 0);
        Vector2D acceleration = new Vector2D(0, 0);
        // create sprite and add to layer
        Repeller repeller = new Repeller(location, velocity, acceleration, width, height);
        // register sprite
        allRepellers.add(repeller);
        layerPane.getChildren().add(repeller);
        // allow moving via mouse
        mouseGestures.makeDraggable(repeller);
    }

    private void removeRepeller () {
        if (!allRepellers.isEmpty()) {
            allRepellers.remove(allRepellers.size() - 1);
            // remove last element from the layer pane; will always be a repeller
            layerPane.getChildren().remove(layerPane.getChildren().size() - 1);
        }
    }

    private void createStencilMarkers() {
        StencilMarker marker;
        // stencil attributes
        double unassigned = 0.0;
        Color repellerStencilColor = Color.YELLOW;
        Color attractorStencilColor = Color.RED;
        // first experiment group
        marker = new StencilMarker(new Vector2D(unassigned,unassigned), repellerStencilColor);
        layerPane.getChildren().add(marker.createView());
        marker = new StencilMarker(new Vector2D(unassigned,unassigned), attractorStencilColor);
        layerPane.getChildren().add(marker.createView());
        // second experiment group
        marker = new StencilMarker(new Vector2D(unassigned,unassigned), repellerStencilColor);
        layerPane.getChildren().add(marker.createView());
        marker = new StencilMarker(new Vector2D(unassigned,unassigned), repellerStencilColor);
        layerPane.getChildren().add(marker.createView());
        marker = new StencilMarker(new Vector2D(unassigned,unassigned), attractorStencilColor);
        layerPane.getChildren().add(marker.createView());
        // third experiment group
        marker = new StencilMarker(new Vector2D(unassigned,unassigned), repellerStencilColor);
        layerPane.getChildren().add(marker.createView());
        marker = new StencilMarker(new Vector2D(unassigned,unassigned), repellerStencilColor);
        layerPane.getChildren().add(marker.createView());
        marker = new StencilMarker(new Vector2D(unassigned,unassigned), repellerStencilColor);
        layerPane.getChildren().add(marker.createView());
        marker = new StencilMarker(new Vector2D(unassigned,unassigned), attractorStencilColor);
        layerPane.getChildren().add(marker.createView());
    }

    private void configureStencilMarkers() {
        // CANVAS WIDTH (X) = 1000 px
        // CANVAS HEIGHT (Y) = 680 px
        // first experiment group
        double radius = Settings.get().getPaddleRadius() / 2 * 1.25;
        layerPane.getChildren().get(1).relocate(500 - radius, 340 - radius);
        layerPane.getChildren().get(2).relocate(500 - radius, 510 - radius);
        layerPane.getChildren().get(1).setVisible(false);
        layerPane.getChildren().get(2).setVisible(false);
        // second experiment group
        layerPane.getChildren().get(3).relocate(250 - radius, 340 - radius);
        layerPane.getChildren().get(4).relocate(750 - radius, 340 - radius);
        layerPane.getChildren().get(5).relocate(500 - radius, 225 - radius);
        layerPane.getChildren().get(3).setVisible(false);
        layerPane.getChildren().get(4).setVisible(false);
        layerPane.getChildren().get(5).setVisible(false);
        // third experiment group
        layerPane.getChildren().get(6).relocate(333 - radius, 340 - radius);
        layerPane.getChildren().get(7).relocate(666 - radius, 340 - radius);
        layerPane.getChildren().get(8).relocate(500 - radius, 510 - radius);
        layerPane.getChildren().get(9).relocate(500 - radius, 170 - radius);
        layerPane.getChildren().get(6).setVisible(false);
        layerPane.getChildren().get(7).setVisible(false);
        layerPane.getChildren().get(8).setVisible(false);
        layerPane.getChildren().get(9).setVisible(false);
    }

    public void addContextMenu(Node node) {

        MenuItem menuItem;

        ContextMenu contextMenu = new ContextMenu();

        // add attractor
//        menuItem = new MenuItem("Add Attractor");
//        menuItem.setOnAction(e -> addAttractor());
//        contextMenu.getItems().add(menuItem);

        // add repeller
        menuItem = new MenuItem("ADD (Repeller)");
        menuItem.setOnAction(e -> addRepeller());
        contextMenu.getItems().add(menuItem);

        // add repeller
        menuItem = new MenuItem("REMOVE (Repeller)");
        menuItem.setOnAction(e -> removeRepeller());
        contextMenu.getItems().add(menuItem);

        // context menu listener
        node.setOnMousePressed(event -> {
            if (event.isSecondaryButtonDown()) {
                contextMenu.show(node, event.getScreenX(), event.getScreenY());
            }
        });
    }

    // helper class for frame rate calculation
    private static class FpsCounter {

        final long[] frameTimes = new long[100];
        int frameTimeIndex = 0;
        boolean arrayFilled = false;
        double frameRate;

        double decimalsFactor = 1000; // we want 3 decimals

        public void update(long now) {
            long oldFrameTime = frameTimes[frameTimeIndex];
            frameTimes[frameTimeIndex] = now;
            frameTimeIndex = (frameTimeIndex + 1) % frameTimes.length;
            if (frameTimeIndex == 0) {
                arrayFilled = true;
            }
            if (arrayFilled) {
                long elapsedNanos = now - oldFrameTime;
                long elapsedNanosPerFrame = elapsedNanos / frameTimes.length;
                frameRate = 1_000_000_000.0 / elapsedNanosPerFrame;
            }
        }

        public double getFrameRate() {
            return ((int) (frameRate * decimalsFactor)) / decimalsFactor; // reduce to n decimals
        }
    }

    // called from @handle.
    // returns number of collisions between the attractor and all particles during a given frame.
    private int detectCollisions() {
        int collisions = 0;
        for (Particle particle : allParticles) {
            if ((int) particle.position.x == (int) allAttractors.get(0).position.x
                    && (int) particle.position.y == (int) allAttractors.get(0).position.y) {
                collisions++;
            }
        }

        return collisions;
    }

    // these methods iterate through the list of particles on screen
    // and return the average velocity and average acceleration of all particles
    // currently colliding with the attractor in a given frame.
    private double reportAverageVelocity() {
        int collisions = 0;
        double total = 0.0;
        for (Particle particle : allParticles) {
            if ((int) particle.position.x == (int) allAttractors.get(0).position.x
                    && (int) particle.position.y == (int) allAttractors.get(0).position.y) {
                collisions++;
                total += particle.velocity.magnitude();
            }
        }
        // return average vel. during frame of function call
        if (collisions != 0) {
            return total / (double) collisions;
        } else {
            return 0;
        }
    }
    private double reportAverageAcceleration() {
        int collisions = 0;
        double total = 0.0;
        for (Particle particle : allParticles) {
            if ((int) particle.position.x == (int) allAttractors.get(0).position.x
                    && (int) particle.position.y == (int) allAttractors.get(0).position.y) {
                collisions++;
                total += particle.normalizedAcceleration();
            }
        }
        // return average accel. during frame of function call
        if (collisions != 0) {
            return total / (double) collisions;
        } else {
            return 0;
        }
    }

    // called from @handle.
    // aggregates all data and pushes to each list during a given frame.
    private void retrieveData(double collisionsDetectedThisFrame, double averagedVelocityThisFrame, double averagedAccelerationThisFrame) {

        dataGroup.iteration++;

        dataGroup.sumOfCollisions += collisionsDetectedThisFrame;
        dataGroup.sumOfVelocityAverages += averagedVelocityThisFrame;
        dataGroup.sumOfAccelerationAverages += averagedAccelerationThisFrame;

        if (dataGroup.iteration == frameSamplingWindow) {
            // adds relevant data points to the list after the N-th collection
            collisionsList.add(dataGroup.sumOfCollisions);
            avgVelocitiesList.add(dataGroup.averagedVelocityValue());
            avgAccelerationsList.add(dataGroup.averagedAccelerationValue());
            // reset DataSet
            dataGroup.iteration = 0;
            dataGroup.sumOfCollisions = 0;
            dataGroup.sumOfVelocityAverages = 0;
            dataGroup.sumOfAccelerationAverages = 0;
            // reset counter
            numberOfFramesCaptured = 0;
        }
    }

    private void logDataToConsole() {
        System.out.println("\n"
                + "Test Results:" + "\t" + (firstExperimentInProgress ? "Experiment 1" : secondExperimentInProgress ? "Experiment 2" : "Experiment 3" + "\n")
                + "Collisions List:" + "\t" + collisionsList + "\n"
                + "Avg. Velocities List:" + "\t" + avgVelocitiesList + "\n"
                + "Avg. Accelerations List:" + "\t" + avgAccelerationsList + "\n"
                + "Average Collisions During Exp:" + "\t" + testingUnit.statistics.calculateMean(collisionsList) + "\n"
                + "Average Velocity During Exp:" + "\t" + testingUnit.statistics.calculateMean(avgVelocitiesList) + "\n"
                + "Average Acceleration During Exp:" + "\t" + testingUnit.statistics.calculateMean(avgAccelerationsList) + "\n"
        );
    }

    private void resetArrayLists() {
        collisionsList.clear();
        avgVelocitiesList.clear();
        avgAccelerationsList.clear();
    }

    // Helper class for data reporting
    private class DataGroup {
        int iteration = 0;
        double sumOfCollisions = 0;
        double sumOfVelocityAverages = 0.0;
        double sumOfAccelerationAverages = 0.0;
        // returns mean of the averages collected / number of iterations
        double averagedVelocityValue() { return sumOfVelocityAverages / (double) iteration; }
        double averagedAccelerationValue() { return sumOfAccelerationAverages / (double) iteration; }
    }
}
