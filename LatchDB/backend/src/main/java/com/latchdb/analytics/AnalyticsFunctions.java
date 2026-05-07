package com.latchdb.analytics;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import java.util.List;

public class AnalyticsFunctions {
    public double avg(List<Double> values) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        values.forEach(stats::addValue);
        return stats.getMean();
    }

    public double variance(List<Double> values) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        values.forEach(stats::addValue);
        return stats.getVariance();
    }

    public double correlation(List<Double> x, List<Double> y) {
        if (x.size() != y.size() || x.isEmpty()) return 0;
        double[] xArray = x.stream().mapToDouble(Double::doubleValue).toArray();
        double[] yArray = y.stream().mapToDouble(Double::doubleValue).toArray();
        return new PearsonsCorrelation().correlation(xArray, yArray);
    }
}
