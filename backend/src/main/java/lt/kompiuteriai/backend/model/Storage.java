package lt.kompiuteriai.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "storage_drives")
public class Storage extends ComputerComponent {

    @Column(nullable = false)
    private String interfaceType;

    @Column(nullable = false)
    private int capacityGb;

    protected Storage() {}

    public Storage(String name, BigDecimal price, String interfaceType, int capacityGb) {
        super(name, price);
        this.interfaceType = interfaceType;
        this.capacityGb = capacityGb;
    }

    public String getInterfaceType() {
        return interfaceType;
    }

    public void setInterfaceType(String interfaceType) {
        this.interfaceType = interfaceType;
    }

    public int getCapacityGb() {
        return capacityGb;
    }

    public void setCapacityGb(int capacityGb) {
        this.capacityGb = capacityGb;
    }
}
