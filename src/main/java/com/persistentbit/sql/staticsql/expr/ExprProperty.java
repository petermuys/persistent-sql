package com.persistentbit.sql.staticsql.expr;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.tuples.Tuple2;
import com.persistentbit.sql.staticsql.ExprRowReaderCache;
import com.persistentbit.sql.staticsql.RowReader;
import com.persistentbit.sql.staticsql.expr.Expr;

import java.util.Optional;

/**
 * @author Peter Muys
 * @since 28/09/2016
 */
public class ExprProperty<T> implements ETypePropertyParent<T> {
    private ETypePropertyParent    parent;
    private String  propertyName;
    private Class<T>  valueClass;
    private String columnName;

    public ExprProperty(Class<T> valueClass,ETypePropertyParent parent, String propertyName, String columnName) {
        this.parent = parent;
        this.propertyName = propertyName;
        this.valueClass = valueClass;
        this.columnName = columnName;
    }

    @Override
    public String toString() {
        return parent.toString() + "." + propertyName;
    }


    public Optional<ETypePropertyParent> getParent() {
        return Optional.of(parent);
    }

    public String getColumnName() {
        return columnName;
    }

    public String getPropertyName() {
        return propertyName;
    }

    @Override
    public String getInstanceName() {
        return columnName;
    }

    public Class<T> getValueClass() {
        return valueClass;
    }

    @Override
    public T read(RowReader _rowReader, ExprRowReaderCache _cache) {
        return _rowReader.readNext(valueClass);
    }

    @Override
    public String _toSql(ExprToSqlContext context) {
        return parent._asParentName(context,getColumnName());

    }

    @Override
    public String _fullColumnName(ExprToSqlContext context) {
        String res = parent._fullColumnName(context);
        if( res.isEmpty() == false){
            res = res + "_";
        }
        return res + getColumnName();
    }

    @Override
    public String _asParentName(ExprToSqlContext context, String propertyName) {

        return getParent().get()._asParentName(context,getColumnName() + "_" + propertyName);
    }

    @Override
    public PList<Expr> _expand() {

        Expr subProp = getProperty(parent,propertyName);
        return PList.val(subProp);
    }



    private Expr getProperty(ETypePropertyParent parent, String propertyName){
        if(parent instanceof ETypeObject){
            // We have a table column
            ETypeObject obj = (ETypeObject)parent;
            PList<Tuple2<String,Expr>> props = obj._all();
            return props.find(tp ->tp._1.equals(propertyName)).get()._2;
        } else if(parent instanceof ExprProperty){
            //We have a embedded object
            ExprProperty<?> ep = (ExprProperty)parent;
            ETypePropertyParent epParent = ep.getParent().get();
            return getProperty(epParent,ep.getPropertyName());
        } else {
            throw new RuntimeException("Don't know what to do with:" + parent + " and propertyName " + propertyName);
        }
    }
}
