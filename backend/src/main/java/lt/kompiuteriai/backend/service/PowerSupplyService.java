package lt.kompiuteriai.backend.service;

import java.math.BigDecimal;
import java.util.List;
import lt.kompiuteriai.backend.model.Company;
import lt.kompiuteriai.backend.model.GraphicsCard;
import lt.kompiuteriai.backend.model.PowerSupply;
import lt.kompiuteriai.backend.model.Processor;
import lt.kompiuteriai.backend.repository.CompanyRepository;
import lt.kompiuteriai.backend.repository.GraphicsCardRepository;
import lt.kompiuteriai.backend.repository.PowerSupplyRepository;
import lt.kompiuteriai.backend.repository.ProcessorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PowerSupplyService {

    private final PowerSupplyRepository powerSupplyRepository;
    private final CompanyRepository companyRepository;
    private final ProcessorRepository processorRepository;
    private final GraphicsCardRepository graphicsCardRepository;
    private final CompatibilityService compatibilityService;

    public PowerSupplyService(
            PowerSupplyRepository powerSupplyRepository,
            CompanyRepository companyRepository,
            ProcessorRepository processorRepository,
            GraphicsCardRepository graphicsCardRepository,
            CompatibilityService compatibilityService) {
        this.powerSupplyRepository = powerSupplyRepository;
        this.companyRepository = companyRepository;
        this.processorRepository = processorRepository;
        this.graphicsCardRepository = graphicsCardRepository;
        this.compatibilityService = compatibilityService;
    }

    @Transactional
    public PowerSupply createPowerSupply(
            Long companyId, String name, BigDecimal price, int wattage) {
        Company company = findCompany(companyId);

        PowerSupply powerSupply = new PowerSupply(name, price, wattage);
        powerSupply.setCompany(company);

        return powerSupplyRepository.save(powerSupply);
    }

    @Transactional(readOnly = true)
    public List<PowerSupply> getPowerSupplies(Long companyId) {
        findCompany(companyId);

        return powerSupplyRepository.findByCompany_Id(companyId);
    }

    @Transactional(readOnly = true)
    public PowerSupply getPowerSupply(Long companyId, Long powerSupplyId) {
        return powerSupplyRepository
                .findByIdAndCompany_Id(powerSupplyId, companyId)
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Maitinimo blokas šios įmonės kataloge nerastas"));
    }

    @Transactional(readOnly = true)
    public List<PowerSupply> getCompatiblePowerSupplies(
            Long companyId, Long processorId, Long graphicsCardId) {
        Processor processor =
                processorRepository
                        .findByIdAndCompany_Id(processorId, companyId)
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Vaizdo plokštė šios įmonės kataloge nerasta"));

        GraphicsCard graphicsCard =
                graphicsCardRepository
                        .findByIdAndCompany_Id(graphicsCardId, companyId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ""));

        return powerSupplyRepository.findByCompany_Id(companyId).stream()
                .filter(
                        powerSupply ->
                                compatibilityService.isCompatible(
                                        processor, graphicsCard, powerSupply))
                .toList();
    }

    private Company findCompany(Long companyId) {
        return companyRepository
                .findById(companyId)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Įmonė nerasta"));
    }
}
