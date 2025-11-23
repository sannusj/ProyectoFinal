package com.solea.web.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Ensures a non-null _csrf attribute is present in the model for all controllers.
 * If the real CsrfToken is not available, injects a harmless dummy token so
 * templates that access _csrf.parameterName won't throw during parsing.
 */
@ControllerAdvice
@Component
public class CsrfModelAdvice {

    @ModelAttribute("_csrf")
    public CsrfToken csrfToken(HttpServletRequest request) {
        Object token = request.getAttribute("_csrf");
        if (token instanceof CsrfToken) {
            return (CsrfToken) token;
        }

        // Provide a dummy CsrfToken implementation to avoid NPE in templates.
        return new CsrfToken() {
            @Override
            public String getHeaderName() { return "X-CSRF-TOKEN"; }
            @Override
            public String getParameterName() { return "_csrf"; }
            @Override
            public String getToken() { return ""; }
        };
    }
}

