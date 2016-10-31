package com.persistentbit.sql;

import com.persistentbit.sql.statement.SqlLoader;
import com.persistentbit.sql.transactions.TransactionRunnerPerThread;
import org.junit.After;
import org.junit.Before;

import java.util.logging.Logger;

/**
 * @author Peter Muys
 * @since 13/07/16
 */
public abstract class AbstractTestWithTransactions{
    protected Logger log = Logger.getLogger(this.getClass().getName());
    protected InMemConnectionProvider    dbConnector;
    protected TransactionRunnerPerThread trans;
    protected TestDbBuilderImpl          builder;
    protected SqlLoader                  loader;

    @Before
    public void setupTransactions() {
        dbConnector = new InMemConnectionProvider();
        trans = new TransactionRunnerPerThread(dbConnector);
        builder = new TestDbBuilderImpl(trans);
        loader = new SqlLoader("/db/Tests.sql");
        if(builder.hasUpdatesThatAreDone()) {
            builder.dropAll();
        }
        builder.buildOrUpdate();
        assert builder.javaUpdaterCalled;
    }
    @After
    public void closeTransactions() {
        if(builder.hasUpdatesThatAreDone()) {
            builder.dropAll();
        }
        trans = null;
        dbConnector.close();
        dbConnector = null;
    }
}
