package com.latchdb.index;

import java.util.ArrayList;
import java.util.List;

public abstract class Node {
    protected final int pageId;
    protected final List<Integer> keys;

    public Node(int pageId) {
        this.pageId = pageId;
        this.keys = new ArrayList<>();
    }

    public int getPageId() {
        return pageId;
    }

    public List<Integer> getKeys() {
        return keys;
    }

    public abstract boolean isLeaf();
}
