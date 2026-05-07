package com.latchdb.index;

import com.latchdb.storage.Page;
import com.latchdb.storage.Record;
import com.latchdb.schema.Table;
import java.nio.ByteBuffer;

public class NodeSerializer {
    public static void serialize(Node node, Page page, Table table) {
        ByteBuffer buffer = ByteBuffer.wrap(page.getData());
        buffer.put((byte) (node.isLeaf() ? 1 : 0));
        buffer.putInt(node.getKeys().size());
        
        if (node.isLeaf()) {
            LeafNode leaf = (LeafNode) node;
            buffer.putInt(leaf.getNextLeafPageId());
            for (int i = 0; i < node.getKeys().size(); i++) {
                buffer.putInt(node.getKeys().get(i));
                byte[] data = leaf.getRecords().get(i).serialize(table);
                buffer.putInt(data.length);
                buffer.put(data);
            }
        } else {
            InternalNode internal = (InternalNode) node;
            for (int key : node.getKeys()) {
                buffer.putInt(key);
            }
            for (int childId : internal.getChildrenPageIds()) {
                buffer.putInt(childId);
            }
        }
        page.setDirty(true);
    }

    public static Node deserialize(Page page, Table table) {
        ByteBuffer buffer = ByteBuffer.wrap(page.getData());
        boolean isLeaf = buffer.get() == 1;
        int numKeys = buffer.getInt();
        
        if (isLeaf) {
            LeafNode leaf = new LeafNode(page.getPageId());
            leaf.setNextLeafPageId(buffer.getInt());
            for (int i = 0; i < numKeys; i++) {
                leaf.getKeys().add(buffer.getInt());
                int length = buffer.getInt();
                byte[] data = new byte[length];
                buffer.get(data);
                leaf.getRecords().add(Record.deserialize(data, table));
            }
            return leaf;
        } else {
            InternalNode internal = new InternalNode(page.getPageId());
            for (int i = 0; i < numKeys; i++) {
                internal.getKeys().add(buffer.getInt());
            }
            for (int i = 0; i <= numKeys; i++) {
                internal.getChildrenPageIds().add(buffer.getInt());
            }
            return internal;
        }
    }
}
