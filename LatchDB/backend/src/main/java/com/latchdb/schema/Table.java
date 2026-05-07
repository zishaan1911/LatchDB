package com.latchdb.schema;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Table {
    private final String name;
    private final List<Column> columns;
    private final Column primaryKeyColumn;

    @JsonCreator
    public Table(@JsonProperty("name") String name, 
                 @JsonProperty("columns") List<Column> columns) {
        this.name = name;
        this.columns = columns;
        this.primaryKeyColumn = columns.stream()
                .filter(Column::isPrimaryKey)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Table must have a primary key"));
    }

    public String getName() {
        return name;
    }

    public List<Column> getColumns() {
        return columns;
    }

    @JsonIgnore
    public Column getPrimaryKeyColumn() {
        return primaryKeyColumn;
    }
}
