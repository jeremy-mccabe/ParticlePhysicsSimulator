
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import org.jetbrains.annotations.Nullable;
import javafx.geometry.Side;
import javafx.scene.Node;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Set;

public class TestingUnit {

    public Statistics statistics = new Statistics();
    // floating point formatting object
    private DecimalFormat df = new DecimalFormat("###.###");

    public HBox generateOutputConsole(@Nullable List<Double> collisionsDataSet,
                                      @Nullable List<Double> avgVelocitiesDataSet,
                                      @Nullable List<Double> avgAccelerationsDataSet) {

        HBox console = new HBox();
        /////////////////////////////////////////
        // CHART 1
        ///////////////////////////////////////
        // boundaries
        double chart1_lowerBound = 100000;
        double chart1_upperBound = 0;
        double chart2_lowerBound = 100000;
        double chart2_upperBound = 0;
        // series 1
        XYChart.Series collisionsSeries = new XYChart.Series();
        collisionsSeries.setName("Number of Collisions");
        // construct series
        if (collisionsDataSet != null) {
            int i = 1;
            for (Double dataPoint : collisionsDataSet) {
                // find min & max
                if (dataPoint < chart1_lowerBound) { chart1_lowerBound = dataPoint; }
                if (dataPoint > chart1_upperBound) { chart1_upperBound = dataPoint; }
                // add data point to series
                collisionsSeries.getData().add(new XYChart.Data(i++, dataPoint));
            }
        } else {
            // set bounds for initial dummy chart
            chart1_lowerBound = 0;
            chart1_upperBound = 5000;
        }
        // graduate x & y axis
        final NumberAxis chart1_xAxis = new NumberAxis(0, 50, 5);
        final NumberAxis chart1_yAxis = new NumberAxis(
                Math.round(chart1_lowerBound - chart1_lowerBound * 0.05),
                Math.round(chart1_upperBound + chart1_upperBound * 0.05),
                Math.round(((chart1_upperBound + chart1_upperBound * 0.05) - (chart1_lowerBound - chart1_lowerBound * 0.05)) / 5)
        );
        final ScatterChart<Number,Number> sc = new
                ScatterChart<Number,Number>(chart1_xAxis,chart1_yAxis);
        final int labelFontSize = 14;
        // axis formatting
        chart1_yAxis.setLabel("Collisions");
        chart1_xAxis.setLabel("Frame Window");
        chart1_yAxis.setStyle("-fx-font-size: " + labelFontSize + ";");
        chart1_xAxis.setStyle("-fx-font-size: " + labelFontSize + ";");
        // add data and set legend
        sc.getData().addAll(collisionsSeries);
        sc.setLegendVisible(true);
        sc.setLegendSide(Side.TOP);
        /////////////////////////////////////////
        // CHART 2
        ///////////////////////////////////////
        // series 2
        XYChart.Series velocitySeries = new XYChart.Series();
        velocitySeries.setName("Average Velocity");
        // series 3
        XYChart.Series accelerationSeries = new XYChart.Series();
        accelerationSeries.setName("Average Acceleration");
        // construct series
        if (avgVelocitiesDataSet != null && avgAccelerationsDataSet != null) {
            int i = 1;
            // velocity dataset
            for (Double dataPoint : avgVelocitiesDataSet) {
                // find min & max
                if (dataPoint < chart2_lowerBound) { chart2_lowerBound = dataPoint; }
                if (dataPoint > chart2_upperBound) { chart2_upperBound = dataPoint; }
                // add data point to series
                velocitySeries.getData().add(new XYChart.Data(i++, dataPoint));
            }
            // acceleration dataset
            i = 1;
            for (Double dataPoint : avgAccelerationsDataSet) {
                // find min & max
                if (dataPoint < chart2_lowerBound) { chart2_lowerBound = dataPoint; }
                if (dataPoint > chart2_upperBound) { chart2_upperBound = dataPoint; }
                // add data point to series
                accelerationSeries.getData().add(new XYChart.Data(i++, dataPoint));
            }
        } else {
            // set bounds for initial dummy chart
            chart2_lowerBound = 0;
            chart2_upperBound = 10;
        }
        // graduate x & y axis
        final NumberAxis chart2_xAxis = new NumberAxis(0, 50, 5);
        final NumberAxis chart2_yAxis = new NumberAxis(
                Math.round((chart2_lowerBound - chart2_lowerBound * 0.05) * 10) / 10.0,
                Math.round((chart2_upperBound + chart2_upperBound * 0.05) * 10) / 10.0,
                Math.round((((chart2_upperBound + chart2_upperBound * 0.05) - (chart2_lowerBound - chart2_lowerBound * 0.05)) / 5) * 10) / 10.0
        );
        final ScatterChart<Number,Number> sc2 = new
                ScatterChart<Number,Number>(chart2_xAxis,chart2_yAxis);
        // axis formatting
        chart2_yAxis.setLabel("Velocity    &    Acceleration");
        chart2_xAxis.setLabel("Frame Window");
        chart2_yAxis.setStyle("-fx-font-size: " + labelFontSize + ";");
        chart2_xAxis.setStyle("-fx-font-size: " + labelFontSize + ";");
        // add data and set legend
        sc2.getData().addAll(velocitySeries, accelerationSeries);
        sc2.setLegendVisible(true);
        sc2.setLegendSide(Side.TOP);
        /////////////////////////////////////////
        // TABLE VIEW
        ///////////////////////////////////////
        TableView tv = new TableView();
        // set column factories
        TableColumn<String, StatisticValues> statisticTypeColumn = new TableColumn<>("Statistic");
        statisticTypeColumn.setCellValueFactory(new PropertyValueFactory<>("statisticType"));
        statisticTypeColumn.setStyle("-fx-font-weight: bold");
        TableColumn<String, StatisticValues>  collisionColumn = new TableColumn<>("Collision");
        collisionColumn.setCellValueFactory(new PropertyValueFactory<>("statForCollisions"));
        TableColumn<String, StatisticValues> velocityColumn = new TableColumn<>("Velocity");
        velocityColumn.setCellValueFactory(new PropertyValueFactory<>("statForVelocities"));
        TableColumn<String, StatisticValues> accelerationColumn = new TableColumn<>("Accel.");
        accelerationColumn.setCellValueFactory(new PropertyValueFactory<>("statForAccelerations"));
        // add columns
        statisticTypeColumn.setMaxWidth(90);
        statisticTypeColumn.setMinWidth(90);
        statisticTypeColumn.setResizable(false);
        tv.getColumns().add(statisticTypeColumn);
        collisionColumn.setStyle("-fx-alignment: center;");
        collisionColumn.setMaxWidth(62);
        collisionColumn.setMinWidth(62);
        collisionColumn.setResizable(false);
        tv.getColumns().add(collisionColumn);
        velocityColumn.setStyle("-fx-alignment: center;");
        velocityColumn.setMaxWidth(62);
        velocityColumn.setMinWidth(62);
        velocityColumn.setResizable(false);
        tv.getColumns().add(velocityColumn);
        accelerationColumn.setStyle("-fx-alignment: center;");
        accelerationColumn.setMaxWidth(61);
        accelerationColumn.setMinWidth(61);
        accelerationColumn.setResizable(false);
        tv.getColumns().add(accelerationColumn);
        /////////////////////////////////////////
        // STATISTICS
        ///////////////////////////////////////
        if (null != collisionsDataSet && null != avgVelocitiesDataSet && null != avgAccelerationsDataSet) {
            // add statistics to table
            tv.getItems().add(new StatisticValues("Count",
                    df.format(statistics.getCount(collisionsDataSet)),
                    df.format(statistics.getCount(avgVelocitiesDataSet)),
                    df.format(statistics.getCount(avgAccelerationsDataSet))
            ));
            tv.getItems().add(new StatisticValues("Range",
                    df.format(statistics.getRange(collisionsDataSet)),
                    df.format(statistics.getRange(avgVelocitiesDataSet)),
                    df.format(statistics.getRange(avgAccelerationsDataSet))
            ));
            tv.getItems().add(new StatisticValues("Min. Value",
                    df.format(statistics.getMinimum(collisionsDataSet)),
                    df.format(statistics.getMinimum(avgVelocitiesDataSet)),
                    df.format(statistics.getMinimum(avgAccelerationsDataSet))
            ));
            tv.getItems().add(new StatisticValues("Max. Value",
                    df.format(statistics.getMaximum(collisionsDataSet)),
                    df.format(statistics.getMaximum(avgVelocitiesDataSet)),
                    df.format(statistics.getMaximum(avgAccelerationsDataSet))
            ));
            tv.getItems().add(new StatisticValues("Mean",
                    !Double.isNaN(statistics.calculateMean(collisionsDataSet)) ? df.format(statistics.calculateMean(collisionsDataSet)) : "0",
                    !Double.isNaN(statistics.calculateMean(avgVelocitiesDataSet)) ? df.format(statistics.calculateMean(avgVelocitiesDataSet)) : "0",
                    !Double.isNaN(statistics.calculateMean(avgAccelerationsDataSet)) ? df.format(statistics.calculateMean(avgAccelerationsDataSet)) : "0"
            ));
            tv.getItems().add(new StatisticValues("Median",
                    df.format(statistics.calculateMedian(collisionsDataSet)),
                    df.format(statistics.calculateMedian(avgVelocitiesDataSet)),
                    df.format(statistics.calculateMedian(avgAccelerationsDataSet))
            ));
            tv.getItems().add(new StatisticValues("Mode",
                    df.format(statistics.calculateMode(collisionsDataSet)),
                    df.format(statistics.calculateMode(avgVelocitiesDataSet)),
                    df.format(statistics.calculateMode(avgAccelerationsDataSet))
            ));
            tv.getItems().add(new StatisticValues("Stand. Dev.",
                    !Double.isNaN(statistics.calculateStandardDeviation(collisionsDataSet)) ? df.format(statistics.calculateStandardDeviation(collisionsDataSet)) : "0",
                    !Double.isNaN(statistics.calculateStandardDeviation(avgVelocitiesDataSet)) ? df.format(statistics.calculateStandardDeviation(avgVelocitiesDataSet)) : "0",
                    !Double.isNaN(statistics.calculateStandardDeviation(avgAccelerationsDataSet)) ? df.format(statistics.calculateStandardDeviation(avgAccelerationsDataSet)) : "0"
            ));
            tv.getItems().add(new StatisticValues("Variance",
                    !Double.isNaN(statistics.calculateVariance(collisionsDataSet)) ? df.format(statistics.calculateVariance(collisionsDataSet)) : "0",
                    !Double.isNaN(statistics.calculateVariance(avgVelocitiesDataSet)) ? df.format(statistics.calculateVariance(avgVelocitiesDataSet)) : "0",
                    !Double.isNaN(statistics.calculateVariance(avgAccelerationsDataSet)) ? df.format(statistics.calculateVariance(avgAccelerationsDataSet)) : "0"
            ));
            tv.getItems().add(new StatisticValues("Corr. Coeff.",
                    !Double.isNaN(statistics.calculateCorrelationCoefficient(collisionsDataSet)) ? df.format(statistics.calculateCorrelationCoefficient(collisionsDataSet)) : "0",
                    !Double.isNaN(statistics.calculateCorrelationCoefficient(avgVelocitiesDataSet)) ? df.format(statistics.calculateCorrelationCoefficient(avgVelocitiesDataSet)) : "0",
                    !Double.isNaN(statistics.calculateCorrelationCoefficient(avgAccelerationsDataSet)) ? df.format(statistics.calculateCorrelationCoefficient(avgAccelerationsDataSet)) : "0"
            ));
            tv.getItems().add(new StatisticValues("Skewness",
                    !Double.isNaN(statistics.calculateSkewness(collisionsDataSet)) ? df.format(statistics.calculateSkewness(collisionsDataSet)) : "0",
                    !Double.isNaN(statistics.calculateSkewness(avgVelocitiesDataSet)) ? df.format(statistics.calculateSkewness(avgVelocitiesDataSet)) : "0",
                    !Double.isNaN(statistics.calculateSkewness(avgAccelerationsDataSet)) ? df.format(statistics.calculateSkewness(avgAccelerationsDataSet)) : "0"
            ));
            tv.getItems().add(new StatisticValues("Kurtosis",
                    !Double.isNaN(statistics.calculateKurtosis(collisionsDataSet)) ? df.format(statistics.calculateKurtosis(collisionsDataSet)) : "0",
                    !Double.isNaN(statistics.calculateKurtosis(avgVelocitiesDataSet)) ? df.format(statistics.calculateKurtosis(avgVelocitiesDataSet)) : "0",
                    !Double.isNaN(statistics.calculateKurtosis(avgAccelerationsDataSet)) ? df.format(statistics.calculateKurtosis(avgAccelerationsDataSet)) : "0"
            ));
            tv.getItems().add(new StatisticValues("Coeff. of Var.",
                    !Double.isNaN(statistics.calculateCoefficientOfVariation(collisionsDataSet)) ? df.format(statistics.calculateCoefficientOfVariation(collisionsDataSet)) : "0",
                    !Double.isNaN(statistics.calculateCoefficientOfVariation(avgVelocitiesDataSet)) ? df.format(statistics.calculateCoefficientOfVariation(avgVelocitiesDataSet)) : "0",
                    !Double.isNaN(statistics.calculateCoefficientOfVariation(avgAccelerationsDataSet)) ? df.format(statistics.calculateCoefficientOfVariation(avgAccelerationsDataSet)) : "0"
            ));
            tv.getItems().add(new StatisticValues("Std. Err. Mean",
                    !Double.isNaN(statistics.calculateStandardErrorOfMean(collisionsDataSet)) ? df.format(statistics.calculateStandardErrorOfMean(collisionsDataSet)) : "0",
                    !Double.isNaN(statistics.calculateStandardErrorOfMean(avgVelocitiesDataSet)) ? df.format(statistics.calculateStandardErrorOfMean(avgVelocitiesDataSet)) : "0",
                    !Double.isNaN(statistics.calculateStandardErrorOfMean(avgAccelerationsDataSet)) ? df.format(statistics.calculateStandardErrorOfMean(avgAccelerationsDataSet)) : "0"
            ));
        } else { tv.getItems().add(new StatisticValues(null,null,null,null)); /* dummy data */ };
        /////////////////////////////////////////
        // STYLES
        ///////////////////////////////////////
        // set TableView styles
        tv.setMaxWidth(277);
        tv.setFixedCellSize(26.7);
        // chart symbol styling
        Set<Node> nodes;
        // first series symbols
        if (null != sc.lookupAll(".default-color0.chart-symbol")) {
            nodes = sc.lookupAll(".default-color0.chart-symbol");
            for (Node n : nodes) {
                n.setStyle(""
                        // + "-fx-background-color: #000099;\n"
                        + "-fx-background-insets: 0, 1;\n"
                        + "-fx-background-radius: 4px;\n"
                        + "-fx-padding: 4px;"
                );
            }
        }
        // second series symbols
        if (null != sc2.lookupAll(".default-color0.chart-symbol")) {
            nodes = sc2.lookupAll(".default-color0.chart-symbol");
            for (Node n : nodes) {
                n.setStyle(""
                        // + "-fx-background-color: #000099;\n"
                        + "-fx-background-insets: 0, 1;\n"
                        + "-fx-background-radius: 4px;\n"
                        + "-fx-padding: 4px;"
                );
            }
        }
        // third series symbols
        if (null != sc2.lookupAll(".default-color1.chart-symbol")) {
            nodes = sc2.lookupAll(".default-color1.chart-symbol");
            for (Node n : nodes) {
                n.setStyle(""
                        // + "-fx-background-color: #000099;\n"
                        + "-fx-background-insets: 0, 1;\n"
                        + "-fx-background-radius: 4px;\n"
                        + "-fx-padding: 4px;"
                );
            }
        }
        // Draw borders
        Node node = sc.lookup(".chart-plot-background");
        final int borderWidth = 5;
        node.setStyle(""
                + "-fx-border-color: dimgrey;"
                + "-fx-border-style: solid;"
                + "-fx-border-width: " + borderWidth + "px;"
                + "-fx-border-insets: -" + borderWidth + "px;"
        );
        node = sc2.lookup(".chart-plot-background");
        node.setStyle(""
                + "-fx-border-color: dimgrey;"
                + "-fx-border-style: solid;"
                + "-fx-border-width: " + borderWidth + "px;"
                + "-fx-border-insets: -" + borderWidth + "px;"
        );
        /////////////////////////////////////////
        // CONSOLE CONSTRUCTION & RETURN
        ///////////////////////////////////////
        VBox collisionsBox = new VBox();
        collisionsBox.setStyle("-fx-padding: 20 0 10 0");
        VBox displacementBox = new VBox();
        displacementBox.setStyle("-fx-padding: 20 0 10 0");
        VBox statsBox= new VBox();
        // add labels and charts to Nodes:
        collisionsBox.getChildren().add(sc);
        displacementBox.getChildren().add(sc2);
        statsBox.getChildren().add(tv);
        // add Nodes to console:
        console.getChildren().addAll(collisionsBox, displacementBox, statsBox);
        console.setStyle("-fx-background-color: silver;");

        return console;
    }

    // helper class for TableView
    public class StatisticValues {

        private String statisticType;
        private String statForCollisions;
        private String statForVelocities;
        private String statForAccelerations;

        public StatisticValues(@Nullable String statisticType, @Nullable String statForCollisions, @Nullable String statForVelocities, @Nullable String statForAccelerations) {
            this.statisticType = statisticType;
            this.statForCollisions = statForCollisions;
            this.statForVelocities = statForVelocities;
            this.statForAccelerations = statForAccelerations;
        }

        public String getStatisticType() { return statisticType; }
        public String getStatForCollisions() { return statForCollisions; }
        public String getStatForVelocities() { return statForVelocities; }
        public String getStatForAccelerations() { return statForAccelerations; }
    }
}
