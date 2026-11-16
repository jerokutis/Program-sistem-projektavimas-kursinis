package lt.kompiuteriai.backend.model;

public record ComputerBuild(
        Processor processor,
        Motherboard motherboard,
        Memory memory,
        GraphicsCard graphicsCard,
        Storage storage,
        PowerSupply powerSupply,
        Cooler cooler,
        ComputerCase computerCase) {}
