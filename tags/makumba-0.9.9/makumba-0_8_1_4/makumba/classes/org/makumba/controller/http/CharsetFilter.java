package org.makumba.controller.http;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.FilterChain;

import org.makumba.providers.TransactionProvider;

/**
 * This is a filter making sure the charset encoding is set (for now, only UTF-8 is supported).
 * 
 * @author Marius Andra
 * @author Manuel Gay
 * @version $Id$
 */
public class CharsetFilter implements Filter {

    /**
     * this is not used
     */
    private String encoding;

    public void init(FilterConfig config) throws ServletException {
        encoding = config.getInitParameter("requestEncoding");
        if (encoding == null)
            encoding = "UTF-8";
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        TransactionProvider tp = TransactionProvider.getInstance();

        if (tp.supportsUTF8()) {
            request.setCharacterEncoding("UTF-8");
            response.setContentType("text/html;charset=UTF-8");
        }

        chain.doFilter(request, response);
        if (tp.supportsUTF8()) {
            response.setContentType("text/html; charset=UTF-8");
            request.setCharacterEncoding("UTF8");
        }
    }

    public void destroy() {
    }
}
