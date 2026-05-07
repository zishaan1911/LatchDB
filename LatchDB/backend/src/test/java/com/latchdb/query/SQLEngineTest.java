package com.latchdb.query;

import com.latchdb.query.execution.ExecutionEngine;
import com.latchdb.schema.SchemaManager;
import com.latchdb.storage.BufferManager;
import com.latchdb.storage.FileManager;
import com.latchdb.storage.Record;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SQLEngineTest {

    @TempDir
    Path tempDir;

    @Test
    public void testEndToEndSQL() throws Exception {
        String dbPath = tempDir.resolve("sql_test.db").toString();
        FileManager fm = new FileManager(dbPath);
        BufferManager bm = new BufferManager(10, fm);
        SchemaManager sm = new SchemaManager();
        ExecutionEngine engine = new ExecutionEngine(sm, bm);

        engine.execute("CREATE TABLE students (id INT PRIMARY KEY, name STRING, score DOUBLE)");
        engine.execute("INSERT INTO students VALUES (1, 'Alice', 95.5)");
        engine.execute("INSERT INTO students VALUES (2, 'Bob', 88.0)");
        
        List<Record> results = engine.execute("SELECT * FROM students");
        assertEquals(2, results.size());
        
        // We need to be careful with ordering, but since we insert in order and it's a B+ tree, it should be ordered by ID.
        assertEquals("Alice", results.get(0).get("name"));
        assertEquals(88.0, results.get(1).get("score"));
        
        // Test UPDATE
        engine.execute("UPDATE students SET score = 99.0 WHERE id = 1");
        List<Record> updateResults = engine.execute("SELECT score FROM students WHERE id = 1");
        assertEquals(1, updateResults.size());
        assertEquals(99.0, updateResults.get(0).get("score"));

        // Test DELETE
        engine.execute("DELETE FROM students WHERE id = 1");
        List<Record> deleteResults = engine.execute("SELECT * FROM students");
        assertEquals(1, deleteResults.size());
        assertEquals("Bob", deleteResults.get(0).get("name"));

        // Test ORDER BY and LIMIT
        engine.execute("INSERT INTO students VALUES (3, 'Charlie', 75.0)");
        engine.execute("INSERT INTO students VALUES (4, 'David', 82.0)");
        List<Record> sortedResults = engine.execute("SELECT name FROM students ORDER BY score DESC LIMIT 2");
        assertEquals(2, sortedResults.size());
        assertEquals("Bob", sortedResults.get(0).get("name")); // Bob has 88.0
        assertEquals("David", sortedResults.get(1).get("name")); // David has 82.0

        fm.close();
    }
}
