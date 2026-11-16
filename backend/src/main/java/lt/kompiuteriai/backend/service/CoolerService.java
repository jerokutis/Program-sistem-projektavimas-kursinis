package lt.kompiuteriai.backend.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import lt.kompiuteriai.backend.model.Company;
import lt.kompiuteriai.backend.model.ComputerCase;
import lt.kompiuteriai.backend.model.Cooler;
import lt.kompiuteriai.backend.model.Processor;
import lt.kompiuteriai.backend.repository.CompanyRepository;
import lt.kompiuteriai.backend.repository.ComputerCaseRepository;
import lt.kompiuteriai.backend.repository.CoolerRepository;
import lt.kompiuteriai.backend.repository.ProcessorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CoolerService {

    private final CoolerRepository coolerRepository;
    private final CompanyRepository companyRepository;
    private final ProcessorRepository processorRepository;
    private final CompatibilityService compatibilityService;
    private final ComputerCaseRepository computerCaseRepository;

    public CoolerService(
            CoolerRepository coolerRepository,
            CompanyRepository companyRepository,
            ProcessorRepository processorRepository,
            CompatibilityService compatibilityService,
            ComputerCaseRepository computerCaseRepository) {
        this.coolerRepository = coolerRepository;
        this.companyRepository = companyRepository;
        this.processorRepository = processorRepository;
        this.compatibilityService = compatibilityService;
        this.computerCaseRepository = computerCaseRepository;
    }

    @Transactional
    public Cooler createCooler(
            Long companyId,
            String name,
            BigDecimal price,
            Set<String> supportedSockets,
            int heightMm) {
        Company company = findCompany(companyId);

        Cooler cooler = new Cooler(name, price, supportedSockets, heightMm);
        cooler.setCompany(company);

        return coolerRepository.save(cooler);
    }

    @Transactional(readOnly = true)
    public List<Cooler> getCoolers(Long companyId) {
        findCompany(companyId);

        return coolerRepository.findByCompany_Id(companyId);
    }

    @Transactional(readOnly = true)
    public Cooler getCooler(Long companyId, Long coolerId) {
        return coolerRepository
                .findByIdAndCompany_Id(coolerId, companyId)
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Aušintuvas šios įmonės kataloge nerastas"));
    }

    @Transactional(readOnly = true)
    public List<Cooler> getCompatibleCoolers(Long companyId, Long processorId, Long caseId) {
        findCompany(companyId);

        List<Cooler> coolers = coolerRepository.findByCompany_Id(companyId);

        if (processorId != null) {
            Processor processor =
                    processorRepository
                            .findByIdAndCompany_Id(processorId, companyId)
                            .orElseThrow(
                                    () ->
                                            new ResponseStatusException(
                                                    HttpStatus.NOT_FOUND,
                                                    "Procesorius šios įmonės kataloge nerastas"));

            coolers =
                    coolers.stream()
                            .filter(cooler -> compatibilityService.isCompatible(processor, cooler))
                            .toList();
        }

        if (caseId != null) {
            ComputerCase computerCase =
                    computerCaseRepository
                            .findByIdAndCompany_Id(caseId, companyId)
                            .orElseThrow(
                                    () ->
                                            new ResponseStatusException(
                                                    HttpStatus.NOT_FOUND,
                                                    "Korpusas šios įmonės kataloge nerastas"));

            coolers =
                    coolers.stream()
                            .filter(
                                    cooler ->
                                            compatibilityService.isCompatible(cooler, computerCase))
                            .toList();
        }

        return coolers;
    }

    private Company findCompany(Long companyId) {
        return companyRepository
                .findById(companyId)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Įmonė nerasta"));
    }
}
