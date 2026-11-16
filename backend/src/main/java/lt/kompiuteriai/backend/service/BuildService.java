package lt.kompiuteriai.backend.service;

import lt.kompiuteriai.backend.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BuildService {

    private final ProcessorService processorService;
    private final MotherboardService motherboardService;
    private final MemoryService memoryService;
    private final GraphicsCardService graphicsCardService;
    private final StorageService storageService;
    private final PowerSupplyService powerSupplyService;
    private final CoolerService coolerService;
    private final ComputerCaseService computerCaseService;

    public BuildService(
            ProcessorService processorService,
            MotherboardService motherboardService,
            MemoryService memoryService,
            GraphicsCardService graphicsCardService,
            StorageService storageService,
            PowerSupplyService powerSupplyService,
            CoolerService coolerService,
            ComputerCaseService computerCaseService) {
        this.processorService = processorService;
        this.motherboardService = motherboardService;
        this.memoryService = memoryService;
        this.graphicsCardService = graphicsCardService;
        this.storageService = storageService;
        this.powerSupplyService = powerSupplyService;
        this.coolerService = coolerService;
        this.computerCaseService = computerCaseService;
    }

    @Transactional(readOnly = true)
    public ComputerBuild getBuild(
            Long companyId,
            Long processorId,
            Long motherboardId,
            Long memoryId,
            Long graphicsCardId,
            Long storageId,
            Long powerSupplyId,
            Long coolerId,
            Long caseId) {
        Processor processor = processorService.getProcessor(companyId, processorId);
        Motherboard motherboard = motherboardService.getMotherboard(companyId, motherboardId);
        Memory memory = memoryService.getMemory(companyId, memoryId);
        GraphicsCard graphicsCard = graphicsCardService.getGraphicsCard(companyId, graphicsCardId);
        Storage storage = storageService.getStorage(companyId, storageId);
        PowerSupply powerSupply = powerSupplyService.getPowerSupply(companyId, powerSupplyId);
        Cooler cooler = coolerService.getCooler(companyId, coolerId);
        ComputerCase computerCase = computerCaseService.getComputerCase(companyId, caseId);

        return new ComputerBuild(
                processor,
                motherboard,
                memory,
                graphicsCard,
                storage,
                powerSupply,
                cooler,
                computerCase);
    }
}
