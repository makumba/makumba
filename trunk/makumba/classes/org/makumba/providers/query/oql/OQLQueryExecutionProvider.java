package org.makumba.providers.query.oql;

import java.util.Vector;

import org.makumba.Attributes;
import org.makumba.LogicException;
import org.makumba.Transaction;
import org.makumba.providers.QueryExecutionProvider;
import org.makumba.util.MultipleKey;
import org.makumba.util.NamedResourceFactory;
import org.makumba.util.NamedResources;

public class OQLQueryExecutionProvider extends QueryExecutionProvider {

    private Transaction tr;
    
    @Override
    public Vector execute(String query, Attributes args, int offset, int limit) throws LogicException
    {    
        return ((MultipleAttributeParametrizer) queries.getResource(query)).execute(tr, args, offset, limit);
    }
    
    @Override
    public void close() {
        tr.close();
    }
    
    @Override
    public void init(String dataSource) {
        tr= org.makumba.MakumbaSystem.getConnectionTo(dataSource);
        
    }
    
    
    

NamedResources queries = new NamedResources("Composed queries", new NamedResourceFactory() {

    private static final long serialVersionUID = 1L;

    protected Object makeResource(Object nm, Object hashName) {
      /*  MultipleKey mk = (MultipleKey) nm;
        String[] sections = new String[5];
        for (int i = 0; i < 5; i++) {
            if (mk.elementAt(i) instanceof String) // not "null key memeber"
                sections[i] = (String) mk.elementAt(i);
        }*/
        return new MultipleAttributeParametrizer((String) nm);
    }
});

    
    

}
