package lt.kompiuteriai.backend.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import lt.kompiuteriai.backend.model.Storage;
import lt.kompiuteriai.backend.service.StorageService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies/{companyId}/storages")
public class StorageController {

    private final StorageService storageService;

    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    public record CreateStorageRequest(
            @NotBlank @Size(max = 100) String name,
            @NotNull @Positive @Digits(integer = 8, fraction = 2) BigDecimal price,
            @NotBlank
                    @Pattern(
                            regexp = "SATA_2_5|M2_2280_PCIE",
                            message = "Leidžiama SATA_2_5 arba M2_2280_PCIE")
                    String interfaceType,
            @Positive int capacityGb) {}

    public record StorageResponse(
            Long id, String name, BigDecimal price, String interfaceType, int capacityGb) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StorageResponse createStorage(
            @PathVariable("companyId") Long companyId,
            @Valid @RequestBody CreateStorageRequest request) {
        Storage storage =
                storageService.createStorage(
                        companyId,
                        request.name().strip(),
                        request.price(),
                        request.interfaceType().strip().toUpperCase(Locale.ROOT),
                        request.capacityGb());

        return toResponse(storage);
    }

    @GetMapping
    public List<StorageResponse> getStorages(@PathVariable("companyId") Long companyId) {
        return storageService.getStorages(companyId).stream().map(this::toResponse).toList();
    }

    @GetMapping("/compatible")
    public List<StorageResponse> getCompatibleStorages(
            @PathVariable("companyId") Long companyId,
            @RequestParam("motherboardId") Long motherboardId) {
        return storageService.getCompatibleStorages(companyId, motherboardId).stream()
                .map(this::toResponse)
                .toList();
    }

    private StorageResponse toResponse(Storage storage) {
        return new StorageResponse(
                storage.getId(),
                storage.getName(),
                storage.getPrice(),
                storage.getInterfaceType(),
                storage.getCapacityGb());
    }
}
