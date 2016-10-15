package com.persistentbit.sql.staticsql.expr;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.utils.NotYet;
import com.persistentbit.sql.PersistSqlException;
import com.persistentbit.sql.staticsql.ExprRowReaderCache;
import com.persistentbit.sql.staticsql.RowReader;
import com.persistentbit.sql.staticsql.codegen.DbJavaGenException;
import com.persistentbit.sql.staticsql.ENumberGroup;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.Function;

/**
 * @author Peter Muys
 * @since 28/09/2016
 */
public interface Expr<S>{
    static  <N extends Number> ExprConstNumber<N> val(N number){
        return new ExprConstNumber<>(number.getClass(),number);
    }
    static  ExprConstNumber<Short> val(Short number){
        return new ExprConstNumber<>(Short.class,number);
    }

    static  ExprConstNumber<Integer> val(Integer number){
        return new ExprConstNumber<>(Integer.class,number);
    }

    static  ExprConstNumber<Long> val(Long number){
        return new ExprConstNumber<>(Long.class,number);
    }

    static  ExprConstNumber<Float> val(Float number){
        return new ExprConstNumber<>(Float.class,number);
    }

    static  ExprConstNumber<Double> val(Double number){
        return new ExprConstNumber<>(Double.class,number);
    }



    static ETypeString  val(String value){
        return new ExprConstString(value);
    }

    static ETypeBoolean val(Boolean value) { return new ExprBoolean(value);}

    static ETypeDate    val(LocalDate date){
        return new ExprDate(date);
    }

    static ETypeDateTime val(LocalDateTime dateTime){
        return new ExprDateTime(dateTime);
    }


    default ETypeBoolean    in(ETypeList<S> in){
        return new ExprIn<>(this,in);
    }

    static ETypeBoolean exists(ETypeList<?> list){
        return new ExprExists(list);
    }


    static EBooleanGroup    group(ETypeBoolean b){
        return new EBooleanGroup(b);
    }
    static <N extends Number> ENumberGroup<N> group(ETypeNumber<N> v){
        return new ENumberGroup<>(v);
    }
    static EStringGroup group(ETypeString v){
        return new EStringGroup(v);
    }

    static <T extends Enum<T>> ETypeEnum<T> val(T value){
        if(value == null){
            throw new PersistSqlException("Need to know the class of the null enum: use Expr.valNullEnum(cls) instead.");
        }
        return new ExprEnum<T>(value,value.getClass());
    }


    static <T extends Enum<T>> ETypeEnum<T> valNullEnum(Class<T> value){
        return new ExprEnum<>(null,value);
    }

    default <R> EMapper<S,R> map(Function<S,R> mapper){
        return new EMapper<>(this,mapper);
    }


    default <T2> ETuple2<S,T2> mergeWith(Expr<T2> expr2){
        return new ETuple2<>(this,expr2);
    }
    default <T2,T3> ETuple3<S,T2,T3> mergeWith(Expr<T2> v2, Expr<T3> v3){
        return new ETuple3<>(this,v2,v3);
    }
    default <T2,T3,T4> ETuple4<S,T2,T3,T4> mergeWith(Expr<T2> v2, Expr<T3> v3, Expr<T4> v4){
        return new ETuple4<>(this,v2,v3,v4);
    }
    default <T2,T3,T4,T5> ETuple5<S,T2,T3,T4,T5> mergeWith(
            Expr<T2> v2, Expr<T3> v3, Expr<T4> v4, Expr<T5> v5){
        return new ETuple5<>(this,v2,v3,v4,v5);
    }
    default <T2,T3,T4,T5,T6> ETuple6<S,T2,T3,T4,T5,T6> mergeWith(
            Expr<T2> v2, Expr<T3> v3, Expr<T4> v4, Expr<T5> v5, Expr<T6> v6){
        return new ETuple6<>(this,v2,v3,v4,v5,v6);
    }
    default <T2,T3,T4,T5,T6,T7> ETuple7<S,T2,T3,T4,T5,T6,T7> mergeWith(
            Expr<T2> v2, Expr<T3> v3, Expr<T4> v4, Expr<T5> v5, Expr<T6> v6, Expr<T7> v7){
        return new ETuple7<>(this,v2,v3,v4,v5,v6,v7);
    }


    S read(RowReader _rowReader, ExprRowReaderCache _cache);

    /**
     * Return the name of the expression without the tablename.<br>
     * Mainly used by the insert sql generator to get a list of all column names to insert.<br>
     * @param context   The context
     * @return  The full property name without the
     */
    default String _fullColumnName(ExprToSqlContext context){
        throw new NotYet("Not Yet supported on " + getClass().getName());
    }

    String _toSql(ExprToSqlContext context);
    PList<Expr> _expand();
}
