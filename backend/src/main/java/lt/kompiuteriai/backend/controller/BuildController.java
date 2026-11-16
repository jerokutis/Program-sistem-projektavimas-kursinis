package lt.kompiuteriai.backend.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
import lt.kompiuteriai.backend.model.ComputerBuild;
import lt.kompiuteriai.backend.service.BuildEvaluationService;
import lt.kompiuteriai.backend.service.BuildService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies/{companyId}/builds")
public class BuildController {

    private final BuildService buildService;
    private final BuildEvaluationService buildEvaluationService;

    public BuildController(
            BuildService buildService, BuildEvaluationService buildEvaluationService) {
        this.buildService = buildService;
        this.buildEvaluationService = buildEvaluationService;
    }

    public record EvaluateBuildRequest(
            @NotNull @Positive Long processorId,
            @NotNull @Positive Long motherboardId,
            @NotNull @Positive Long memoryId,
            @NotNull @Positive Long graphicsCardId,
            @NotNull @Positive Long storageId,
            @NotNull @Positive Long powerSupplyId,
            @NotNull @Positive Long coolerId,
            @NotNull @Positive Long caseId,
            @NotNull @Positive @Digits(integer = 8, fraction = 2) BigDecimal budget) {}

    public record BuildEvaluationResponse(
            BigDecimal totalPrice,
            BigDecimal budget,
            boolean withinBudget,
            BigDecimal budgetExcess,
            List<String> compatibilityIssues) {}

    @PostMapping("/evaluate")
    public BuildEvaluationResponse evaluateBuild(
            @PathVariable("companyId") Long companyId,
            @Valid @RequestBody EvaluateBuildRequest request) {
        ComputerBuild build =
                buildService.getBuild(
                        companyId,
                        request.processorId(),
                        request.motherboardId(),
                        request.memoryId(),
                        request.graphicsCardId(),
                        request.storageId(),
                        request.powerSupplyId(),
                        request.coolerId(),
                        request.caseId());

        BigDecimal totalPrice = buildEvaluationService.calculateTotalPrice(build);
        boolean withinBudget = buildEvaluationService.isWithinBudget(build, request.budget());
        BigDecimal budgetExcess =
                buildEvaluationService.calculateBudgetExcess(build, request.budget());
        List<String> compatibilityIssues = buildEvaluationService.findCompatibilityIssues(build);

        return new BuildEvaluationResponse(
                totalPrice, request.budget(), withinBudget, budgetExcess, compatibilityIssues);
    }
}
