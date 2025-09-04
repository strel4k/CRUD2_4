package com.crudapp.filestorage.web.filter;

import com.crudapp.filestorage.util.JsonUtil;
import com.crudapp.filestorage.web.ApiError;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/*")
public class ErrorHandlingFilter implements Filter {
    @Override public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        try {
            chain.doFilter(req, resp);
        } catch (IllegalArgumentException e) {
            JsonUtil.writeJson((HttpServletResponse) resp, HttpServletResponse.SC_BAD_REQUEST, new ApiError(e.getMessage()));
        } catch (SecurityException e) {
            JsonUtil.writeJson((HttpServletResponse) resp, HttpServletResponse.SC_FORBIDDEN, new ApiError(e.getMessage()));
        } catch (Throwable t) {
            JsonUtil.writeJson((HttpServletResponse) resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new ApiError("Internal error"));
        }
    }
}