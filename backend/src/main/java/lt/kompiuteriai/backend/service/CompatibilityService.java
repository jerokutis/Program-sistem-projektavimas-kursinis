package lt.kompiuteriai.backend.service;

import java.math.BigDecimal;
import lt.kompiuteriai.backend.model.*;
import org.springframework.stereotype.Service;

@Service
public class CompatibilityService {

    private static final BigDecimal OTHER_COMPONENTS_POWER_W = new BigDecimal("100");
    private static final BigDecimal POWER_RESERVE_MULTIPLIER = new BigDecimal("1.25");

    public boolean isCompatible(GraphicsCard graphicsCard, ComputerCase computerCase) {
        return graphicsCard.getLengthMm() <= computerCase.getMaxGpuLengthMm();
    }

    public boolean isCompatible(Processor processor, Motherboard motherboard) {
        return processor.getSocket().equalsIgnoreCase(motherboard.getSocket());
    }

    public boolean isCompatible(Memory memory, Motherboard motherboard) {
        String memoryType = memory.getMemoryType();
        String supportedType = motherboard.getMemoryType();

        if (memoryType == null
                || supportedType == null
                || memoryType.isBlank()
                || supportedType.isBlank()) {
            return false;
        }

        return memoryType.strip().equalsIgnoreCase(supportedType.strip());
    }

    public boolean isCompatible(Processor processor, Cooler cooler) {
        String socket = processor.getSocket();

        if (socket == null || socket.isBlank()) {
            return false;
        }
        return cooler.getSupportedSockets().stream()
                .anyMatch(
                        supportedSocket ->
                                supportedSocket != null
                                        && supportedSocket
                                                .strip()
                                                .equalsIgnoreCase(socket.strip()));
    }

    public boolean isCompatible(Cooler cooler, ComputerCase computerCase) {
        Integer maxCoolerHeightMm = computerCase.getMaxCoolerHeightMm();

        if (maxCoolerHeightMm == null || maxCoolerHeightMm <= 0 || cooler.getHeightMm() <= 0) {
            return false;
        }

        return cooler.getHeightMm() <= maxCoolerHeightMm;
    }

    public boolean isCompatible(
            Processor processor, GraphicsCard graphicsCard, PowerSupply powerSupply) {
        Integer processorPower = processor.getPowerConsumptionW();
        Integer graphicsCardPower = graphicsCard.getPowerConsumptionW();

        if (processorPower == null || graphicsCardPower == null) {
            return false;
        }

        if (processorPower <= 0 || graphicsCardPower <= 0 || powerSupply.getWattage() <= 0) {
            return false;
        }

        BigDecimal requiredPower =
                BigDecimal.valueOf(processorPower)
                        .add(BigDecimal.valueOf(graphicsCardPower))
                        .add(OTHER_COMPONENTS_POWER_W)
                        .multiply(POWER_RESERVE_MULTIPLIER);

        return BigDecimal.valueOf(powerSupply.getWattage()).compareTo(requiredPower) >= 0;
    }

    public boolean isCompatible(Storage storage, Motherboard motherboard) {
        String interfaceType = storage.getInterfaceType();

        if (interfaceType == null || interfaceType.isBlank()) {
            return false;
        }

        if (!interfaceType.equals("SATA_2_5") && !interfaceType.equals("M2_2280_PCIE")) {
            return false;
        }

        return motherboard.getSupportedStorageTypes().contains(interfaceType);
    }
}
