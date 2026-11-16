package lt.kompiuteriai.backend.controller;

import java.math.BigDecimal;
import java.util.List;
import lt.kompiuteriai.backend.model.GraphicsCard;
import lt.kompiuteriai.backend.service.GraphicsCardService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies/{companyId}/management/graphics-cards")
public class SupplierGraphicsCardController {

    private final GraphicsCardService graphicsCardService;

    public SupplierGraphicsCardController(GraphicsCardService graphicsCardService) {
        this.graphicsCardService = graphicsCardService;
    }

    public record SupplierGraphicsCardResponse(
            Long id,
            String name,
            BigDecimal price,
            int lengthMm,
            Integer powerConsumptionW,
            boolean published) {}

    @GetMapping
    public List<SupplierGraphicsCardResponse> getGraphicsCards(
            @PathVariable("companyId") Long companyId) {
        return graphicsCardService.getSupplierGraphicsCards(companyId).stream()
                .map(this::toResponse)
                .toList();
    }

    @PutMapping("/{cardId}/publish")
    public SupplierGraphicsCardResponse publishGraphicsCard(
            @PathVariable("companyId") Long companyId, @PathVariable("cardId") Long cardId) {
        GraphicsCard card = graphicsCardService.publishGraphicsCard(companyId, cardId);
        return toResponse(card);
    }

    @DeleteMapping("/{cardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGraphicsCard(
            @PathVariable("companyId") Long companyId, @PathVariable("cardId") Long cardId) {
        graphicsCardService.deleteGraphicsCard(companyId, cardId);
    }

    private SupplierGraphicsCardResponse toResponse(GraphicsCard card) {
        return new SupplierGraphicsCardResponse(
                card.getId(),
                card.getName(),
                card.getPrice(),
                card.getLengthMm(),
                card.getPowerConsumptionW(),
                card.isPublished());
    }
}
