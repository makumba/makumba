package org.makumba.controller.http;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.FilterChain;

public class CharsetFilter implements Filter {

/**
    this is not used
**/
	private String encoding;
	 
//    static FilterConfig conf;
    public void init(FilterConfig config) throws ServletException
    {
    	encoding = config.getInitParameter("requestEncoding");
     if( encoding==null ) encoding="UTF-8";
    }
    /*public void init(FilterConfig c) { conf=c; }*/

    public void doFilter(ServletRequest request, ServletResponse
	        response, FilterChain chain) throws IOException, ServletException {
		
		if(org.makumba.db.Database.supportsUTF8() == true)
        {
		    request.setCharacterEncoding("UTF-8");
		    response.setContentType("text/html;charset=UTF-8");
        }

        chain.doFilter(request, response);
		if(org.makumba.db.Database.supportsUTF8() == true)
        {
	        response.setContentType("text/html; charset=UTF-8");
	        request.setCharacterEncoding("UTF8");	
        }
    }
    
    public void destroy(){}
} 

