package com.persistentbit.sql.statement;

import com.persistentbit.core.Immutable;
import com.persistentbit.core.Tuple2;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.sql.connect.SQLRunner;
import com.persistentbit.sql.objectmappers.ReadableRow;

import java.sql.PreparedStatement;
import java.util.Optional;
import java.util.function.Function;

/**
 * Created by petermuys on 16/07/16.
 */
@Immutable
public class EJoinStats<L, R, T> {
    private final Function<Tuple2<L, R>, T> resultConverter;
    private final EJoinable<L> left;
    private final EJoinable<R> right;
    private final String joinSQL;
    private final String joinType;


    static public <L, R> EJoinStats<L, R, Tuple2<L, R>> joinTuple(String joinType, EJoinable<L> left, EJoinable<R> right, String joinSQL) {
        return new EJoinStats<L, R, Tuple2<L, R>>(joinType, left, right, joinSQL, t -> t);
    }
    static public <L, R, T > EJoinStats<L, R, T> join(String joinType, EJoinable<L> left, EJoinable<R> right, String joinSQL,Function<Tuple2<L,R>,T> mapper) {
        return new EJoinStats<L, R, T>(joinType, left, right, joinSQL, mapper);
    }

    private EJoinStats(String joinType, EJoinable<L> left, EJoinable<R> right, String joinSQL, Function<Tuple2<L, R>, T> resultConverter) {
        this.joinType = joinType;
        this.resultConverter = resultConverter;
        this.left = left;
        this.right = right;
        this.joinSQL = joinSQL;
    }

    public EJoinable<T> asJoinable() {
        return new EJoinable<T>() {
            @Override
            public String getName() {
                return left.getName();
            }

            @Override
            public String getSelectPart() {
                return left.getSelectPart() + ", " + right.getSelectPart();
            }

            @Override
            public String getTableName() {
                return left.getTableName();
            }

            @Override
            public String getOwnJoins() {
                return joinSQL;
            }

            @Override
            public T mapRow(Record row) {
                Tuple2<L, R> t = Tuple2.of(left.mapRow(row), right.mapRow(row));
                return resultConverter.apply(t);
            }

            @Override
            public SQLRunner getRunner() {
                return left.getRunner();
            }

            @Override
            public EStatementPreparer getStatementPreparer() {
                return left.getStatementPreparer();
            }
        };
    }


    public class SelectBuilder implements SqlArguments<SelectBuilder>, ReadableRow {
        private PMap<String, Object> args = PMap.empty();
        private String sqlRest = "";

        @Override
        public SelectBuilder arg(String name, Object value) {
            args = args.put(name, value);
            return this;
        }

        @Override
        public <T> T read(Class<T>cls, String name) {
            return (T)args.find(a -> a._1.equalsIgnoreCase(name)).map(a -> a._2).orElse(null);
        }


        public SelectBuilder sqlRest(String sql) {
            sqlRest = sql;
            return this;
        }

        public Optional<T> getOne() {
            return left.getRunner().run(c -> {
                return visit(v -> v.headOpt());
            });
        }

        public PList<T> getList() {
            return visit(s -> s.plist());
        }



        private <X> X visit(Function<PStream<T>,X> visitor) {
            return left.getRunner().run(c -> {
                String sql = "SELECT " + left.getSelectPart() + "," + right.getSelectPart() + " FROM :" + left.getTableName() + ".as." + left.getName()
                        + " " + joinType + " :" + right.getTableName() + ".as." + right.getName() + " ON " + joinSQL + right.getOwnJoins() + sqlRest;

                try (PreparedStatement stat = left.getStatementPreparer().prepare(c, sql, SelectBuilder.this)) {

                    ResultSetRecordStream rs = new ResultSetRecordStream(stat.executeQuery());


                    PStream<T> trs = rs.map(r -> {
                        L leftValue = left.mapRow(r);
                        R rightValue = right.mapRow(r);
                        return Tuple2.of(leftValue,rightValue);
                    }).map(resultConverter);

                    return visitor.apply(trs);
                }
            });

        }


    }

    public SelectBuilder select() {
        return new SelectBuilder();
    }

}
