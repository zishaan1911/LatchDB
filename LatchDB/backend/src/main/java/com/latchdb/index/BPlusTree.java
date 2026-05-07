package com.latchdb.index;

import com.latchdb.storage.BufferManager;
import com.latchdb.storage.Page;
import com.latchdb.storage.Record;
import com.latchdb.schema.Table;
import java.io.IOException;

public class BPlusTree {
    private final int degree;
    private int rootPageId;
    private final BufferManager bufferManager;
    private final Table table;
    private int nextPageId;

    public BPlusTree(int degree, BufferManager bufferManager, Table table, int rootPageId) throws IOException {
        this.degree = degree;
        this.bufferManager = bufferManager;
        this.table = table;
        this.rootPageId = rootPageId;
        this.nextPageId = rootPageId + 1;
        
        // Initialize root as an empty leaf
        LeafNode root = new LeafNode(rootPageId);
        saveNode(root);
    }

    public BPlusTree(int degree, BufferManager bufferManager, Table table, int rootPageId, int nextPageId) {
        this.degree = degree;
        this.bufferManager = bufferManager;
        this.table = table;
        this.rootPageId = rootPageId;
        this.nextPageId = nextPageId;
    }

    private void saveNode(Node node) throws IOException {
        Page page = bufferManager.getPage(node.getPageId());
        NodeSerializer.serialize(node, page, table);
        bufferManager.flushPage(page);
    }

    private Node loadNode(int pageId) throws IOException {
        Page page = bufferManager.getPage(pageId);
        return NodeSerializer.deserialize(page, table);
    }

    public void insert(int key, Record record) throws IOException {
        Node root = loadNode(rootPageId);
        if (root.getKeys().size() == 2 * degree - 1) {
            InternalNode newRoot = new InternalNode(nextPageId++);
            newRoot.getChildrenPageIds().add(rootPageId);
            int oldRootPageId = rootPageId;
            rootPageId = newRoot.getPageId();
            splitChild(newRoot, 0, root);
            insertNonFull(newRoot, key, record);
        } else {
            insertNonFull(root, key, record);
        }
    }

    private void splitChild(InternalNode parent, int i, Node child) throws IOException {
        Node newNode;
        if (child.isLeaf()) {
            LeafNode leafChild = (LeafNode) child;
            LeafNode newLeaf = new LeafNode(nextPageId++);
            // Move half of keys and records to new leaf
            for (int j = 0; j < degree; j++) {
                newLeaf.getKeys().add(0, leafChild.getKeys().remove(leafChild.getKeys().size() - 1));
                newLeaf.getRecords().add(0, leafChild.getRecords().remove(leafChild.getRecords().size() - 1));
            }
            newLeaf.setNextLeafPageId(leafChild.getNextLeafPageId());
            leafChild.setNextLeafPageId(newLeaf.getPageId());
            newNode = newLeaf;
            parent.getKeys().add(i, newLeaf.getKeys().get(0));
        } else {
            InternalNode internalChild = (InternalNode) child;
            InternalNode newInternal = new InternalNode(nextPageId++);
            parent.getKeys().add(i, internalChild.getKeys().remove(degree - 1));
            for (int j = 0; j < degree - 1; j++) {
                newInternal.getKeys().add(internalChild.getKeys().remove(degree - 1));
            }
            for (int j = 0; j < degree; j++) {
                newInternal.getChildrenPageIds().add(0, internalChild.getChildrenPageIds().remove(internalChild.getChildrenPageIds().size() - 1));
            }
            newNode = newInternal;
        }
        parent.getChildrenPageIds().add(i + 1, newNode.getPageId());
        saveNode(child);
        saveNode(newNode);
        saveNode(parent);
    }

    private void insertNonFull(Node node, int key, Record record) throws IOException {
        int i = node.getKeys().size() - 1;
        if (node.isLeaf()) {
            LeafNode leaf = (LeafNode) node;
            while (i >= 0 && key < leaf.getKeys().get(i)) {
                i--;
            }
            leaf.getKeys().add(i + 1, key);
            leaf.getRecords().add(i + 1, record);
            saveNode(leaf);
        } else {
            InternalNode internal = (InternalNode) node;
            while (i >= 0 && key < internal.getKeys().get(i)) {
                i--;
            }
            i++;
            Node child = loadNode(internal.getChildrenPageIds().get(i));
            if (child.getKeys().size() == 2 * degree - 1) {
                splitChild(internal, i, child);
                if (key >= internal.getKeys().get(i)) {
                    i++;
                }
                child = loadNode(internal.getChildrenPageIds().get(i));
            }
            insertNonFull(child, key, record);
        }
    }

    public int getRootPageId() {
        return rootPageId;
    }

    public int getNextPageId() {
        return nextPageId;
    }

    public Record search(int key) throws IOException {
        return search(loadNode(rootPageId), key);
    }

    private Record search(Node node, int key) throws IOException {
        int i = 0;
        while (i < node.getKeys().size() && key >= node.getKeys().get(i)) {
            i++;
        }
        if (node.isLeaf()) {
            int j = 0;
            while (j < node.getKeys().size() && key > node.getKeys().get(j)) {
                j++;
            }
            if (j < node.getKeys().size() && key == node.getKeys().get(j)) {
                return ((LeafNode) node).getRecords().get(j);
            }
            return null;
        } else {
            return search(loadNode(((InternalNode) node).getChildrenPageIds().get(i)), key);
        }
    }

    public LeafNode findLeafNode(int key) throws IOException {
        Node current = loadNode(rootPageId);
        while (!current.isLeaf()) {
            int i = 0;
            while (i < current.getKeys().size() && key >= current.getKeys().get(i)) {
                i++;
            }
            current = loadNode(((InternalNode) current).getChildrenPageIds().get(i));
        }
        return (LeafNode) current;
    }

    public LeafNode loadLeafNode(int pageId) throws IOException {
        return (LeafNode) loadNode(pageId);
    }

    public void update(int key, Record newRecord) throws IOException {
        LeafNode leaf = findLeafNode(key);
        for (int i = 0; i < leaf.getKeys().size(); i++) {
            if (leaf.getKeys().get(i) == key) {
                leaf.getRecords().set(i, newRecord);
                saveNode(leaf);
                return;
            }
        }
    }

    public void delete(int key) throws IOException {
        LeafNode leaf = findLeafNode(key);
        for (int i = 0; i < leaf.getKeys().size(); i++) {
            if (leaf.getKeys().get(i) == key) {
                leaf.getKeys().remove(i);
                leaf.getRecords().remove(i);
                saveNode(leaf);
                return;
            }
        }
    }
}
