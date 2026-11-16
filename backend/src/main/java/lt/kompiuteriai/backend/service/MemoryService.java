package lt.kompiuteriai.backend.service;

import java.math.BigDecimal;
import java.util.List;
import lt.kompiuteriai.backend.model.Company;
import lt.kompiuteriai.backend.model.Memory;
import lt.kompiuteriai.backend.model.Motherboard;
import lt.kompiuteriai.backend.repository.CompanyRepository;
import lt.kompiuteriai.backend.repository.MemoryRepository;
import lt.kompiuteriai.backend.repository.MotherboardRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MemoryService {
    private final MemoryRepository memoryRepository;
    private final CompanyRepository companyRepository;
    private final MotherboardRepository motherboardRepository;
    private final CompatibilityService compatibilityService;

    public MemoryService(
            MemoryRepository memoryRepository,
            CompanyRepository companyRepository,
            MotherboardRepository motherboardRepository,
            CompatibilityService compatibilityService) {
        this.memoryRepository = memoryRepository;
        this.companyRepository = companyRepository;
        this.motherboardRepository = motherboardRepository;
        this.compatibilityService = compatibilityService;
    }

    @Transactional
    public Memory createMemory(
            Long companyId, String name, BigDecimal price, String memoryType, int capacityGb) {
        Company company = findCompany(companyId);

        Memory memory = new Memory(name, price, memoryType, capacityGb);
        memory.setCompany(company);

        return memoryRepository.save(memory);
    }

    @Transactional(readOnly = true)
    public Memory getMemory(Long companyId, Long memoryId) {
        return memoryRepository
                .findByIdAndCompany_Id(memoryId, companyId)
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND, "RAM šios įmonės kataloge nerasta"));
    }

    @Transactional(readOnly = true)
    public List<Memory> getCompatibleMemories(Long companyId, Long motherboardId) {
        Motherboard motherboard =
                motherboardRepository
                        .findByIdAndCompany_Id(motherboardId, companyId)
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Pagrindinė plokštė šios įmonės kataloge nerasta"));

        return memoryRepository.findByCompany_Id(companyId).stream()
                .filter(memory -> compatibilityService.isCompatible(memory, motherboard))
                .toList();
    }

    private Company findCompany(Long companyId) {
        return companyRepository
                .findById(companyId)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Įmonė nerasta"));
    }

    @Transactional(readOnly = true)
    public List<Memory> getMemories(Long companyId) {
        findCompany(companyId);

        return memoryRepository.findByCompany_Id(companyId);
    }
}
