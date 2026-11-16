package lt.kompiuteriai.backend.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Set;
import lt.kompiuteriai.backend.model.*;
import org.junit.jupiter.api.Test;

public class CompatibilityServiceTest {
    private CompatibilityService service = new CompatibilityService();

    @Test
    void shouldFitWhenGraphicsCardIsShorter() {
        GraphicsCard graphicsCard = new GraphicsCard("RTX 4060", new BigDecimal("200.00"), 300);
        ComputerCase computerCase = new ComputerCase("Gamemax T20", new BigDecimal("80.00"), 320);

        boolean result = service.isCompatible(graphicsCard, computerCase);

        assertTrue(result);
    }

    @Test
    void shouldFitWhenLengthsAreEqual() {
        GraphicsCard graphicsCard = new GraphicsCard("RTX 5090", new BigDecimal("200.00"), 320);
        ComputerCase computerCase =
                new ComputerCase("Gamemax HYPE-M", new BigDecimal("80.00"), 320);

        boolean result = service.isCompatible(graphicsCard, computerCase);

        assertTrue(result);
    }

    @Test
    void shouldNotFitWhenGraphicsCardIsLonger() {
        GraphicsCard graphicsCard = new GraphicsCard("GTX 1060", new BigDecimal("200.00"), 340);
        ComputerCase computerCase =
                new ComputerCase("Gamemac Aeris 330 M", new BigDecimal("80.00"), 330);

        boolean result = service.isCompatible(graphicsCard, computerCase);

        assertFalse(result);
    }

    @Test
    void shouldMatchWhenSocketsAreEqual() {
        Processor processor =
                new Processor("Testinis procesorius", new BigDecimal("200.00"), "AM5");
        Motherboard motherboard =
                new Motherboard("Testinė plokštė", new BigDecimal("150.00"), "AM5", "DDR5");

        assertTrue(service.isCompatible(processor, motherboard));
    }

    @Test
    void shouldNotMatchWhenSocketsAreDifferent() {
        Processor processor =
                new Processor("Testinis procesorius", new BigDecimal("200.00"), "AM5");
        Motherboard motherboard =
                new Motherboard("Testinė plokštė", new BigDecimal("150.00"), "LGA1700", "DDR5");

        assertFalse(service.isCompatible(processor, motherboard));
    }

    @Test
    void shouldMatchWhenSocketLetterCaseDiffers() {
        Processor processor =
                new Processor("Testinis procesorius", new BigDecimal("200.00"), "am5");
        Motherboard motherboard =
                new Motherboard("Testinė plokštė", new BigDecimal("150.00"), "AM5", "DDR5");

        assertTrue(service.isCompatible(processor, motherboard));
    }

    @Test
    void shouldMatchWhenMemoryTypesAreEqual() {
        Memory memory = new Memory("Testinė RAM", new BigDecimal("90.00"), "DDR5", 32);
        Motherboard motherboard =
                new Motherboard("Testinė plokštė", new BigDecimal("150.00"), "AM5", "DDR5");

        assertTrue(service.isCompatible(memory, motherboard));
    }

    @Test
    void shouldNotMatchWhenMemoryTypesAreDifferent() {
        Memory memory = new Memory("Testinė RAM", new BigDecimal("60.00"), "DDR4", 16);
        Motherboard motherboard =
                new Motherboard("Testinė plokštė", new BigDecimal("150.00"), "AM5", "DDR5");

        assertFalse(service.isCompatible(memory, motherboard));
    }

    @Test
    void shouldNotMatchWhenMotherboardMemoryTypeIsMissing() {
        Memory memory = new Memory("Testinė RAM", new BigDecimal("90.00"), "DDR5", 32);
        Motherboard motherboard =
                new Motherboard("Testinė plokštė", new BigDecimal("150.00"), "AM5", null);

        assertFalse(service.isCompatible(memory, motherboard));
    }

    @Test
    void shouldMatchWhenCoolerSupportsProcessorSocket() {
        Processor processor =
                new Processor("Testinis procesorius", new BigDecimal("200.00"), "AM5");
        Cooler cooler =
                new Cooler(
                        "Testinis aušintuvas",
                        new BigDecimal("39.99"),
                        Set.of("AM5", "LGA1700"),
                        155);

        assertTrue(service.isCompatible(processor, cooler));
    }

    @Test
    void shouldNotMatchWhenCoolerDoesNotSupportProcessorSocket() {
        Processor processor =
                new Processor("Testinis procesorius", new BigDecimal("200.00"), "AM5");
        Cooler cooler =
                new Cooler("Testinis aušintuvas", new BigDecimal("39.99"), Set.of("LGA1700"), 155);

        assertFalse(service.isCompatible(processor, cooler));
    }

    @Test
    void shouldNotMatchWhenProcessorSocketIsMissing() {
        Processor processor = new Processor("Testinis procesorius", new BigDecimal("200.00"), null);
        Cooler cooler =
                new Cooler("Testinis aušintuvas", new BigDecimal("39.99"), Set.of("AM5"), 155);

        assertFalse(service.isCompatible(processor, cooler));
    }

    @Test
    void shouldFitWhenCoolerIsShorterThanCaseLimit() {
        Cooler cooler =
                new Cooler("Testinis aušintuvas", new BigDecimal("39.99"), Set.of("AM5"), 155);
        ComputerCase computerCase =
                new ComputerCase("Testinis korpusas", new BigDecimal("80.00"), 320);
        computerCase.setMaxCoolerHeightMm(160);

        assertTrue(service.isCompatible(cooler, computerCase));
    }

    @Test
    void shouldFitWhenCoolerHeightEqualsCaseLimit() {
        Cooler cooler =
                new Cooler("Testinis aušintuvas", new BigDecimal("39.99"), Set.of("AM5"), 160);
        ComputerCase computerCase =
                new ComputerCase("Testinis korpusas", new BigDecimal("80.00"), 320);
        computerCase.setMaxCoolerHeightMm(160);

        assertTrue(service.isCompatible(cooler, computerCase));
    }

    @Test
    void shouldNotFitWhenCoolerIsTallerThanCaseLimit() {
        Cooler cooler =
                new Cooler("Testinis aušintuvas", new BigDecimal("39.99"), Set.of("AM5"), 165);
        ComputerCase computerCase =
                new ComputerCase("Testinis korpusas", new BigDecimal("80.00"), 320);
        computerCase.setMaxCoolerHeightMm(160);

        assertFalse(service.isCompatible(cooler, computerCase));
    }

    @Test
    void shouldNotFitWhenCaseCoolerHeightLimitIsMissing() {
        Cooler cooler =
                new Cooler("Testinis aušintuvas", new BigDecimal("39.99"), Set.of("AM5"), 155);
        ComputerCase computerCase =
                new ComputerCase("Testinis korpusas", new BigDecimal("80.00"), 320);

        assertFalse(service.isCompatible(cooler, computerCase));
    }
}
