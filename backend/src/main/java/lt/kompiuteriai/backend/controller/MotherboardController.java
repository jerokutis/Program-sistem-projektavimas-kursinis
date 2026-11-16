package lt.kompiuteriai.backend.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import lt.kompiuteriai.backend.model.Motherboard;
import lt.kompiuteriai.backend.service.MotherboardService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies/{companyId}/motherboards")
public class MotherboardController {

    private final MotherboardService motherboardService;

    public MotherboardController(MotherboardService motherboardService) {
        this.motherboardService = motherboardService;
    }

    public record CreateMotherboardRequest(
            @NotBlank @Size(max = 100) String name,
            @NotNull @Positive @Digits(integer = 8, fraction = 2) BigDecimal price,
            @NotBlank @Size(max = 30) String socket,
            @NotBlank @Size(max = 30) String memoryType,
            @NotEmpty
                    Set<
                                    @NotNull
                                    @Pattern(
                                            regexp = "SATA_2_5|M2_2280_PCIE",
                                            message = "Leidžiama SATA_2_5 arba M2_2280_PCIE")
                                    String>
                            supportedStorageTypes) {}

    public record MotherboardResponse(
            Long id,
            String name,
            BigDecimal price,
            String socket,
            String memoryType,
            Set<String> supportedStorageTypes) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MotherboardResponse createMotherboard(
            @PathVariable("companyId") Long companyId,
            @Valid @RequestBody CreateMotherboardRequest request) {
        Set<String> supportedStorageTypes =
                request.supportedStorageTypes().stream()
                        .map(type -> type.strip().toUpperCase(Locale.ROOT))
                        .collect(Collectors.toSet());

        Motherboard motherboard =
                motherboardService.createMotherboard(
                        companyId,
                        request.name().strip(),
                        request.price(),
                        request.socket().strip().toUpperCase(Locale.ROOT),
                        request.memoryType().strip().toUpperCase(Locale.ROOT),
                        supportedStorageTypes);

        return toResponse(motherboard);
    }

    @GetMapping
    public List<MotherboardResponse> getMotherboards(@PathVariable("companyId") Long companyId) {
        return motherboardService.getMotherboards(companyId).stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/compatible")
    public List<MotherboardResponse> getCompatibleMotherboards(
            @PathVariable("companyId") Long companyId,
            @RequestParam(value = "processorId", required = false) Long processorId,
            @RequestParam(value = "memoryId", required = false) Long memoryId,
            @RequestParam(value = "storageId", required = false) Long storageId) {
        return motherboardService
                .getCompatibleMotherboards(companyId, processorId, memoryId, storageId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private MotherboardResponse toResponse(Motherboard motherboard) {
        return new MotherboardResponse(
                motherboard.getId(),
                motherboard.getName(),
                motherboard.getPrice(),
                motherboard.getSocket(),
                motherboard.getMemoryType(),
                Set.copyOf(motherboard.getSupportedStorageTypes()));
    }
}
