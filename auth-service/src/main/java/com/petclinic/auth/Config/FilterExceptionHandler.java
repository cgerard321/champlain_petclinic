package com.petclinic.auth.Config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.auth.Exceptions.HTTPErrorMessage;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * Created by IntelliJ IDEA.
 *
 * User: @Fube
 * Date: 2021-10-16
 * Ticket: feat(AUTH-CPC-460)
 */
@Component
public class FilterExceptionHandler extends OncePerRequestFilter {



    private final ObjectMapper objectMapper;
    private final Map<Class<?>, Function<Throwable, HTTPErrorMessage>> handlers;

    public FilterExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        handlers = new HashMap<>();
    }

    // NOTE: if this gets to like 10 else...ifs, change to a simple Map<? extends Class, Function>
    // I am not implementing it now because it would be too complex
    // If you do not understand this note you are NOT ready to touch this code
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (Exception ex) {

            // Very low level & cursed implementation, but I could not find a better solution
            // Do NOT touch this code unless you understand Java servlets and the middleware design pattern
            Throwable cause = ex.getCause();
            while(cause != null) {
                final Function<Throwable, HTTPErrorMessage> handler = handlers.get(cause.getClass());
                if(handler != null) {
                    final HTTPErrorMessage handled = handler.apply(cause);
                    if(handled != null) {
                        response.setStatus(BAD_REQUEST.value());
                        response.getWriter().write(
                                objectMapper.writeValueAsString(handled));
                        return;
                    }
                }
                cause = cause.getCause();
            }

            // Do not handle
            throw ex;
        }
    }

    public <T> FilterExceptionHandler registerHandler(Class<T> key, Function<? super T, HTTPErrorMessage> handler) {
        handlers.put(key, o -> handler.apply(key.cast(o)));
        return this;
    }
}
