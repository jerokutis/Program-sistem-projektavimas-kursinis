package lt.kompiuteriai.backend.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import lt.kompiuteriai.backend.model.Memory;
import lt.kompiuteriai.backend.service.MemoryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies/{companyId}/memories")
public class MemoryController {

    private final MemoryService memoryService;

    public MemoryController(MemoryService memoryService) {
        this.memoryService = memoryService;
    }

    public record CreateMemoryRequest(
            @NotBlank @Size(max = 100) String name,
            @NotNull @Positive @Digits(integer = 8, fraction = 2) BigDecimal price,
            @NotBlank @Size(max = 30) String memoryType,
            @Positive int capacityGb) {}

    public record MemoryResponse(
            Long id, String name, BigDecimal price, String memoryType, int capacityGb) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemoryResponse createMemory(
            @PathVariable("companyId") Long companyId,
            @Valid @RequestBody CreateMemoryRequest request) {
        Memory memory =
                memoryService.createMemory(
                        companyId,
                        request.name().strip(),
                        request.price(),
                        request.memoryType().strip().toUpperCase(Locale.ROOT),
                        request.capacityGb());

        return toResponse(memory);
    }

    @GetMapping
    public List<MemoryResponse> getMemories(@PathVariable("companyId") Long companyId) {
        return memoryService.getMemories(companyId).stream().map(this::toResponse).toList();
    }

    private MemoryResponse toResponse(Memory memory) {
        return new MemoryResponse(
                memory.getId(),
                memory.getName(),
                memory.getPrice(),
                memory.getMemoryType(),
                memory.getCapacityGb());
    }

    @GetMapping("/compatible")
    public List<MemoryResponse> getCompatibleMemories(
            @PathVariable("companyId") Long companyId,
            @RequestParam("motherboardId") Long motherboardId) {
        return memoryService.getCompatibleMemories(companyId, motherboardId).stream()
                .map(this::toResponse)
                .toList();
    }
}
