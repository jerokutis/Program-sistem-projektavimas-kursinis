package lt.kompiuteriai.backend.controller;

import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lt.kompiuteriai.backend.model.ComputerCase;
import lt.kompiuteriai.backend.model.GraphicsCard;
import lt.kompiuteriai.backend.service.CompatibilityService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CompatibilityController {

    private CompatibilityService compatibilityService;

    public CompatibilityController(CompatibilityService compatibilityService) {
        this.compatibilityService = compatibilityService;
    }

    @GetMapping("/api/compatibility")
    public boolean checkCompatibility(
            @RequestParam("gpuLength") @Positive int gpuLength,
            @RequestParam("caseLimit") @Positive int caseLimit) {
        GraphicsCard graphicsCard =
                new GraphicsCard("Pasirinkta plokste", new BigDecimal("200.00"), gpuLength);
        ComputerCase computerCase =
                new ComputerCase("Pasirinktas korpusas", new BigDecimal("80.00"), caseLimit);

        return compatibilityService.isCompatible(graphicsCard, computerCase);
    }
    ;
}
