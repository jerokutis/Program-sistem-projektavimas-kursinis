package lt.kompiuteriai.backend.service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import lt.kompiuteriai.backend.model.Company;
import lt.kompiuteriai.backend.model.GraphicsCard;
import lt.kompiuteriai.backend.repository.CompanyRepository;
import lt.kompiuteriai.backend.repository.GraphicsCardRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
@Transactional
public class CompanyIsolationTest {

    @Autowired private CompanyRepository companyRepository;

    @Autowired private GraphicsCardRepository graphicsCardRepository;

    @Autowired private GraphicsCardService graphicsCardService;

    @Test
    void shouldNotReturnGraphicsCardFromAnotherCompany() {
        Company firstCompany = companyRepository.save(new Company("Izoliavimo testo įmonė A"));
        Company secondCompany = companyRepository.save(new Company("Izoliavimo testo įmonė B"));

        GraphicsCard card =
                new GraphicsCard("Antros įmonės plokštė", new BigDecimal("219.99"), 280);
        card.setCompany(secondCompany);
        GraphicsCard savedCard = graphicsCardRepository.saveAndFlush(card);

        GraphicsCard foundCard =
                graphicsCardService.getGraphicsCard(secondCompany.getId(), savedCard.getId());

        assertEquals(savedCard.getId(), foundCard.getId());

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                graphicsCardService.getGraphicsCard(
                                        firstCompany.getId(), savedCard.getId()));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Vaizdo plokštė šios įmonės kataloge nerasta", exception.getReason());

        assertTrue(graphicsCardService.getGraphicsCards(firstCompany.getId()).isEmpty());
    }

    @Test
    void shouldKeepDraftPrivateUntilOwnerPublishesIt() {
        Company owner = companyRepository.save(new Company("Juodraščio savininkė"));
        Company other = companyRepository.save(new Company("Kita įmonė"));

        GraphicsCard draft =
                graphicsCardService.createGraphicsCard(
                        owner.getId(), "Testinė RTX 4070", new BigDecimal("499.00"), 244, 220);

        Long cardId = draft.getId();

        assertFalse(draft.isPublished());
        assertTrue(graphicsCardService.getGraphicsCards(owner.getId()).isEmpty());
        assertEquals(
                cardId,
                graphicsCardService.getSupplierGraphicsCards(owner.getId()).getFirst().getId());
        assertTrue(graphicsCardService.getSupplierGraphicsCards(other.getId()).isEmpty());

        ResponseStatusException hiddenDraft =
                assertThrows(
                        ResponseStatusException.class,
                        () -> graphicsCardService.getGraphicsCard(owner.getId(), cardId));
        assertEquals(HttpStatus.NOT_FOUND, hiddenDraft.getStatusCode());

        ResponseStatusException foreignPublish =
                assertThrows(
                        ResponseStatusException.class,
                        () -> graphicsCardService.publishGraphicsCard(other.getId(), cardId));
        assertEquals(HttpStatus.NOT_FOUND, foreignPublish.getStatusCode());

        ResponseStatusException foreignDelete =
                assertThrows(
                        ResponseStatusException.class,
                        () -> graphicsCardService.deleteGraphicsCard(other.getId(), cardId));
        assertEquals(HttpStatus.NOT_FOUND, foreignDelete.getStatusCode());

        graphicsCardService.publishGraphicsCard(owner.getId(), cardId);

        assertEquals(cardId, graphicsCardService.getGraphicsCard(owner.getId(), cardId).getId());

        graphicsCardService.deleteGraphicsCard(owner.getId(), cardId);

        assertFalse(graphicsCardRepository.existsById(cardId));
    }
}
