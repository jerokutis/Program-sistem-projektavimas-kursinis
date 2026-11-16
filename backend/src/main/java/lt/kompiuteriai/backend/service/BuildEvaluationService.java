package lt.kompiuteriai.backend.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lt.kompiuteriai.backend.model.ComputerBuild;
import org.springframework.stereotype.Service;

@Service
public class BuildEvaluationService {

    private final CompatibilityService compatibilityService;

    public BuildEvaluationService(CompatibilityService compatibilityService) {
        this.compatibilityService = compatibilityService;
    }

    public BigDecimal calculateTotalPrice(ComputerBuild build) {
        return build.processor()
                .getPrice()
                .add(build.motherboard().getPrice())
                .add(build.memory().getPrice())
                .add(build.graphicsCard().getPrice())
                .add(build.storage().getPrice())
                .add(build.powerSupply().getPrice())
                .add(build.cooler().getPrice())
                .add(build.computerCase().getPrice());
    }

    public boolean isWithinBudget(ComputerBuild build, BigDecimal budget) {
        BigDecimal totalPrice = calculateTotalPrice(build);

        return totalPrice.compareTo(budget) <= 0;
    }

    public BigDecimal calculateBudgetExcess(ComputerBuild build, BigDecimal budget) {
        BigDecimal totalPrice = calculateTotalPrice(build);

        if (totalPrice.compareTo(budget) > 0) {
            return totalPrice.subtract(budget);
        }

        return BigDecimal.ZERO;
    }

    public List<String> findCompatibilityIssues(ComputerBuild build) {
        List<String> issues = new ArrayList<>();

        if (!compatibilityService.isCompatible(build.processor(), build.motherboard())) {
            issues.add("Procesoriaus ir pagrindinės plokštės lizdai nesutampa.");
        }

        if (!compatibilityService.isCompatible(build.memory(), build.motherboard())) {
            issues.add("RAM tipas nesutampa su pagrindinės plokštės tipu arba tipas nenurodytas.");
        }

        if (!compatibilityService.isCompatible(build.graphicsCard(), build.computerCase())) {
            issues.add("Vaizdo plokštė viršija korpuso leidžiamą ilgį.");
        }

        if (!compatibilityService.isCompatible(build.processor(), build.cooler())) {
            issues.add("Aušintuvas nepalaiko procesoriaus lizdo arba lizdas nenurodytas.");
        }

        if (!compatibilityService.isCompatible(build.cooler(), build.computerCase())) {
            issues.add("Aušintuvo aukštis netinkamas korpusui arba aukščio riba nenurodyta.");
        }

        if (!compatibilityService.isCompatible(
                build.processor(), build.graphicsCard(), build.powerSupply())) {
            issues.add(
                    "Maitinimo bloko galios nepakanka pagal skaičiavimo taisyklę arba trūksta galios duomenų.");
        }

        if (!compatibilityService.isCompatible(build.storage(), build.motherboard())) {
            issues.add(
                    "SSD prijungimo tipas nepalaikomas pagrindinės plokštės arba trūksta palaikymo duomenų.");
        }

        return issues;
    }
}
