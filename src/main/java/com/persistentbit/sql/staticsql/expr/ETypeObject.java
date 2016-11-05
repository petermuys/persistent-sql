package com.persistentbit.sql.staticsql.expr;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.tuples.Tuple2;

import java.util.Optional;
import java.util.function.Function;

/**
 * @author Peter Muys
 * @since 28/09/2016
 */
public interface ETypeObject<T> extends ETypePropertyParent<T>{


	PList<Tuple2<String, Expr<?>>> _all();

	default <R> EMapper<T, R> map(Function<T, R> mapper) {
		return new EMapper<T, R>(this, mapper);
	}

	default String getFullTableName(String schema) {
		return (schema == null ? "" : schema + ".") + _getTableName();
	}

	String _getTableName();

	default EValTable<T> val(T value) {
		return new EValTable<>(this, value);
	}

	PList<Expr<?>> _asExprValues(T value);

	@Override
	default String _toSql(ExprToSqlContext context) {

		return _expand().map(e -> e._toSql(context)).toString(", ");
	}

	@Override
	default String _asParentName(ExprToSqlContext context, String propertyName) {
		if(getParent().isPresent()) {
			return getParent().get()._asParentName(context, propertyName);
		}
		return context.uniqueInstanceName(this, _getTableName()) + "." + propertyName;
	}

	Optional<ETypePropertyParent> getParent();
}
