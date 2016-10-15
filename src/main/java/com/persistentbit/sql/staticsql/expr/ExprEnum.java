package com.persistentbit.sql.staticsql.expr;

import com.persistentbit.core.collections.PList;

/**
 * Created by petermuys on 5/10/16.
 */
public class ExprEnum<T extends Enum<T>> implements ETypeEnum<T> {
    private T value;
    private Class<T> enumClass;

    public ExprEnum(T value,Class<? extends Enum> enumClass) {
        this.value = value;
        this.enumClass = (Class<T>)enumClass;
    }


    public T getValue() {
        return value;
    }

    @Override
    public Class<T> _getEnumClass() {
        return enumClass;
    }
    @Override
    public String _toSql(ExprToSqlContext context) {
        return context.getDbType().asLiteralString(value.name());
    }

    @Override
    public PList<Expr> _expand() {
        return PList.val(this);
    }
}
