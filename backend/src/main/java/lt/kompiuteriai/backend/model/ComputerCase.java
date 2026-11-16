package lt.kompiuteriai.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "computer_cases")
public class ComputerCase extends ComputerComponent {

    @Column(nullable = false)
    private int maxGpuLengthMm;

    private Integer maxCoolerHeightMm;

    protected ComputerCase() {}

    public ComputerCase(String name, BigDecimal price, int maxGpuLengthMm) {
        super(name, price);
        this.maxGpuLengthMm = maxGpuLengthMm;
    }

    public int getMaxGpuLengthMm() {
        return maxGpuLengthMm;
    }

    public void setMaxGpuLengthMm(int maxGpuLengthMm) {
        this.maxGpuLengthMm = maxGpuLengthMm;
    }

    public Integer getMaxCoolerHeightMm() {
        return maxCoolerHeightMm;
    }

    public void setMaxCoolerHeightMm(Integer maxCoolerHeightMm) {
        this.maxCoolerHeightMm = maxCoolerHeightMm;
    }
}
