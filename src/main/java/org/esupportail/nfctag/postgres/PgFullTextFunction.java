package org.esupportail.nfctag.postgres;

import java.util.List;

import org.hibernate.QueryException;
import org.hibernate.type.Type;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.BooleanType;

public class PgFullTextFunction implements SQLFunction {

	/* Column name of TSVECTOR field in PgSQL table */
    public static final String FTS_VECTOR_FIELD = "textsearchable_index_col";

    @Override
    public Type getReturnType(Type firstArgumentType, Mapping mapping) throws QueryException{
        return new BooleanType();
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
        return FTS_VECTOR_FIELD + " @@ to_tsquery('simple'," + searchString + ")";
    }
    
}
