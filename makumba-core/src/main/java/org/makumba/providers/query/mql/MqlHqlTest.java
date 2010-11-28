package org.makumba.providers.query.mql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.makumba.commons.ClassResource;
import org.makumba.commons.NullNameResolver;
import org.makumba.providers.QueryAnalysisProvider;
import org.makumba.providers.QueryProvider;

public class MqlHqlTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader((InputStream) ClassResource.get(
                "org/makumba/providers/query/mql/mqlHqlCorpus.txt").getContent()));
            String query = null;
            int line = 0;
            QueryAnalysisProvider qap = QueryProvider.getQueryAnalzyer("oql");

            while ((query = rd.readLine()) != null) {
                line++;
                if (query.trim().startsWith("#")) {
                    continue;
                }
                System.out.println(line);
                System.out.println(query);

                try {
                    MqlQueryAnalysis analyzer = (MqlQueryAnalysis) qap.getQueryAnalysis(query);

                    analyzer.prepareForHQL();

                    MqlParameterTransformer paramTransformer = MqlParameterTransformer.getSQLQueryGenerator(analyzer,
                        new HashMap<String, Object>() {
                            @Override
                            public Object get(Object name) {
                                return "";
                            }
                        });
                    String hql = paramTransformer.getSQLQuery(new MqlHqlGenerator(), new NullNameResolver());

                    System.out.println(hql);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
