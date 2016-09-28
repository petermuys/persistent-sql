package com.persistentbit.sql.staticsql;

/**
 * Created by petermuys on 28/09/16.
 */
public class ExprNumberCast<F extends Number,T extends Number> implements ETypeNumber<T>{
    private ETypeNumber<F> from;
    private Class<? extends Number> clsTo;

    public ExprNumberCast(ETypeNumber<F> from, Class<? extends Number> clsTo) {
        this.from = from;
        this.clsTo = clsTo;
    }

    @Override
    public String toString() {
        return "((" + clsTo.getSimpleName() + ")" + from + ")";
    }
}
