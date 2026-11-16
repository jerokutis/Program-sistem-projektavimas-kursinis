package lt.kompiuteriai.backend.service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import lt.kompiuteriai.backend.model.*;
import org.junit.jupiter.api.Test;

public class BuildEvaluationServiceTest {

    private final BuildEvaluationService service =
            new BuildEvaluationService(new CompatibilityService());

    @Test
    void shouldCalculateTotalPrice() {
        ComputerBuild build = createBuild();

        BigDecimal totalPrice = service.calculateTotalPrice(build);

        assertEquals(0, new BigDecimal("900.00").compareTo(totalPrice));
    }

    @Test
    void shouldFitWhenTotalIsBelowBudget() {
        ComputerBuild build = createBuild();

        assertTrue(service.isWithinBudget(build, new BigDecimal("1000.00")));
        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(
                        service.calculateBudgetExcess(build, new BigDecimal("1000.00"))));
    }

    @Test
    void shouldFitWhenTotalEqualsBudget() {
        ComputerBuild build = createBuild();

        assertTrue(service.isWithinBudget(build, new BigDecimal("900.00")));
        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(
                        service.calculateBudgetExcess(build, new BigDecimal("900.00"))));
    }

    @Test
    void shouldCalculateExcessWhenTotalExceedsBudget() {
        ComputerBuild build = createBuild();

        assertFalse(service.isWithinBudget(build, new BigDecimal("800.00")));
        assertEquals(
                0,
                new BigDecimal("100.00")
                        .compareTo(service.calculateBudgetExcess(build, new BigDecimal("800.00"))));
    }

    @Test
    void shouldReturnNoIssuesWhenBuildMatchesAllCurrentRules() {
        ComputerBuild build = createBuild();

        assertTrue(service.findCompatibilityIssues(build).isEmpty());
    }

    @Test
    void shouldReturnBothIssuesWhenGraphicsCardAndCoolerAreTooLarge() {
        ComputerBuild build = createBuild();
        build.graphicsCard().setLengthMm(400);
        build.cooler().setHeightMm(180);

        List<String> issues = service.findCompatibilityIssues(build);

        assertEquals(2, issues.size());
        assertTrue(issues.contains("Vaizdo plokštė viršija korpuso leidžiamą ilgį."));
        assertTrue(
                issues.contains(
                        "Aušintuvo aukštis netinkamas korpusui arba aukščio riba nenurodyta."));
    }

    @Test
    void shouldAcceptPowerSupplyAtRequiredPowerBoundary() {
        ComputerBuild build = createBuild();
        build.powerSupply().setWattage(500);

        assertTrue(service.findCompatibilityIssues(build).isEmpty());
    }

    @Test
    void shouldRejectPowerSupplyBelowRequiredPower() {
        ComputerBuild build = createBuild();
        build.powerSupply().setWattage(499);

        List<String> issues = service.findCompatibilityIssues(build);

        assertEquals(1, issues.size());
        assertTrue(
                issues.contains(
                        "Maitinimo bloko galios nepakanka pagal skaičiavimo taisyklę arba trūksta galios duomenų."));
    }

    @Test
    void shouldReportMissingPowerConsumption() {
        ComputerBuild build = createBuild();
        build.processor().setPowerConsumptionW(null);

        List<String> issues = service.findCompatibilityIssues(build);

        assertEquals(1, issues.size());
        assertTrue(
                issues.contains(
                        "Maitinimo bloko galios nepakanka pagal skaičiavimo taisyklę arba trūksta galios duomenų."));
    }

    @Test
    void shouldReportUnsupportedStorageType() {
        ComputerBuild build = createBuild();
        build.motherboard().setSupportedStorageTypes(Set.of("SATA_2_5"));

        List<String> issues = service.findCompatibilityIssues(build);

        assertEquals(1, issues.size());
        assertTrue(
                issues.contains(
                        "SSD prijungimo tipas nepalaikomas pagrindinės plokštės arba trūksta palaikymo duomenų."));
    }

    @Test
    void shouldReportMissingStorageSupport() {
        ComputerBuild build = createBuild();
        build.motherboard().setSupportedStorageTypes(Set.of());

        List<String> issues = service.findCompatibilityIssues(build);

        assertEquals(1, issues.size());
        assertTrue(
                issues.contains(
                        "SSD prijungimo tipas nepalaikomas pagrindinės plokštės arba trūksta palaikymo duomenų."));
    }

    private ComputerBuild createBuild() {
        Processor processor = new Processor("Procesorius", new BigDecimal("200.00"), "AM5");
        Motherboard motherboard =
                new Motherboard("Pagrindinė plokštė", new BigDecimal("150.00"), "AM5", "DDR5");
        Memory memory = new Memory("RAM", new BigDecimal("90.00"), "DDR5", 32);
        GraphicsCard graphicsCard =
                new GraphicsCard("Vaizdo plokštė", new BigDecimal("250.00"), 300);
        Storage storage = new Storage("SSD", new BigDecimal("60.00"), "M2_2280_PCIE", 1000);
        PowerSupply powerSupply = new PowerSupply("Maitinimo blokas", new BigDecimal("70.00"), 650);
        Cooler cooler = new Cooler("Aušintuvas", new BigDecimal("30.00"), Set.of("AM5"), 155);
        ComputerCase computerCase = new ComputerCase("Korpusas", new BigDecimal("50.00"), 320);

        computerCase.setMaxCoolerHeightMm(160);
        processor.setPowerConsumptionW(100);
        graphicsCard.setPowerConsumptionW(200);
        motherboard.setSupportedStorageTypes(Set.of("SATA_2_5", "M2_2280_PCIE"));

        return new ComputerBuild(
                processor,
                motherboard,
                memory,
                graphicsCard,
                storage,
                powerSupply,
                cooler,
                computerCase);
    }
}
