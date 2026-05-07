package com.latchdb.query.execution;

import com.latchdb.analytics.AnalyticsFunctions;
import com.latchdb.storage.Record;
import java.util.ArrayList;
import java.util.List;

public class AggregateIterator implements Iterator {
    public enum AggregateType {
        SUM, AVG, MIN, MAX, COUNT, VARIANCE, CORRELATION
    }

    private final Iterator child;
    private final String column;
    private final AggregateType type;
    private boolean consumed = false;
    private final AnalyticsFunctions analytics = new AnalyticsFunctions();

    public AggregateIterator(Iterator child, String column, AggregateType type) {
        this.child = child;
        this.column = column;
        this.type = type;
    }

    @Override
    public void open() throws Exception {
        child.open();
    }

    @Override
    public Record next() throws Exception {
        if (consumed) return null;
        List<Double> valuesX = new ArrayList<>();
        List<Double> valuesY = new ArrayList<>();
        Record r;
        long count = 0;
        
        String[] cols = column.split(",");

        while ((r = child.next()) != null) {
            Object valX = r.get(cols[0].trim());
            if (valX instanceof Number) {
                valuesX.add(((Number) valX).doubleValue());
            }
            if (type == AggregateType.CORRELATION && cols.length > 1) {
                Object valY = r.get(cols[1].trim());
                if (valY instanceof Number) {
                    valuesY.add(((Number) valY).doubleValue());
                }
            }
            count++;
        }
        consumed = true;
        Record result = new Record();
        String resultCol = type.name() + "(" + column + ")";
        
        switch (type) {
            case SUM: result.put(resultCol, valuesX.stream().mapToDouble(d -> d).sum()); break;
            case AVG: result.put(resultCol, analytics.avg(valuesX)); break;
            case MIN: result.put(resultCol, valuesX.stream().mapToDouble(d -> d).min().orElse(0)); break;
            case MAX: result.put(resultCol, valuesX.stream().mapToDouble(d -> d).max().orElse(0)); break;
            case COUNT: result.put(resultCol, (double) count); break;
            case VARIANCE: result.put(resultCol, analytics.variance(valuesX)); break;
            case CORRELATION: result.put(resultCol, analytics.correlation(valuesX, valuesY)); break;
        }
        return result;
    }

    @Override
    public void close() throws Exception {
        child.close();
    }
}
