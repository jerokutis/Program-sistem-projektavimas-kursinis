package lt.kompiuteriai.backend.security;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import lt.kompiuteriai.backend.controller.PowerSupplyController;
import lt.kompiuteriai.backend.model.PowerSupply;
import lt.kompiuteriai.backend.service.PowerSupplyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PowerSupplyController.class)
@Import({SecurityConfig.class, CompanyApiKeyService.class})
@TestPropertySource(
        properties = {
            "app.security.baltic-company-id=1",
            "app.security.baltic-api-key=test-baltic-key",
            "app.security.nordic-company-id=2",
            "app.security.nordic-api-key=test-nordic-key"
        })
public class CompanyApiSecurityTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private PowerSupplyService powerSupplyService;

    private static final String REQUEST_BODY =
            """
            {
                "name": "Testinis blokas",
                "price": 59.99,
                "wattage": 550
            }
            """;

    @Test
    void shouldAllowPublicCatalogRead() throws Exception {
        String path = "/api/companies/1/power-supplies";

        mockMvc.perform(get(path).servletPath(path)).andExpect(status().isOk());

        verify(powerSupplyService).getPowerSupplies(1L);
    }

    @Test
    void shouldReadOwnCatalog() throws Exception {
        String path = "/api/companies/1/power-supplies";
        PowerSupply supply = new PowerSupply("Baltic PSU", new BigDecimal("59.99"), 550);

        when(powerSupplyService.getPowerSupplies(1L)).thenReturn(List.of(supply));

        mockMvc.perform(get(path).servletPath(path).header("X-API-Key", "test-baltic-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Baltic PSU"));

        verify(powerSupplyService).getPowerSupplies(1L);
    }

    @Test
    void shouldRejectReadOfOtherCompanyCatalog() throws Exception {
        String path = "/api/companies/2/power-supplies";

        mockMvc.perform(get(path).servletPath(path).header("X-API-Key", "test-baltic-key"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(powerSupplyService);
    }

    @Test
    void shouldRejectRequestWithoutApiKey() throws Exception {
        String path = "/api/companies/1/power-supplies";

        mockMvc.perform(
                        post(path)
                                .servletPath(path)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(REQUEST_BODY))
                .andExpect(status().isUnauthorized())
                .andExpect(
                        jsonPath("$.message").value("Reikalingas galiojantis įmonės API raktas."));

        verifyNoInteractions(powerSupplyService);
    }

    @Test
    void shouldRejectInvalidApiKey() throws Exception {
        String path = "/api/companies/1/power-supplies";

        mockMvc.perform(
                        post(path)
                                .servletPath(path)
                                .header("X-API-Key", "wrong-key")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(REQUEST_BODY))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Netinkamas API raktas."));

        verifyNoInteractions(powerSupplyService);
    }

    @Test
    void shouldRejectAnotherCompanyAddress() throws Exception {
        String path = "/api/companies/2/power-supplies";

        mockMvc.perform(
                        post(path)
                                .servletPath(path)
                                .header("X-API-Key", "test-baltic-key")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(REQUEST_BODY))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Negalite veikti kitos įmonės vardu."));

        verifyNoInteractions(powerSupplyService);
    }

    @Test
    void shouldAllowOwnCompanyAddress() throws Exception {
        String path = "/api/companies/1/power-supplies";
        PowerSupply powerSupply = new PowerSupply("Testinis blokas", new BigDecimal("59.99"), 550);

        when(powerSupplyService.createPowerSupply(
                        1L, "Testinis blokas", new BigDecimal("59.99"), 550))
                .thenReturn(powerSupply);

        mockMvc.perform(
                        post(path)
                                .servletPath(path)
                                .header("X-API-Key", "test-baltic-key")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(REQUEST_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Testinis blokas"))
                .andExpect(jsonPath("$.wattage").value(550));

        verify(powerSupplyService)
                .createPowerSupply(1L, "Testinis blokas", new BigDecimal("59.99"), 550);
    }
}
