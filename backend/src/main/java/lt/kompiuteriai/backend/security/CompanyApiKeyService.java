package lt.kompiuteriai.backend.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CompanyApiKeyService {

    private final Long balticCompanyId;
    private final String balticApiKey;
    private final Long nordicCompanyId;
    private final String nordicApiKey;

    public CompanyApiKeyService(
            @Value("${app.security.baltic-company-id}") Long balticCompanyId,
            @Value("${app.security.baltic-api-key}") String balticApiKey,
            @Value("${app.security.nordic-company-id}") Long nordicCompanyId,
            @Value("${app.security.nordic-api-key}") String nordicApiKey) {
        if (balticApiKey.isBlank() || nordicApiKey.isBlank()) {
            throw new IllegalArgumentException("Įmonių API raktai negali būti tušti.");
        }

        if (balticApiKey.equals(nordicApiKey)) {
            throw new IllegalArgumentException("Įmonių API raktai turi skirtis.");
        }

        this.balticCompanyId = balticCompanyId;
        this.balticApiKey = balticApiKey;
        this.nordicCompanyId = nordicCompanyId;
        this.nordicApiKey = nordicApiKey;
    }

    public Optional<Long> findCompanyId(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return Optional.empty();
        }

        if (keysMatch(apiKey, balticApiKey)) {
            return Optional.of(balticCompanyId);
        }

        if (keysMatch(apiKey, nordicApiKey)) {
            return Optional.of(nordicCompanyId);
        }

        return Optional.empty();
    }

    private boolean keysMatch(String providedKey, String expectedKey) {
        return MessageDigest.isEqual(
                providedKey.getBytes(StandardCharsets.UTF_8),
                expectedKey.getBytes(StandardCharsets.UTF_8));
    }
}
