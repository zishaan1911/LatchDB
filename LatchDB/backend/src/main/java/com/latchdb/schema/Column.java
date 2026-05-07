package com.latchdb.schema;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Column {
    private final String name;
    private final DataType type;
    private final boolean isPrimaryKey;

    @JsonCreator
    public Column(@JsonProperty("name") String name, 
                  @JsonProperty("type") DataType type, 
                  @JsonProperty("primaryKey") boolean isPrimaryKey) {
        this.name = name;
        this.type = type;
        this.isPrimaryKey = isPrimaryKey;
    }

    public String getName() {
        return name;
    }

    public DataType getType() {
        return type;
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }
}
