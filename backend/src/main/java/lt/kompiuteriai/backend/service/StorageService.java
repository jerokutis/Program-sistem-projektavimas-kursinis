package lt.kompiuteriai.backend.service;

import java.math.BigDecimal;
import java.util.List;
import lt.kompiuteriai.backend.model.Company;
import lt.kompiuteriai.backend.model.Motherboard;
import lt.kompiuteriai.backend.model.Storage;
import lt.kompiuteriai.backend.repository.CompanyRepository;
import lt.kompiuteriai.backend.repository.StorageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StorageService {

    private final StorageRepository storageRepository;
    private final CompanyRepository companyRepository;
    private final MotherboardService motherboardService;
    private final CompatibilityService compatibilityService;

    public StorageService(
            StorageRepository storageRepository,
            CompanyRepository companyRepository,
            MotherboardService motherboardService,
            CompatibilityService compatibilityService) {
        this.storageRepository = storageRepository;
        this.companyRepository = companyRepository;
        this.motherboardService = motherboardService;
        this.compatibilityService = compatibilityService;
    }

    @Transactional
    public Storage createStorage(
            Long companyId, String name, BigDecimal price, String interfaceType, int capacityGb) {
        Company company = findCompany(companyId);

        Storage storage = new Storage(name, price, interfaceType, capacityGb);
        storage.setCompany(company);

        return storageRepository.save(storage);
    }

    @Transactional(readOnly = true)
    public List<Storage> getStorages(Long companyId) {
        findCompany(companyId);

        return storageRepository.findByCompany_Id(companyId);
    }

    @Transactional(readOnly = true)
    public Storage getStorage(Long companyId, Long storageId) {
        return storageRepository
                .findByIdAndCompany_Id(storageId, companyId)
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Diskas šios įmonės kataloge nerastas"));
    }

    @Transactional(readOnly = true)
    public List<Storage> getCompatibleStorages(Long companyId, Long motherboardId) {
        Motherboard motherboard = motherboardService.getMotherboard(companyId, motherboardId);

        return storageRepository.findByCompany_Id(companyId).stream()
                .filter(storage -> compatibilityService.isCompatible(storage, motherboard))
                .toList();
    }

    private Company findCompany(Long companyId) {
        return companyRepository
                .findById(companyId)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Įmonė nerasta"));
    }
}
