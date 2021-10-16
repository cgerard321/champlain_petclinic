package com.petclinic.auth.Config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.auth.Exceptions.HTTPErrorMessage;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

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
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        response.setStatus(handled.getStatusCode());
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
