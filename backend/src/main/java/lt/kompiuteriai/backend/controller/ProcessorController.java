package lt.kompiuteriai.backend.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import lt.kompiuteriai.backend.model.Processor;
import lt.kompiuteriai.backend.service.ProcessorService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies/{companyId}/processors")
public class ProcessorController {

    private final ProcessorService processorService;

    public ProcessorController(ProcessorService processorService) {
        this.processorService = processorService;
    }

    public record CreateProcessorRequest(
            @NotBlank @Size(max = 100) String name,
            @NotNull @Positive @Digits(integer = 8, fraction = 2) BigDecimal price,
            @NotBlank @Size(max = 30) String socket,
            @NotNull @Positive Integer powerConsumptionW) {}

    public record ProcessorResponse(
            Long id, String name, BigDecimal price, String socket, Integer powerConsumptionW) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProcessorResponse createProcessor(
            @PathVariable("companyId") Long companyId,
            @Valid @RequestBody CreateProcessorRequest request) {
        Processor processor =
                processorService.createProcessor(
                        companyId,
                        request.name().strip(),
                        request.price(),
                        request.socket().strip().toUpperCase(Locale.ROOT),
                        request.powerConsumptionW());

        return toResponse(processor);
    }

    @GetMapping
    public List<ProcessorResponse> getProcessors(@PathVariable("companyId") Long companyId) {
        return processorService.getProcessors(companyId).stream().map(this::toResponse).toList();
    }

    @GetMapping("/compatible")
    public List<ProcessorResponse> getCompatibleProcessors(
            @PathVariable("companyId") Long companyId,
            @RequestParam(value = "motherboardId", required = false) Long motherboardId,
            @RequestParam(value = "coolerId", required = false) Long coolerId,
            @RequestParam(value = "graphicsCardId", required = false) Long graphicsCardId,
            @RequestParam(value = "powerSupplyId", required = false) Long powerSupplyId) {
        return processorService
                .getCompatibleProcessors(
                        companyId, motherboardId, coolerId, graphicsCardId, powerSupplyId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ProcessorResponse toResponse(Processor processor) {
        return new ProcessorResponse(
                processor.getId(),
                processor.getName(),
                processor.getPrice(),
                processor.getSocket(),
                processor.getPowerConsumptionW());
    }
}
