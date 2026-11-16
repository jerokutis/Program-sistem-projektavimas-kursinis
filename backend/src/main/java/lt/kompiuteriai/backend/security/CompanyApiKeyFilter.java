package lt.kompiuteriai.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class CompanyApiKeyFilter extends OncePerRequestFilter {

    private static final Pattern COMPANY_PATH =
            Pattern.compile("^/api/companies/([0-9]+)(?:/.*)?$");

    private final CompanyApiKeyService companyApiKeyService;

    public CompanyApiKeyFilter(CompanyApiKeyService companyApiKeyService) {
        this.companyApiKeyService = companyApiKeyService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String apiKey = request.getHeader("X-API-Key");

        if (apiKey == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<Long> companyId = companyApiKeyService.findCompanyId(apiKey);

        if (companyId.isEmpty()) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Netinkamas API raktas.");
            return;
        }

        Matcher matcher = COMPANY_PATH.matcher(request.getServletPath());

        if (matcher.matches()) {
            Long requestedCompanyId;

            try {
                requestedCompanyId = Long.valueOf(matcher.group(1));
            } catch (NumberFormatException exception) {
                writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Netinkamas įmonės ID.");
                return;
            }

            if (!companyId.get().equals(requestedCompanyId)) {
                writeError(
                        response,
                        HttpServletResponse.SC_FORBIDDEN,
                        "Negalite veikti kitos įmonės vardu.");
                return;
            }
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        companyId.get(), null, List.of(new SimpleGrantedAuthority("ROLE_COMPANY")));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        try {
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private void writeError(HttpServletResponse response, int status, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
