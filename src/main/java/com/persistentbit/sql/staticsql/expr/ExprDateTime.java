package com.persistentbit.sql.staticsql.expr;

import com.persistentbit.core.collections.PList;

import java.time.LocalDateTime;

/**
 * Created by petermuys on 4/10/16.
 */
public class ExprDateTime implements Expr<LocalDateTime>,ETypeDateTime {
    private final LocalDateTime   value;

    public ExprDateTime(LocalDateTime value) {
        this.value = value;
    }

    public LocalDateTime getValue() {
        return value;
    }


    @Override
    public String _toSql(ExprToSqlContext context) {
        return context.getDbType().asLiteralDateTime(value);
    }

    @Override
    public PList<Expr> _expand() {
        return PList.val(this);
    }
}
