package lt.kompiuteriai.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "memory_modules")
public class Memory extends ComputerComponent {

    @Column(nullable = false)
    private String memoryType;

    @Column(nullable = false)
    private int capacityGb;

    protected Memory() {}

    public Memory(String name, BigDecimal price, String memoryType, int capacityGb) {
        super(name, price);
        this.memoryType = memoryType;
        this.capacityGb = capacityGb;
    }

    public String getMemoryType() {
        return memoryType;
    }

    public void setMemoryType(String memoryType) {
        this.memoryType = memoryType;
    }

    public int getCapacityGb() {
        return capacityGb;
    }

    public void setCapacityGb(int capacityGb) {
        this.capacityGb = capacityGb;
    }
}
