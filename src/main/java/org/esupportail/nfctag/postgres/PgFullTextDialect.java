package org.esupportail.nfctag.postgres;

import org.hibernate.dialect.PostgreSQL9Dialect;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.DoubleType;
import org.hibernate.type.ObjectType;

public class PgFullTextDialect extends PostgreSQL9Dialect{

    public PgFullTextDialect() {
        registerFunction("fts", new PgFullTextFunction());
        registerFunction("ts_rank", new PgFullTextRankFunction());
        //registerFunction("ts_rank", new StandardSQLFunction("ts_rank", DoubleType.INSTANCE));
        //registerFunction("to_tsquery", new StandardSQLFunction("to_tsquery", ObjectType.INSTANCE));
    }
    
}
