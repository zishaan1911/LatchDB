package com.latchdb.index;

import com.latchdb.storage.BufferManager;
import com.latchdb.storage.FileManager;
import com.latchdb.storage.Record;
import com.latchdb.schema.Column;
import com.latchdb.schema.DataType;
import com.latchdb.schema.Table;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BPlusTreeTest {

    @TempDir
    Path tempDir;

    @Test
    public void testInsertAndSearch() throws IOException {
        String dbPath = tempDir.resolve("test_index.db").toString();
        FileManager fm = new FileManager(dbPath);
        BufferManager bm = new BufferManager(10, fm);
        
        List<Column> columns = List.of(
            new Column("id", DataType.INTEGER, true),
            new Column("name", DataType.STRING, false)
        );
        Table table = new Table("users", columns);
        
        BPlusTree tree = new BPlusTree(2, bm, table, 0);
        
        for (int i = 1; i <= 10; i++) {
            Record r = new Record();
            r.put("id", i);
            r.put("name", "User" + i);
            tree.insert(i, r);
        }
        
        for (int i = 1; i <= 10; i++) {
            Record r = tree.search(i);
            assertNotNull(r, "Record " + i + " should not be null");
            assertEquals(i, r.get("id"));
            assertEquals("User" + i, r.get("name"));
        }
        
        assertNull(tree.search(11));
        
        fm.close();
    }
}
