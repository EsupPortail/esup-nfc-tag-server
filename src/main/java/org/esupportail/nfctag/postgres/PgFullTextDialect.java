package org.esupportail.nfctag.postgres;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.dialect.PostgreSQLDialect;

public class PgFullTextDialect extends PostgreSQLDialect {

    /* Column name of TSVECTOR field in PgSQL table */
    public static final String FTS_VECTOR_FIELD = "textsearchable_index_col";


    @Override
    public void initializeFunctionRegistry(FunctionContributions functionContributions) {
        super.initializeFunctionRegistry(functionContributions);
        var functionRegistry = functionContributions.getFunctionRegistry();
        functionRegistry.registerPattern(
                "fts",
                FTS_VECTOR_FIELD + " @@ to_tsquery('simple',?1)"
        );
        functionRegistry.registerPattern(
                "ts_rank",
                "ts_rank(" + FTS_VECTOR_FIELD  + ", to_tsquery('simple', ?1))"
        );
    }

}
