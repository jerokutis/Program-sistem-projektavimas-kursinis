package lt.kompiuteriai.backend.service;

import java.math.BigDecimal;
import java.util.List;
import lt.kompiuteriai.backend.model.Company;
import lt.kompiuteriai.backend.model.ComputerCase;
import lt.kompiuteriai.backend.model.Cooler;
import lt.kompiuteriai.backend.model.GraphicsCard;
import lt.kompiuteriai.backend.repository.CompanyRepository;
import lt.kompiuteriai.backend.repository.ComputerCaseRepository;
import lt.kompiuteriai.backend.repository.CoolerRepository;
import lt.kompiuteriai.backend.repository.GraphicsCardRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ComputerCaseService {

    private final GraphicsCardRepository graphicsCardRepository;
    private final CompatibilityService compatibilityService;
    private final ComputerCaseRepository computerCaseRepository;
    private final CompanyRepository companyRepository;
    private final CoolerRepository coolerRepository;

    public ComputerCaseService(
            ComputerCaseRepository computerCaseRepository,
            CompanyRepository companyRepository,
            GraphicsCardRepository graphicsCardRepository,
            CompatibilityService compatibilityService,
            CoolerRepository coolerRepository) {
        this.computerCaseRepository = computerCaseRepository;
        this.companyRepository = companyRepository;
        this.graphicsCardRepository = graphicsCardRepository;
        this.compatibilityService = compatibilityService;
        this.coolerRepository = coolerRepository;
    }

    @Transactional
    public ComputerCase createComputerCase(
            Long companyId,
            String name,
            BigDecimal price,
            int maxGpuLengthMm,
            Integer maxCoolerHeightMm) {
        Company company = findCompany(companyId);

        ComputerCase computerCase = new ComputerCase(name, price, maxGpuLengthMm);
        computerCase.setMaxCoolerHeightMm(maxCoolerHeightMm);

        computerCase.setCompany(company);

        return computerCaseRepository.save(computerCase);
    }

    @Transactional(readOnly = true)
    public List<ComputerCase> getComputerCases(Long companyId) {
        findCompany(companyId);

        return computerCaseRepository.findByCompany_Id(companyId);
    }

    @Transactional(readOnly = true)
    public ComputerCase getComputerCase(Long companyId, Long caseId) {
        return computerCaseRepository
                .findByIdAndCompany_Id(caseId, companyId)
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Korpusas šios įmonės kataloge nerastas"));
    }

    private Company findCompany(Long companyId) {
        return companyRepository
                .findById(companyId)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Įmonė nerasta"));
    }

    @Transactional(readOnly = true)
    public List<ComputerCase> getCompatibleCases(
            Long companyId, Long graphicsCardId, Long coolerId) {
        findCompany(companyId);

        List<ComputerCase> cases = computerCaseRepository.findByCompany_Id(companyId);

        if (graphicsCardId != null) {
            GraphicsCard graphicsCard =
                    graphicsCardRepository
                            .findByIdAndCompany_Id(graphicsCardId, companyId)
                            .orElseThrow(
                                    () ->
                                            new ResponseStatusException(
                                                    HttpStatus.NOT_FOUND,
                                                    "Vaizdo plokštė šios įmonės kataloge nerasta"));

            cases =
                    cases.stream()
                            .filter(
                                    computerCase ->
                                            compatibilityService.isCompatible(
                                                    graphicsCard, computerCase))
                            .toList();
        }

        if (coolerId != null) {
            Cooler cooler =
                    coolerRepository
                            .findByIdAndCompany_Id(coolerId, companyId)
                            .orElseThrow(
                                    () ->
                                            new ResponseStatusException(
                                                    HttpStatus.NOT_FOUND,
                                                    "Aušintuvas šios įmonės kataloge nerastas"));

            cases =
                    cases.stream()
                            .filter(
                                    computerCase ->
                                            compatibilityService.isCompatible(cooler, computerCase))
                            .toList();
        }

        return cases;
    }
}
