
import java.util.ArrayList;
import java.util.List;

public class Statistics {

    double getMinimum(List<Double> list) {
        double min = list.get(0);
        for (double dataPoint : list) {
            if (dataPoint < min) {
                min = dataPoint;
            }
        }
        return min;
    }

    double getMaximum(List<Double> list) {
        double max = list.get(0);
        for (double dataPoint : list) {
            if (dataPoint > max) {
                max = dataPoint;
            }
        }
        return max;
    }

    double getRange(List<Double> list) {
        double max = getMaximum(list);
        double min = getMinimum(list);
        return max - min;
    }

    int getCount(List<Double> list) {
        return list.size();
    }

    double calculateSkewness(List<Double> list) {
        double mean = calculateMean(list);
        double sumOfCubedDifferences = 0;
        for (double dataPoint : list) {
            sumOfCubedDifferences += Math.pow(dataPoint - mean, 3);
        }
        return (sumOfCubedDifferences / ((list.size() - 1) * Math.pow(calculateStandardDeviation(list), 3)));
    }

    double calculateKurtosis(List<Double> list) {
        double mean = calculateMean(list);
        double sumOfCubedDifferences = 0;
        for (double dataPoint : list) {
            sumOfCubedDifferences += Math.pow(dataPoint - mean, 4);
        }
        return (sumOfCubedDifferences / ((list.size() - 1) * Math.pow(calculateStandardDeviation(list), 4)));
    }

    double calculateCoefficientOfVariation(List<Double> list) {
        return calculateStandardDeviation(list) / calculateMean(list);
    }

    double calculateStandardErrorOfMean(List<Double> list) {
        return calculateStandardDeviation(list) / Math.sqrt(list.size());
    }

    double calculateMean(List<Double> list) {
        double sum = 0;
        for (Double dataPoint : list) {
            sum += (double) dataPoint;
        }
        return sum / list.size();
    }

    double calculateMedian(List<Double> list) {
        list.sort(null);
        int targetIndex;
        if (list.size() % 2 == 0) {
            // cardinality is even
            targetIndex = list.size() / 2;
            double combinedMedian = list.get(targetIndex) + list.get(targetIndex - 1);
            return combinedMedian / 2;
        } else {
            // cardinality is odd
            targetIndex = list.size() / 2;
            return list.get(targetIndex);
        }
    }

    double calculateMode(List<Double> list) {
        double mostFrequentValue = 0.0;
        int currentHighValue = 0;
        int occurances;

        for (int i = 0; i < list.size(); i++) {
            occurances = 0;
            for (int j = 0; j < list.size(); j++) {
                if (list.get(i).equals(list.get(j))) {
                    occurances++;
                }
            }
            if (occurances > currentHighValue) {
                currentHighValue = occurances;
                mostFrequentValue = (double) list.get(i);
            }
        }

        return mostFrequentValue;
    }

    double calculateVariance(List<Double> list) {
        double sumOfSquaredDeviations = 0.0;
        double mean = this.calculateMean(list);
        // sum of squared deviations from the mean:
        for (Double dataPoint : list) {
            sumOfSquaredDeviations += Math.pow((dataPoint - mean), 2);
        }

        return sumOfSquaredDeviations / (list.size() - 1);
    }

    double calculateStandardDeviation(List<Double> list) {
        return Math.sqrt(calculateVariance(list));
    }

    double calculateCorrelationCoefficient(List<Double> xValueList) {
        double standardDeviationOfX = calculateStandardDeviation(xValueList);
        double standardDeviationOfY;
        double meanOfX = calculateMean(xValueList);
        double meanOfY;
        int sampleSize = xValueList.size();

        List<Double> yValueList = new ArrayList<>();
        for (int i = 1; i <= sampleSize; i++) {
            yValueList.add((double) i);
        }
        standardDeviationOfY = calculateStandardDeviation(yValueList);
        meanOfY = calculateMean(yValueList);

        double sum = 0.0;
        // calculate each series term:
        for (int i = 0; i < sampleSize; i++) {
            sum += (
                    ((xValueList.get(i) - meanOfX) * (yValueList.get(i) - meanOfY))
                            / (standardDeviationOfX * standardDeviationOfY)
            );
        }

        return sum / (sampleSize - 1);
    }

}
