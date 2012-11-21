/*
 * Created on Jun 20, 2012
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.makumba.commons;

import java.util.ArrayList;

import org.makumba.DataDefinition;
import org.makumba.commons.NameResolver.Resolvable;

public class TextList {
    ArrayList<Object> content = new ArrayList<Object>();

    StringBuffer lastBuffer;

    public TextList append(Object o) {

        if (o instanceof String) {
            // optimization: we want to have only one StringBuffer in between two non-Strings
            // if last time we had a string
            if (lastBuffer != null) {
                // we add to the previous buffer
                lastBuffer.append(o);
            } else {
                // otherwise we make a new buffer
                lastBuffer = new StringBuffer();
                // and add to it
                lastBuffer.append(o);
                // and add the buffer to the content
                content.add(lastBuffer);
            }
            return this;
        }
        // the string buffer stops here
        lastBuffer = null;
        if (o instanceof DataDefinition) {
            Resolvable r = new Resolvable();
            r.dd = (DataDefinition) o;
            o = r;
        }
        content.add(o);
        return this;
    }

    public TextList append(DataDefinition dd, String field) {
        Resolvable r = new Resolvable();
        r.dd = dd;
        r.field = field;
        return append(r);
    }

    public void clear() {
        content.clear();
    }

    private static NameResolver defaultNameResolver = new NameResolver();

    /** resolve the TextList and flatten it so no TextLists remain inside it, just Strings and ParamInfo */
    public TextList resolve(NameResolver nr) {
        TextList.ResolveState rs = new ResolveState();
        resolve(nr, rs);
        rs.close();
        return rs.ret;
    }

    static class ResolveState {
        TextList ret = new TextList();

        StringBuffer lastBuffer = new StringBuffer();

        void append(Object o, NameResolver nr) {
            if (o instanceof StringBuffer) {
                lastBuffer.append(o.toString());
            } else if (o instanceof Resolvable) {
                Resolvable rs = (Resolvable) o;
                lastBuffer.append(rs.resolve(nr));
            } else if (o instanceof ParamInfo) {
                close();
                ret.append(o);
            } else if (o instanceof TextList) {
                ((TextList) o).resolve(nr, this);
            }
        }

        public void close() {
            if (lastBuffer.length() > 0) {
                ret.append(lastBuffer);
                lastBuffer = new StringBuffer();
            }
        }
    }

    private void resolve(NameResolver nr, TextList.ResolveState rs) {

        for (Object o : content) {
            rs.append(o, nr);
        }
    }

    public String resolveParameters(ParamInfo.Writer wr) {
        StringBuffer ret = new StringBuffer();
        for (Object o : content) {
            if (o instanceof StringBuffer) {
                ret.append(o);
            } else if (o instanceof ParamInfo) {
                ParamInfo po = (ParamInfo) o;
                wr.write(po, ret);
            } else if (o instanceof TextList) {
                ret.append(((TextList) o).resolveParameters(wr));
            }
        }
        return ret.toString();
    }

    public String resolveAll() {
        return resolveParameters(new ParamInfo.Writer());
    }
}