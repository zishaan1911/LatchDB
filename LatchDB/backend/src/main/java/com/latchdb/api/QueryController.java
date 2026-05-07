package com.latchdb.api;

import com.latchdb.query.execution.ExecutionEngine;
import com.latchdb.schema.SchemaManager;
import com.latchdb.storage.BufferManager;
import com.latchdb.storage.FileManager;
import com.latchdb.storage.Record;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
public class QueryController {
    private final ExecutionEngine engine;
    private final SchemaManager schemaManager;

    public QueryController() throws IOException {
        FileManager fm = new FileManager("latchdb.db");
        BufferManager bm = new BufferManager(100, fm);
        this.schemaManager = new SchemaManager();
        this.engine = new ExecutionEngine(schemaManager, bm);
    }

    @PostMapping("/query")
    public List<Map<String, Object>> query(@RequestBody Map<String, String> body) throws Exception {
        String sql = body.get("sql");
        List<Record> records = engine.execute(sql);
        if (records == null) return new ArrayList<>();
        return records.stream().map(Record::getValues).collect(Collectors.toList());
    }

    @GetMapping("/tables")
    public List<String> getTables() {
        return new ArrayList<>(schemaManager.getTables().keySet());
    }

    @PostMapping("/table/create")
    public void createTable(@RequestBody Map<String, Object> body) throws Exception {
        String name = (String) body.get("name");
        List<Map<String, Object>> columns = (List<Map<String, Object>>) body.get("columns");
        StringBuilder sql = new StringBuilder("CREATE TABLE ").append(name).append(" (");
        for (int i = 0; i < columns.size(); i++) {
            Map<String, Object> col = columns.get(i);
            sql.append(col.get("name")).append(" ").append(col.get("type"));
            if (col.containsKey("primaryKey") && (Boolean) col.get("primaryKey")) {
                sql.append(" PRIMARY KEY");
            }
            if (i < columns.size() - 1) sql.append(", ");
        }
        sql.append(")");
        engine.execute(sql.toString());
    }

    @PostMapping("/import/csv")
    public void importCsv(@RequestBody Map<String, String> body) throws Exception {
        String tableName = body.get("tableName");
        String csvContent = body.get("csv");
        String[] lines = csvContent.split("\n");
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            // Basic CSV to SQL INSERT (assuming values are already formatted or simple)
            String sql = "INSERT INTO " + tableName + " VALUES (" + line + ")";
            engine.execute(sql);
        }
    }
}
