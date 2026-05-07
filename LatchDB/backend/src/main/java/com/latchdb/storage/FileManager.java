package com.latchdb.storage;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.File;

public class FileManager {
    private final File dbFile;
    private final RandomAccessFile raf;

    public FileManager(String filePath) throws IOException {
        this.dbFile = new File(filePath);
        this.raf = new RandomAccessFile(dbFile, "rw");
    }

    public synchronized void writePage(Page page) throws IOException {
        long offset = (long) page.getPageId() * Page.PAGE_SIZE;
        raf.seek(offset);
        raf.write(page.getData());
    }

    public synchronized Page readPage(int pageId) throws IOException {
        long offset = (long) pageId * Page.PAGE_SIZE;
        if (offset >= raf.length()) {
            return new Page(pageId);
        }
        raf.seek(offset);
        byte[] data = new byte[Page.PAGE_SIZE];
        raf.readFully(data);
        return new Page(pageId, data);
    }

    public long getFileSize() throws IOException {
        return raf.length();
    }

    public void close() throws IOException {
        raf.close();
    }
}
