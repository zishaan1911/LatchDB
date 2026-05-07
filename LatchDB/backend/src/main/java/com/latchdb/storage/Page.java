package com.latchdb.storage;

import java.util.Arrays;

public class Page {
    public static final int PAGE_SIZE = 4096; // 4KB
    private final byte[] data;
    private final int pageId;
    private boolean dirty;

    public Page(int pageId) {
        this.pageId = pageId;
        this.data = new byte[PAGE_SIZE];
        this.dirty = false;
    }

    public Page(int pageId, byte[] data) {
        this.pageId = pageId;
        this.data = Arrays.copyOf(data, PAGE_SIZE);
        this.dirty = false;
    }

    public byte[] getData() {
        return data;
    }

    public int getPageId() {
        return pageId;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public void writeBytes(int offset, byte[] bytes) {
        System.arraycopy(bytes, 0, data, offset, bytes.length);
        this.dirty = true;
    }

    public byte[] readBytes(int offset, int length) {
        byte[] result = new byte[length];
        System.arraycopy(data, offset, result, 0, length);
        return result;
    }
}
