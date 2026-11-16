package lt.kompiuteriai.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "processors")
public class Processor extends ComputerComponent {

    private Integer powerConsumptionW;

    @Column(nullable = false)
    private String socket;

    protected Processor() {}

    public Processor(String name, BigDecimal price, String socket) {
        super(name, price);
        this.socket = socket;
    }

    public Integer getPowerConsumptionW() {
        return powerConsumptionW;
    }

    public void setPowerConsumptionW(Integer powerConsumptionW) {
        this.powerConsumptionW = powerConsumptionW;
    }

    public String getSocket() {
        return socket;
    }

    public void setSocket(String socket) {
        this.socket = socket;
    }
}
