package lt.kompiuteriai.backend.service;

import java.math.BigDecimal;
import java.util.List;
import lt.kompiuteriai.backend.model.*;
import lt.kompiuteriai.backend.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProcessorService {

    private final ProcessorRepository processorRepository;
    private final CompanyRepository companyRepository;
    private final MotherboardRepository motherboardRepository;
    private final CompatibilityService compatibilityService;
    private final CoolerRepository coolerRepository;
    private final GraphicsCardRepository graphicsCardRepository;
    private final PowerSupplyRepository powerSupplyRepository;

    public ProcessorService(
            ProcessorRepository processorRepository,
            CompanyRepository companyRepository,
            MotherboardRepository motherboardRepository,
            CompatibilityService compatibilityService,
            CoolerRepository coolerRepository,
            GraphicsCardRepository graphicsCardRepository,
            PowerSupplyRepository powerSupplyRepository) {
        this.processorRepository = processorRepository;
        this.companyRepository = companyRepository;
        this.motherboardRepository = motherboardRepository;
        this.compatibilityService = compatibilityService;
        this.coolerRepository = coolerRepository;
        this.graphicsCardRepository = graphicsCardRepository;
        this.powerSupplyRepository = powerSupplyRepository;
    }

    @Transactional
    public Processor createProcessor(
            Long companyId,
            String name,
            BigDecimal price,
            String socket,
            Integer powerConsumptionW) {
        Company company = findCompany(companyId);

        Processor processor = new Processor(name, price, socket);
        processor.setPowerConsumptionW(powerConsumptionW);
        processor.setCompany(company);

        return processorRepository.save(processor);
    }

    @Transactional(readOnly = true)
    public List<Processor> getProcessors(Long companyId) {
        findCompany(companyId);

        return processorRepository.findByCompany_Id(companyId);
    }

    @Transactional(readOnly = true)
    public Processor getProcessor(Long companyId, Long processorId) {
        return processorRepository
                .findByIdAndCompany_Id(processorId, companyId)
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Procesorius šios įmonės kataloge nerastas"));
    }

    @Transactional(readOnly = true)
    public List<Processor> getCompatibleProcessors(
            Long companyId,
            Long motherboardId,
            Long coolerId,
            Long graphicsCardId,
            Long powerSupplyId) {
        List<Processor> processors = getProcessors(companyId);

        if (motherboardId != null) {
            Motherboard motherboard =
                    motherboardRepository
                            .findByIdAndCompany_Id(motherboardId, companyId)
                            .orElseThrow(
                                    () ->
                                            new ResponseStatusException(
                                                    HttpStatus.NOT_FOUND,
                                                    "Pagrindinė plokštė šios įmonės kataloge nerasta"));

            processors =
                    processors.stream()
                            .filter(
                                    processor ->
                                            compatibilityService.isCompatible(
                                                    processor, motherboard))
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

            processors =
                    processors.stream()
                            .filter(
                                    processor ->
                                            compatibilityService.isCompatible(processor, cooler))
                            .toList();
        }

        if (graphicsCardId != null && powerSupplyId != null) {
            GraphicsCard graphicsCard =
                    graphicsCardRepository
                            .findByIdAndCompany_Id(graphicsCardId, companyId)
                            .orElseThrow(
                                    () ->
                                            new ResponseStatusException(
                                                    HttpStatus.NOT_FOUND,
                                                    "Vaizdo plokštė šios įmonės kataloge nerasta"));

            PowerSupply powerSupply =
                    powerSupplyRepository
                            .findByIdAndCompany_Id(powerSupplyId, companyId)
                            .orElseThrow(
                                    () ->
                                            new ResponseStatusException(
                                                    HttpStatus.NOT_FOUND,
                                                    "Maitinimo blokas šios įmonės kataloge nerastas"));

            processors =
                    processors.stream()
                            .filter(
                                    processor ->
                                            compatibilityService.isCompatible(
                                                    processor, graphicsCard, powerSupply))
                            .toList();
        }

        return processors;
    }

    private Company findCompany(Long companyId) {
        return companyRepository
                .findById(companyId)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Įmonė nerasta"));
    }
}
