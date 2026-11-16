package lt.kompiuteriai.backend.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import lt.kompiuteriai.backend.model.PowerSupply;
import lt.kompiuteriai.backend.service.PowerSupplyService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies/{companyId}/power-supplies")
public class PowerSupplyController {

    private final PowerSupplyService powerSupplyService;

    public PowerSupplyController(PowerSupplyService powerSupplyService) {
        this.powerSupplyService = powerSupplyService;
    }

    public record CreatePowerSupplyRequest(
            @NotBlank @Size(max = 100) String name,
            @NotNull @Positive @Digits(integer = 8, fraction = 2) BigDecimal price,
            @Positive int wattage) {}

    public record PowerSupplyResponse(Long id, String name, BigDecimal price, int wattage) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PowerSupplyResponse createPowerSupply(
            @PathVariable("companyId") Long companyId,
            @Valid @RequestBody CreatePowerSupplyRequest request) {
        PowerSupply powerSupply =
                powerSupplyService.createPowerSupply(
                        companyId, request.name().strip(), request.price(), request.wattage());

        return toResponse(powerSupply);
    }

    @GetMapping
    public List<PowerSupplyResponse> getPowerSupplies(@PathVariable("companyId") Long companyId) {
        return powerSupplyService.getPowerSupplies(companyId).stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/compatible")
    public List<PowerSupplyResponse> getCompatiblePowerSupplies(
            @PathVariable("companyId") Long companyId,
            @RequestParam("processorId") Long processorId,
            @RequestParam("graphicsCardId") Long graphicsCardId) {
        return powerSupplyService
                .getCompatiblePowerSupplies(companyId, processorId, graphicsCardId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private PowerSupplyResponse toResponse(PowerSupply powerSupply) {
        return new PowerSupplyResponse(
                powerSupply.getId(),
                powerSupply.getName(),
                powerSupply.getPrice(),
                powerSupply.getWattage());
    }
}
