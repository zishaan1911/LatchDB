package com.latchdb.storage;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class BufferManager {
    private final int capacity;
    private final FileManager fileManager;
    private final Map<Integer, Page> pageBuffer;

    public BufferManager(int capacity, FileManager fileManager) {
        this.capacity = capacity;
        this.fileManager = fileManager;
        this.pageBuffer = new LinkedHashMap<Integer, Page>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, Page> eldest) {
                if (size() > capacity) {
                    try {
                        flushPage(eldest.getValue());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
                return false;
            }
        };
    }

    public synchronized Page getPage(int pageId) throws IOException {
        if (pageBuffer.containsKey(pageId)) {
            return pageBuffer.get(pageId);
        }
        Page page = fileManager.readPage(pageId);
        pageBuffer.put(pageId, page);
        return page;
    }

    public synchronized void flushPage(Page page) throws IOException {
        if (page.isDirty()) {
            fileManager.writePage(page);
            page.setDirty(false);
        }
    }

    public synchronized void flushAll() throws IOException {
        for (Page page : pageBuffer.values()) {
            flushPage(page);
        }
    }
}
