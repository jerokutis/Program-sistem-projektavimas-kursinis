package lt.kompiuteriai.backend.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import lt.kompiuteriai.backend.model.Cooler;
import lt.kompiuteriai.backend.service.CoolerService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies/{companyId}/coolers")
public class CoolerController {

    private final CoolerService coolerService;

    public CoolerController(CoolerService coolerService) {
        this.coolerService = coolerService;
    }

    public record CreateCoolerRequest(
            @NotBlank @Size(max = 100) String name,
            @NotNull @Positive @Digits(integer = 8, fraction = 2) BigDecimal price,
            @NotEmpty Set<@NotBlank @Size(max = 30) String> supportedSockets,
            @Positive int heightMm) {}

    public record CoolerResponse(
            Long id, String name, BigDecimal price, Set<String> supportedSockets, int heightMm) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CoolerResponse createCooler(
            @PathVariable("companyId") Long companyId,
            @Valid @RequestBody CreateCoolerRequest request) {
        Set<String> supportedSockets =
                request.supportedSockets().stream()
                        .map(socket -> socket.strip().toUpperCase(Locale.ROOT))
                        .collect(Collectors.toSet());

        Cooler cooler =
                coolerService.createCooler(
                        companyId,
                        request.name().strip(),
                        request.price(),
                        supportedSockets,
                        request.heightMm());

        return toResponse(cooler);
    }

    @GetMapping
    public List<CoolerResponse> getCoolers(@PathVariable("companyId") Long companyId) {
        return coolerService.getCoolers(companyId).stream().map(this::toResponse).toList();
    }

    @GetMapping("/compatible")
    public List<CoolerResponse> getCompatibleCoolers(
            @PathVariable("companyId") Long companyId,
            @RequestParam(value = "processorId", required = false) Long processorId,
            @RequestParam(value = "caseId", required = false) Long caseId) {
        return coolerService.getCompatibleCoolers(companyId, processorId, caseId).stream()
                .map(this::toResponse)
                .toList();
    }

    private CoolerResponse toResponse(Cooler cooler) {
        return new CoolerResponse(
                cooler.getId(),
                cooler.getName(),
                cooler.getPrice(),
                Set.copyOf(cooler.getSupportedSockets()),
                cooler.getHeightMm());
    }
}
