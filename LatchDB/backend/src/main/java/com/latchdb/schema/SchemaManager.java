package com.latchdb.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SchemaManager {
    private Map<String, Table> tables = new HashMap<>();
    private Map<String, Integer> tableRootPages = new HashMap<>();
    private Map<String, Integer> tableNextPages = new HashMap<>();
    private final String schemaPath;
    private final ObjectMapper mapper;

    public SchemaManager() {
        this("schema.json");
    }

    public SchemaManager(String schemaPath) {
        this.schemaPath = schemaPath;
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        load();
    }

    public void createTable(Table table, int rootPageId) {
        tables.put(table.getName(), table);
        tableRootPages.put(table.getName(), rootPageId);
        tableNextPages.put(table.getName(), rootPageId + 1);
        save();
    }

    public Table getTable(String name) {
        return tables.get(name);
    }

    public int getRootPageId(String tableName) {
        return tableRootPages.getOrDefault(tableName, 0);
    }

    public int getNextPageId(String tableName) {
        return tableNextPages.getOrDefault(tableName, 1);
    }

    public void updateTableMetadata(String tableName, int rootPageId, int nextPageId) {
        tableRootPages.put(tableName, rootPageId);
        tableNextPages.put(tableName, nextPageId);
        save();
    }

    private void save() {
        try {
            SchemaData data = new SchemaData(tables, tableRootPages, tableNextPages);
            mapper.writeValue(new File(schemaPath), data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void load() {
        File file = new File(schemaPath);
        if (file.exists()) {
            try {
                SchemaData data = mapper.readValue(file, SchemaData.class);
                this.tables = data.tables;
                this.tableRootPages = data.tableRootPages;
                this.tableNextPages = data.tableNextPages;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Map<String, Table> getTables() {
        return tables;
    }

    public int getGlobalNextPageId() {
        return tableNextPages.values().stream().mapToInt(Integer::intValue).max().orElse(0);
    }

    private static class SchemaData {
        public Map<String, Table> tables;
        public Map<String, Integer> tableRootPages;
        public Map<String, Integer> tableNextPages;

        public SchemaData() {}

        public SchemaData(Map<String, Table> tables, Map<String, Integer> tableRootPages, Map<String, Integer> tableNextPages) {
            this.tables = tables;
            this.tableRootPages = tableRootPages;
            this.tableNextPages = tableNextPages;
        }
    }
}
