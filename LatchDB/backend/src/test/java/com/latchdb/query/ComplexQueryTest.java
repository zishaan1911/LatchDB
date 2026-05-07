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

public class ComplexQueryTest {

    @TempDir
    Path tempDir;

    @Test
    public void testComplexQueries() throws Exception {
        String dbPath = tempDir.resolve("complex_test.db").toString();
        String schemaPath = tempDir.resolve("schema.json").toString();
        FileManager fm = new FileManager(dbPath);
        BufferManager bm = new BufferManager(10, fm);
        SchemaManager sm = new SchemaManager(schemaPath);
        ExecutionEngine engine = new ExecutionEngine(sm, bm);

        engine.execute("CREATE TABLE employees (id INT PRIMARY KEY, name STRING, salary DOUBLE, dept_id INT)");
        engine.execute("INSERT INTO employees VALUES (1, 'Alice', 90000.0, 10)");
        engine.execute("INSERT INTO employees VALUES (2, 'Bob', 80000.0, 10)");
        engine.execute("INSERT INTO employees VALUES (3, 'Charlie', 70000.0, 20)");
        engine.execute("INSERT INTO employees VALUES (4, 'David', 85000.0, 20)");

        // Test Filtering
        List<Record> highSalary = engine.execute("SELECT * FROM employees WHERE salary > 75000");
        assertEquals(3, highSalary.size());

        // Test Projection
        List<Record> namesOnly = engine.execute("SELECT name FROM employees WHERE dept_id = 10");
        assertEquals(2, namesOnly.size());
        assertTrue(namesOnly.get(0).getValues().containsKey("name"));
        assertFalse(namesOnly.get(0).getValues().containsKey("salary"));

        // Test Aggregation
        List<Record> countResult = engine.execute("SELECT COUNT(*) FROM employees");
        assertEquals(1, countResult.size());
        assertEquals(4.0, countResult.get(0).get("COUNT(*)"));

        List<Record> avgSalary = engine.execute("SELECT AVG(salary) FROM employees WHERE dept_id = 10");
        assertEquals(1, avgSalary.size());
        assertEquals(85000.0, avgSalary.get(0).get("AVG(salary)"));

        fm.close();
    }
}
