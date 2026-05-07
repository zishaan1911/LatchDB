package com.latchdb.query.execution;

import com.latchdb.storage.Record;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SortIterator implements Iterator {
    private final Iterator child;
    private final List<String> sortColumns;
    private final List<Boolean> ascending;
    private List<Record> sortedRecords;
    private int cursor;

    public SortIterator(Iterator child, List<String> sortColumns, List<Boolean> ascending) {
        this.child = child;
        this.sortColumns = sortColumns;
        this.ascending = ascending;
    }

    @Override
    public void open() throws Exception {
        child.open();
        sortedRecords = new ArrayList<>();
        Record r;
        while ((r = child.next()) != null) {
            sortedRecords.add(r);
        }
        child.close();

        sortedRecords.sort((r1, r2) -> {
            for (int i = 0; i < sortColumns.size(); i++) {
                String col = sortColumns.get(i);
                Object v1 = r1.get(col);
                Object v2 = r2.get(col);
                int cmp;
                if (v1 instanceof Comparable && v2 != null && v1.getClass().equals(v2.getClass())) {
                    cmp = ((Comparable) v1).compareTo(v2);
                } else if (v1 instanceof Number && v2 instanceof Number) {
                    cmp = Double.compare(((Number) v1).doubleValue(), ((Number) v2).doubleValue());
                } else {
                    cmp = 0;
                }
                if (cmp != 0) {
                    return ascending.get(i) ? cmp : -cmp;
                }
            }
            return 0;
        });
        cursor = 0;
    }

    @Override
    public Record next() throws Exception {
        if (cursor < sortedRecords.size()) {
            return sortedRecords.get(cursor++);
        }
        return null;
    }

    @Override
    public void close() throws Exception {
        // Already closed child in open()
    }
}
