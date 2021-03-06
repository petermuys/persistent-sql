package com.persistentbit.sql.dbbuilder;

import com.persistentbit.core.OK;
import com.persistentbit.sql.staticsql.DbWork;

import java.sql.Connection;

/**
 * Class used to create, update or drop all tables
 * in a database.<br>
 *
 * @author petermuys
 * @since 31/10/16
 */
public interface DbBuilder{

	/**
	 * Execute all the database update methods not registered in the SchemaHistory table.<br>
	 * If there is a declared method in this class with the same name,
	 * then that method is executed with a {@link Connection} as argument.<br>
	 */
	DbWork<OK> buildOrUpdate();

	/**
	 * Drop all tables, views,... created by buildOrUpdate
	 *
	 * @return true if dropAll executed without errors
	 */
	DbWork<OK> dropAll();

	/**
	 * Check if there is at least 1  update done for this builder.<br>
	 *
	 * @return true if 1 update is done for this builder
	 */
	DbWork<Boolean> hasUpdatesThatAreDone();

	/**
	 * If there are any updates done: drop all and rebuild
	 *
	 * @return succes or failure
	 */
	default DbWork<OK> resetDb() {
		return DbWork.function().code(l -> (dbc, tm) -> {
			boolean hasUpdates = hasUpdatesThatAreDone().execute(dbc, tm).orElse(false);
			if(hasUpdates) {
				dropAll().execute(dbc, tm);
			}
			return buildOrUpdate().execute(dbc, tm);
		});

	}

}
