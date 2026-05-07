package com.latchdb.query.ast;

import java.util.List;

public class SelectNode extends ASTNode {
    public List<String> columns;
    public String table;
    public Object where;
    public List<String> orderBy;
    public List<Boolean> ascending;
    public Integer limit;
}
