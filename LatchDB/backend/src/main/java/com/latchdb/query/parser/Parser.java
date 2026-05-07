package com.latchdb.query.parser;

import com.latchdb.query.ast.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.PlainSelect;

public class Parser {
    public ASTNode parse(String sql) throws Exception {
        Statement statement = CCJSqlParserUtil.parse(sql);
        if (statement instanceof Select) {
            SelectNode node = new SelectNode();
            PlainSelect plainSelect = (PlainSelect) ((Select) statement).getSelectBody();
            node.table = plainSelect.getFromItem().toString();
            // ... conversion logic ...
            return node;
        }
        // ... other statements ...
        return null;
    }
}
