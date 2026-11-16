package lt.kompiuteriai.backend.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lt.kompiuteriai.backend.model.*;
import lt.kompiuteriai.backend.repository.CompanyRepository;
import lt.kompiuteriai.backend.repository.GraphicsCardRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GraphicsCardService {

    private final ComputerCaseService computerCaseService;
    private final CompatibilityService compatibilityService;
    private final GraphicsCardRepository graphicsCardRepository;
    private final CompanyRepository companyRepository;
    private final ProcessorService processorService;
    private final PowerSupplyService powerSupplyService;

    public GraphicsCardService(
            GraphicsCardRepository graphicsCardRepository,
            CompanyRepository companyRepository,
            ComputerCaseService computerCaseService,
            CompatibilityService compatibilityService,
            ProcessorService processorService,
            PowerSupplyService powerSupplyService) {
        this.graphicsCardRepository = graphicsCardRepository;
        this.companyRepository = companyRepository;
        this.computerCaseService = computerCaseService;
        this.compatibilityService = compatibilityService;
        this.processorService = processorService;
        this.powerSupplyService = powerSupplyService;
    }

    @Transactional
    public GraphicsCard createGraphicsCard(
            Long companyId,
            String name,
            BigDecimal price,
            int lengthMm,
            Integer powerConsumptionW) {
        Company company = findCompany(companyId);

        GraphicsCard graphicsCard = new GraphicsCard(name, price, lengthMm);
        graphicsCard.setPowerConsumptionW(powerConsumptionW);
        graphicsCard.setCompany(company);
        graphicsCard.setPublished(false);

        return graphicsCardRepository.save(graphicsCard);
    }

    @Transactional(readOnly = true)
    public List<GraphicsCard> getGraphicsCards(Long companyId) {
        findCompany(companyId);

        return graphicsCardRepository.findByCompany_Id(companyId);
    }

    private Company findCompany(Long companyId) {
        return companyRepository
                .findById(companyId)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imone nerasta"));
    }

    @Transactional(readOnly = true)
    public List<GraphicsCard> getCompatibleGraphicsCards(
            Long companyId, Long caseId, Long processorId, Long powerSupplyId) {
        List<GraphicsCard> cards = getGraphicsCards(companyId);

        if (caseId != null) {
            ComputerCase computerCase = computerCaseService.getComputerCase(companyId, caseId);

            cards =
                    cards.stream()
                            .filter(card -> compatibilityService.isCompatible(card, computerCase))
                            .toList();
        }

        Processor processor = null;
        PowerSupply powerSupply = null;

        if (processorId != null) {
            processor = processorService.getProcessor(companyId, processorId);
        }

        if (powerSupplyId != null) {
            powerSupply = powerSupplyService.getPowerSupply(companyId, powerSupplyId);
        }

        if (processor != null && powerSupply != null) {
            List<GraphicsCard> compatibleCards = new ArrayList<>();

            for (GraphicsCard card : cards) {
                if (compatibilityService.isCompatible(processor, card, powerSupply)) {
                    compatibleCards.add(card);
                }
            }

            return compatibleCards;
        }

        return cards;
    }

    @Transactional(readOnly = true)
    public GraphicsCard getGraphicsCard(Long companyId, Long graphicsCardId) {
        return graphicsCardRepository
                .findByIdAndCompany_Id(graphicsCardId, companyId)
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Vaizdo plokštė šios įmonės kataloge nerasta"));
    }

    @Transactional(readOnly = true)
    public List<GraphicsCard> getSupplierGraphicsCards(Long companyId) {
        findCompany(companyId);
        return graphicsCardRepository.findByCompany_IdOrderByIdAsc(companyId);
    }

    @Transactional
    public GraphicsCard publishGraphicsCard(Long companyId, Long cardId) {
        GraphicsCard card = findSupplierGraphicsCard(companyId, cardId);
        card.setPublished(true);
        return card;
    }

    @Transactional
    public void deleteGraphicsCard(Long companyId, Long cardId) {
        GraphicsCard card = findSupplierGraphicsCard(companyId, cardId);
        graphicsCardRepository.delete(card);
    }

    private GraphicsCard findSupplierGraphicsCard(Long companyId, Long cardId) {
        return graphicsCardRepository
                .findByCompany_IdAndId(companyId, cardId)
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Vaizdo plokštė šios įmonės kataloge nerasta"));
    }
}
