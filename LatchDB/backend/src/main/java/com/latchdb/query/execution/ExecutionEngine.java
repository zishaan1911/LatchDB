package com.latchdb.query.execution;

import com.latchdb.index.BPlusTree;

import com.latchdb.schema.*;
import com.latchdb.storage.BufferManager;
import com.latchdb.storage.Record;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.Function;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ExecutionEngine {
    private final SchemaManager schemaManager;
    private final BufferManager bufferManager;
    private int globalNextPageId = 0;

    public ExecutionEngine(SchemaManager schemaManager, BufferManager bufferManager) {
        this.schemaManager = schemaManager;
        this.bufferManager = bufferManager;
        this.globalNextPageId = schemaManager.getGlobalNextPageId();
    }

    public List<Record> execute(String sql) throws Exception {
        Statement statement = CCJSqlParserUtil.parse(sql);
        if (statement instanceof Select) {
            return handleSelect((Select) statement);
        } else if (statement instanceof CreateTable) {
            handleCreate((CreateTable) statement);
            return null;
        } else if (statement instanceof Insert) {
            handleInsert((Insert) statement);
            return null;
        } else if (statement instanceof Update) {
            handleUpdate((Update) statement);
            return null;
        } else if (statement instanceof Delete) {
            handleDelete((Delete) statement);
            return null;
        }
        throw new UnsupportedOperationException("Unsupported statement type");
    }

    private void handleDelete(Delete delete) throws Exception {
        String tableName = delete.getTable().getName();
        Table table = schemaManager.getTable(tableName);
        BPlusTree tree = new BPlusTree(2, bufferManager, table, schemaManager.getRootPageId(tableName), globalNextPageId);

        Iterator iterator = new IndexScanIterator(tree, Integer.MIN_VALUE, Integer.MAX_VALUE);
        if (delete.getWhere() != null) {
            iterator = new FilterIterator(iterator, record -> evaluate(delete.getWhere(), record));
        }

        iterator.open();
        Record r;
        List<Integer> pksToDelete = new ArrayList<>();
        while ((r = iterator.next()) != null) {
            pksToDelete.add((Integer) r.get(table.getPrimaryKeyColumn().getName()));
        }
        iterator.close();

        for (Integer pk : pksToDelete) {
            tree.delete(pk);
        }
    }

    private void handleUpdate(Update update) throws Exception {
        String tableName = update.getTable().getName();
        Table table = schemaManager.getTable(tableName);
        BPlusTree tree = new BPlusTree(2, bufferManager, table, schemaManager.getRootPageId(tableName), globalNextPageId);

        Iterator iterator = new IndexScanIterator(tree, Integer.MIN_VALUE, Integer.MAX_VALUE);
        if (update.getWhere() != null) {
            iterator = new FilterIterator(iterator, record -> evaluate(update.getWhere(), record));
        }

        iterator.open();
        Record r;
        List<Record> toUpdate = new ArrayList<>();
        while ((r = iterator.next()) != null) {
            toUpdate.add(r);
        }
        iterator.close();

        for (Record record : toUpdate) {
            for (UpdateSet set : update.getUpdateSets()) {
                String colName = set.getColumns().get(0).getColumnName();
                Expression expr = set.getExpressions().get(0);
                Object val = getValue(expr, record);
                record.put(colName, val);
            }
            int pk = (Integer) record.get(table.getPrimaryKeyColumn().getName());
            tree.update(pk, record);
        }
    }

    private void handleCreate(CreateTable createTable) throws Exception {
        String tableName = createTable.getTable().getName();
        List<com.latchdb.schema.Column> columns = createTable.getColumnDefinitions().stream()
                .map(cd -> {
                    String typeStr = cd.getColDataType().getDataType().toUpperCase();
                    DataType type = typeStr.equals("INT") ? DataType.INTEGER : DataType.valueOf(typeStr);
                    boolean isPk = cd.getColumnSpecs() != null && cd.getColumnSpecs().stream().anyMatch(s -> s.toUpperCase().contains("PRIMARY"));
                    return new com.latchdb.schema.Column(cd.getColumnName(), type, isPk);
                })
                .collect(Collectors.toList());
        Table table = new Table(tableName, columns);
        int rootPageId = globalNextPageId;
        schemaManager.createTable(table, rootPageId);
        // Initialize the tree file/pages
        BPlusTree tree = new BPlusTree(2, bufferManager, table, rootPageId);
        globalNextPageId = tree.getNextPageId();
        schemaManager.updateTableMetadata(tableName, tree.getRootPageId(), tree.getNextPageId());
    }

    private void handleInsert(Insert insert) throws Exception {
        String tableName = insert.getTable().getName();
        Table table = schemaManager.getTable(tableName);
        BPlusTree tree = new BPlusTree(2, bufferManager, table, schemaManager.getRootPageId(tableName), schemaManager.getNextPageId(tableName));
        
        Record record = new Record();
        List<Expression> values = ((ExpressionList) insert.getItemsList()).getExpressions();
        for (int i = 0; i < table.getColumns().size(); i++) {
            com.latchdb.schema.Column col = table.getColumns().get(i);
            String valStr = values.get(i).toString().replace("'", "");
            Object val;
            if (col.getType() == DataType.INTEGER) {
                val = Integer.parseInt(valStr);
            } else if (col.getType() == DataType.DOUBLE) {
                val = Double.parseDouble(valStr);
            } else {
                val = valStr;
            }
            record.put(col.getName(), val);
        }
        int pk = (Integer) record.get(table.getPrimaryKeyColumn().getName());
        tree.insert(pk, record);
        
        // Update metadata after potential splits
        schemaManager.updateTableMetadata(tableName, tree.getRootPageId(), tree.getNextPageId());
        if (tree.getNextPageId() > globalNextPageId) {
            globalNextPageId = tree.getNextPageId();
        }
    }

    private List<Record> handleSelect(Select select) throws Exception {
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        String tableName = plainSelect.getFromItem().toString();
        Table table = schemaManager.getTable(tableName);
        BPlusTree tree = new BPlusTree(2, bufferManager, table, schemaManager.getRootPageId(tableName), globalNextPageId);
        
        Iterator iterator = new IndexScanIterator(tree, Integer.MIN_VALUE, Integer.MAX_VALUE);

        // WHERE clause
        if (plainSelect.getWhere() != null) {
            Expression where = plainSelect.getWhere();
            iterator = new FilterIterator(iterator, record -> evaluate(where, record));
        }

        // ORDER BY clause
        if (plainSelect.getOrderByElements() != null) {
            List<String> sortColumns = new ArrayList<>();
            List<Boolean> ascending = new ArrayList<>();
            for (OrderByElement obe : plainSelect.getOrderByElements()) {
                sortColumns.add(obe.getExpression().toString());
                ascending.add(obe.isAsc());
            }
            iterator = new SortIterator(iterator, sortColumns, ascending);
        }

        // LIMIT clause
        if (plainSelect.getLimit() != null) {
            Limit limit = plainSelect.getLimit();
            if (limit.getRowCount() instanceof LongValue) {
                iterator = new LimitIterator(iterator, (int) ((LongValue) limit.getRowCount()).getValue());
            }
        }

        // SELECT items (Aggregation or Projection)
        List<SelectItem> selectItems = plainSelect.getSelectItems();
        if (selectItems.size() == 1 && selectItems.get(0) instanceof SelectExpressionItem) {
            SelectExpressionItem item = (SelectExpressionItem) selectItems.get(0);
            if (item.getExpression() instanceof Function) {
                Function func = (Function) item.getExpression();
                String colName;
                if (func.getParameters() != null) {
                    colName = func.getParameters().getExpressions().stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(","));
                } else {
                    colName = "*";
                }
                AggregateIterator.AggregateType type = AggregateIterator.AggregateType.valueOf(func.getName().toUpperCase());
                iterator = new AggregateIterator(iterator, colName, type);
            } else if (!(item.getExpression() instanceof net.sf.jsqlparser.statement.select.AllColumns)) {
                List<String> columns = selectItems.stream()
                        .map(si -> ((SelectExpressionItem) si).getExpression().toString())
                        .collect(Collectors.toList());
                iterator = new ProjectionIterator(iterator, columns);
            }
        } else if (!(selectItems.get(0) instanceof AllColumns)) {
            List<String> columns = selectItems.stream()
                    .map(si -> ((SelectExpressionItem) si).getExpression().toString())
                    .collect(Collectors.toList());
            iterator = new ProjectionIterator(iterator, columns);
        }
        
        List<Record> results = new ArrayList<>();
        iterator.open();
        Record r;
        while ((r = iterator.next()) != null) {
            results.add(r);
        }
        iterator.close();
        return results;
    }

    private boolean evaluate(Expression expr, Record record) {
        if (expr instanceof AndExpression) {
            AndExpression and = (AndExpression) expr;
            return evaluate(and.getLeftExpression(), record) && evaluate(and.getRightExpression(), record);
        } else if (expr instanceof OrExpression) {
            OrExpression or = (OrExpression) expr;
            return evaluate(or.getLeftExpression(), record) || evaluate(or.getRightExpression(), record);
        } else if (expr instanceof EqualsTo) {
            EqualsTo eq = (EqualsTo) expr;
            Object left = getValue(eq.getLeftExpression(), record);
            Object right = getValue(eq.getRightExpression(), record);
            if (left instanceof Number && right instanceof Number) {
                return ((Number) left).doubleValue() == ((Number) right).doubleValue();
            }
            return left != null && left.equals(right);
        } else if (expr instanceof GreaterThan) {
            GreaterThan gt = (GreaterThan) expr;
            Object left = getValue(gt.getLeftExpression(), record);
            Object right = getValue(gt.getRightExpression(), record);
            if (left instanceof Number && right instanceof Number) {
                return ((Number) left).doubleValue() > ((Number) right).doubleValue();
            }
            if (left instanceof Comparable && right != null && left.getClass().equals(right.getClass())) {
                return ((Comparable) left).compareTo(right) > 0;
            }
        } else if (expr instanceof MinorThan) {
            MinorThan mt = (MinorThan) expr;
            Object left = getValue(mt.getLeftExpression(), record);
            Object right = getValue(mt.getRightExpression(), record);
            if (left instanceof Number && right instanceof Number) {
                return ((Number) left).doubleValue() < ((Number) right).doubleValue();
            }
            if (left instanceof Comparable && right != null && left.getClass().equals(right.getClass())) {
                return ((Comparable) left).compareTo(right) < 0;
            }
        }
        return true;
    }

    private Object getValue(Expression expr, Record record) {
        if (expr instanceof net.sf.jsqlparser.schema.Column) {
            return record.get(((net.sf.jsqlparser.schema.Column) expr).getColumnName());
        } else if (expr instanceof LongValue) {
            return (int) ((LongValue) expr).getValue();
        } else if (expr instanceof DoubleValue) {
            return ((DoubleValue) expr).getValue();
        } else if (expr instanceof StringValue) {
            return ((StringValue) expr).getValue();
        }
        return null;
    }
}
