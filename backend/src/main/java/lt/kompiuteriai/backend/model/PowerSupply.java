package lt.kompiuteriai.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "power_supplies")
public class PowerSupply extends ComputerComponent {

    @Column(nullable = false)
    private int wattage;

    protected PowerSupply() {}

    public PowerSupply(String name, BigDecimal price, int wattage) {
        super(name, price);
        this.wattage = wattage;
    }

    public int getWattage() {
        return wattage;
    }

    public void setWattage(int wattage) {
        this.wattage = wattage;
    }
}
