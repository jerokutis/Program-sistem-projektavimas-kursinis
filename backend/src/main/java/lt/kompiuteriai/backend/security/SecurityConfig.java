package lt.kompiuteriai.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, CompanyApiKeyService companyApiKeyService) throws Exception {
        CompanyApiKeyFilter apiKeyFilter = new CompanyApiKeyFilter(companyApiKeyService);

        http.csrf(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.logout(AbstractHttpConfigurer::disable);
        http.requestCache(AbstractHttpConfigurer::disable);

        http.sessionManagement(
                session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.exceptionHandling(
                errors -> {
                    errors.authenticationEntryPoint(
                            (request, response, exception) -> {
                                response.setStatus(401);
                                response.setContentType("application/json");
                                response.setCharacterEncoding("UTF-8");
                                response.getWriter()
                                        .write(
                                                "{\"message\":\"Reikalingas galiojantis įmonės API raktas.\"}");
                            });

                    errors.accessDeniedHandler(
                            (request, response, exception) -> {
                                response.setStatus(403);
                                response.setContentType("application/json");
                                response.setCharacterEncoding("UTF-8");
                                response.getWriter()
                                        .write(
                                                "{\"message\":\"Neturite teisės atlikti šio veiksmo.\"}");
                            });
                });

        String companyPath = "/api/companies/{companyId:[0-9]+}";
        String componentPath =
                companyPath
                        + "/{component:processors|motherboards|memories|graphics-cards|storages|power-supplies|coolers|cases}";

        http.authorizeHttpRequests(
                authorize -> {
                    authorize.requestMatchers(HttpMethod.GET, "/api/companies").permitAll();

                    authorize
                            .requestMatchers(
                                    HttpMethod.GET,
                                    componentPath,
                                    componentPath + "/compatible",
                                    companyPath + "/motherboards/compatible-by-memory")
                            .permitAll();

                    authorize
                            .requestMatchers(HttpMethod.POST, companyPath + "/builds/evaluate")
                            .permitAll();

                    authorize.requestMatchers(HttpMethod.POST, componentPath).hasRole("COMPANY");

                    String supplierCardsPath = companyPath + "/management/graphics-cards";

                    authorize.requestMatchers(HttpMethod.GET, supplierCardsPath).hasRole("COMPANY");

                    authorize
                            .requestMatchers(
                                    HttpMethod.PUT, supplierCardsPath + "/{cardId:[0-9]+}/publish")
                            .hasRole("COMPANY");

                    authorize
                            .requestMatchers(
                                    HttpMethod.DELETE, supplierCardsPath + "/{cardId:[0-9]+}")
                            .hasRole("COMPANY");

                    authorize.anyRequest().denyAll();
                });

        http.addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
