package com.latchdb.index;

import com.latchdb.storage.Record;
import java.util.ArrayList;
import java.util.List;

public class LeafNode extends Node {
    private final List<Record> records;
    private int nextLeafPageId = -1;

    public LeafNode(int pageId) {
        super(pageId);
        this.records = new ArrayList<>();
    }

    public List<Record> getRecords() {
        return records;
    }

    public int getNextLeafPageId() {
        return nextLeafPageId;
    }

    public void setNextLeafPageId(int nextLeafPageId) {
        this.nextLeafPageId = nextLeafPageId;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }
}
