package com.latchdb.storage;

import com.latchdb.schema.Column;
import com.latchdb.schema.DataType;
import com.latchdb.schema.Table;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Record {
    private final Map<String, Object> values;

    public Record() {
        this.values = new HashMap<>();
    }

    public void put(String column, Object value) {
        values.put(column, value);
    }

    public Object get(String column) {
        return values.get(column);
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public byte[] serialize(Table table) {
        ByteBuffer buffer = ByteBuffer.allocate(Page.PAGE_SIZE); // Max record size is page size for now
        for (Column col : table.getColumns()) {
            Object val = values.get(col.getName());
            switch (col.getType()) {
                case INTEGER:
                    buffer.putInt((Integer) val);
                    break;
                case DOUBLE:
                    buffer.putDouble((Double) val);
                    break;
                case STRING:
                    byte[] strBytes = ((String) val).getBytes(StandardCharsets.UTF_8);
                    buffer.putInt(strBytes.length);
                    buffer.put(strBytes);
                    break;
            }
        }
        byte[] result = new byte[buffer.position()];
        buffer.flip();
        buffer.get(result);
        return result;
    }

    public static Record deserialize(byte[] data, Table table) {
        Record record = new Record();
        ByteBuffer buffer = ByteBuffer.wrap(data);
        for (Column col : table.getColumns()) {
            switch (col.getType()) {
                case INTEGER:
                    record.put(col.getName(), buffer.getInt());
                    break;
                case DOUBLE:
                    record.put(col.getName(), buffer.getDouble());
                    break;
                case STRING:
                    int length = buffer.getInt();
                    byte[] strBytes = new byte[length];
                    buffer.get(strBytes);
                    record.put(col.getName(), new String(strBytes, StandardCharsets.UTF_8));
                    break;
            }
        }
        return record;
    }
}
