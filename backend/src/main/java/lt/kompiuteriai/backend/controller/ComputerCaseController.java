package lt.kompiuteriai.backend.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import lt.kompiuteriai.backend.model.ComputerCase;
import lt.kompiuteriai.backend.service.ComputerCaseService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies/{companyId}/cases")
public class ComputerCaseController {

    private final ComputerCaseService computerCaseService;

    public ComputerCaseController(ComputerCaseService computerCaseService) {
        this.computerCaseService = computerCaseService;
    }

    public record CreateComputerCaseRequest(
            @NotBlank @Size(max = 100) String name,
            @NotNull @Positive @Digits(integer = 8, fraction = 2) BigDecimal price,
            @Positive int maxGpuLengthMm,
            @NotNull @Positive Integer maxCoolerHeightMm) {}

    public record ComputerCaseResponse(
            Long id,
            String name,
            BigDecimal price,
            int maxGpuLengthMm,
            Integer maxCoolerHeightMm) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ComputerCaseResponse createComputerCase(
            @PathVariable("companyId") Long companyId,
            @Valid @RequestBody CreateComputerCaseRequest request) {
        ComputerCase computerCase =
                computerCaseService.createComputerCase(
                        companyId,
                        request.name().strip(),
                        request.price(),
                        request.maxGpuLengthMm(),
                        request.maxCoolerHeightMm());

        return toResponse(computerCase);
    }

    @GetMapping
    public List<ComputerCaseResponse> getComputerCases(@PathVariable("companyId") Long companyId) {
        return computerCaseService.getComputerCases(companyId).stream()
                .map(this::toResponse)
                .toList();
    }

    private ComputerCaseResponse toResponse(ComputerCase computerCase) {
        return new ComputerCaseResponse(
                computerCase.getId(),
                computerCase.getName(),
                computerCase.getPrice(),
                computerCase.getMaxGpuLengthMm(),
                computerCase.getMaxCoolerHeightMm());
    }

    @GetMapping("/compatible")
    public List<ComputerCaseResponse> getCompatibleCases(
            @PathVariable("companyId") Long companyId,
            @RequestParam(value = "graphicsCardId", required = false) Long graphicsCardId,
            @RequestParam(value = "coolerId", required = false) Long coolerId) {
        return computerCaseService.getCompatibleCases(companyId, graphicsCardId, coolerId).stream()
                .map(this::toResponse)
                .toList();
    }
}
