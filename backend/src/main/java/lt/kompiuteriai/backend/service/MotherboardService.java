package lt.kompiuteriai.backend.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import lt.kompiuteriai.backend.model.*;
import lt.kompiuteriai.backend.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MotherboardService {

    private final MotherboardRepository motherboardRepository;
    private final CompanyRepository companyRepository;
    private final ProcessorRepository processorRepository;
    private final CompatibilityService compatibilityService;
    private final MemoryRepository memoryRepository;
    private final StorageRepository storageRepository;

    public MotherboardService(
            MotherboardRepository motherboardRepository,
            CompanyRepository companyRepository,
            ProcessorRepository processorRepository,
            CompatibilityService compatibilityService,
            MemoryRepository memoryRepository,
            StorageRepository storageRepository) {
        this.motherboardRepository = motherboardRepository;
        this.companyRepository = companyRepository;
        this.processorRepository = processorRepository;
        this.compatibilityService = compatibilityService;
        this.memoryRepository = memoryRepository;
        this.storageRepository = storageRepository;
    }

    @Transactional
    public Motherboard createMotherboard(
            Long companyId,
            String name,
            BigDecimal price,
            String socket,
            String memoryType,
            Set<String> supportedStorageTypes) {
        Company company = findCompany(companyId);

        Motherboard motherboard = new Motherboard(name, price, socket, memoryType);
        motherboard.setSupportedStorageTypes(supportedStorageTypes);
        motherboard.setCompany(company);

        return motherboardRepository.save(motherboard);
    }

    @Transactional(readOnly = true)
    public List<Motherboard> getMotherboards(Long companyId) {
        findCompany(companyId);

        return motherboardRepository.findByCompany_Id(companyId);
    }

    @Transactional(readOnly = true)
    public Motherboard getMotherboard(Long companyId, Long motherboardId) {
        return motherboardRepository
                .findByIdAndCompany_Id(motherboardId, companyId)
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Pagrindinė plokštė šios įmonės kataloge nerasta"));
    }

    @Transactional(readOnly = true)
    public List<Motherboard> getCompatibleMotherboards(
            Long companyId, Long processorId, Long memoryId, Long storageId) {
        findCompany(companyId);

        List<Motherboard> motherboards = motherboardRepository.findByCompany_Id(companyId);

        if (processorId != null) {
            Processor processor =
                    processorRepository
                            .findByIdAndCompany_Id(processorId, companyId)
                            .orElseThrow(
                                    () ->
                                            new ResponseStatusException(
                                                    HttpStatus.NOT_FOUND,
                                                    "Procesorius šios įmonės kataloge nerastas"));

            motherboards =
                    motherboards.stream()
                            .filter(
                                    motherboard ->
                                            compatibilityService.isCompatible(
                                                    processor, motherboard))
                            .toList();
        }

        if (memoryId != null) {
            Memory memory =
                    memoryRepository
                            .findByIdAndCompany_Id(memoryId, companyId)
                            .orElseThrow(
                                    () ->
                                            new ResponseStatusException(
                                                    HttpStatus.NOT_FOUND,
                                                    "RAM šios įmonės kataloge nerasta"));

            motherboards =
                    motherboards.stream()
                            .filter(
                                    motherboard ->
                                            compatibilityService.isCompatible(memory, motherboard))
                            .toList();
        }

        if (storageId != null) {
            Storage storage =
                    storageRepository
                            .findByIdAndCompany_Id(storageId, companyId)
                            .orElseThrow(
                                    () ->
                                            new ResponseStatusException(
                                                    HttpStatus.NOT_FOUND,
                                                    "Diskas šios įmonės kataloge nerastas"));

            motherboards =
                    motherboards.stream()
                            .filter(
                                    motherboard ->
                                            compatibilityService.isCompatible(storage, motherboard))
                            .toList();
        }

        return motherboards;
    }

    private Company findCompany(Long companyId) {
        return companyRepository
                .findById(companyId)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Įmonė nerasta"));
    }
}
