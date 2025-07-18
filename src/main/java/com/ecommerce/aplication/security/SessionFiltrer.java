package com.ecommerce.aplication.security;

import com.ecommerce.aplication.services.ServiceUsers;
import com.ecommerce.model.users.TypeRole;
import com.ecommerce.model.users.Users;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


public class SessionFiltrer extends OncePerRequestFilter  {
    private final ServiceUsers userService;

    public SessionFiltrer(ServiceUsers userService) {
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        var context = SecurityContextHolder.getContext();

        if (context.getAuthentication() == null) {
            var email = (String) request.getSession().getAttribute("USER_EMAIL");
            if (email != null) {
                var userDetails = userService.loadUserByUsername(email);
                var authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                context.setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
