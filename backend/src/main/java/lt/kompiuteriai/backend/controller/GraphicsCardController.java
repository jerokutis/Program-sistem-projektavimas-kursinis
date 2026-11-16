package lt.kompiuteriai.backend.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import lt.kompiuteriai.backend.model.GraphicsCard;
import lt.kompiuteriai.backend.service.GraphicsCardService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies/{companyId}/graphics-cards")
public class GraphicsCardController {

    private final GraphicsCardService graphicsCardService;

    public GraphicsCardController(GraphicsCardService graphicsCardService) {
        this.graphicsCardService = graphicsCardService;
    }

    public record CreateGraphicsCardRequest(
            @NotBlank @Size(max = 100) String name,
            @NotNull @Positive @Digits(integer = 8, fraction = 2) BigDecimal price,
            @Positive int lengthMm,
            @NotNull @Positive Integer powerConsumptionW) {}

    public record GraphicsCardResponse(
            Long id, String name, BigDecimal price, int lengthMm, Integer powerConsumptionW) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GraphicsCardResponse createGraphicsCard(
            @PathVariable("companyId") Long companyId,
            @Valid @RequestBody CreateGraphicsCardRequest request) {
        GraphicsCard card =
                graphicsCardService.createGraphicsCard(
                        companyId,
                        request.name().strip(),
                        request.price(),
                        request.lengthMm(),
                        request.powerConsumptionW());

        return toResponse(card);
    }

    @GetMapping
    public List<GraphicsCardResponse> getGraphicsCard(@PathVariable("companyId") Long companyId) {
        return graphicsCardService.getGraphicsCards(companyId).stream()
                .map(this::toResponse)
                .toList();
    }

    private GraphicsCardResponse toResponse(GraphicsCard card) {
        return new GraphicsCardResponse(
                card.getId(),
                card.getName(),
                card.getPrice(),
                card.getLengthMm(),
                card.getPowerConsumptionW());
    }

    @GetMapping("/compatible")
    public List<GraphicsCardResponse> getCompatibleGraphicsCards(
            @PathVariable("companyId") Long companyId,
            @RequestParam(value = "caseId", required = false) Long caseId,
            @RequestParam(value = "processorId", required = false) Long processorId,
            @RequestParam(value = "powerSupplyId", required = false) Long powerSupplyId) {
        return graphicsCardService
                .getCompatibleGraphicsCards(companyId, caseId, processorId, powerSupplyId)
                .stream()
                .map(this::toResponse)
                .toList();
    }
}
