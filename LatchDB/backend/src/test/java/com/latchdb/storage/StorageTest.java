package com.latchdb.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class StorageTest {

    @TempDir
    Path tempDir;

    @Test
    public void testFileAndBufferManager() throws IOException {
        String dbPath = tempDir.resolve("test.db").toString();
        FileManager fm = new FileManager(dbPath);
        BufferManager bm = new BufferManager(2, fm);

        // Test writing a page
        Page p0 = bm.getPage(0);
        byte[] data = "Hello LatchDB".getBytes();
        p0.writeBytes(0, data);
        bm.flushPage(p0);

        // Verify page content after reloading
        fm.close();
        fm = new FileManager(dbPath);
        bm = new BufferManager(2, fm);
        Page p0_reload = bm.getPage(0);
        byte[] readData = p0_reload.readBytes(0, data.length);
        assertArrayEquals(data, readData);

        fm.close();
    }
}
