package com.latchdb.index;

import java.util.ArrayList;
import java.util.List;

public class InternalNode extends Node {
    private final List<Integer> childrenPageIds;

    public InternalNode(int pageId) {
        super(pageId);
        this.childrenPageIds = new ArrayList<>();
    }

    public List<Integer> getChildrenPageIds() {
        return childrenPageIds;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }
}
