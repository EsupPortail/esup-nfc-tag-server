package org.esupportail.nfctag.postgres;

import org.hibernate.QueryException;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.DoubleType;
import org.hibernate.type.Type;

import java.util.List;

public class PgFullTextRankFunction implements SQLFunction {

	/* Column name of TSVECTOR field in PgSQL table */
    public static final String FTS_VECTOR_FIELD = "textsearchable_index_col";

    @Override
    public Type getReturnType(Type firstArgumentType, Mapping mapping) throws QueryException{
        return new DoubleType();
    }
    

    @Override
    public boolean hasArguments() {
        return true;
    }

    @Override
    public boolean hasParenthesesIfNoArguments() {
        return false;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public String render(Type type, List args, SessionFactoryImplementor factory) throws QueryException {
        String searchString = (String) args.get(0);
        return "ts_rank(" + FTS_VECTOR_FIELD  + ", to_tsquery('simple'," + searchString + "))";
    }
    
}
