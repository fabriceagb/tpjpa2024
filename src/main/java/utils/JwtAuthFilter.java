package utils;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;
import java.security.Principal;


@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtAuthFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) {

        String path = requestContext.getUriInfo().getPath();
        String method = requestContext.getMethod();

        if (path.contains("login") || path.contains("register")) {
            return;
        }

        if (method.equals("GET")) {
            return;
        }

        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            return;
        }

        String token = authorizationHeader.substring("Bearer".length()).trim();

        try {
            String email = JwtUtil.getEmailFromToken(token);
            String role = JwtUtil.getRoleFromToken(token);
            requestContext.setSecurityContext(new SecurityContext() {
                @Override
                public Principal getUserPrincipal() {
                    return () -> email;
                }

                @Override
                public boolean isUserInRole(String expectedRole) {
                    return expectedRole.equalsIgnoreCase(role);
                }

                @Override
                public boolean isSecure() { return requestContext.getUriInfo().getAbsolutePath().toString().startsWith("https"); }

                @Override
                public String getAuthenticationScheme() { return "Bearer"; }
            });

        } catch (Exception e) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }
}
