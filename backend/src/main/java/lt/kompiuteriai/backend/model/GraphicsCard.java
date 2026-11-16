package lt.kompiuteriai.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "graphics_card")
public class GraphicsCard extends ComputerComponent {

    @Column(nullable = false)
    private boolean published = true;

    private Integer powerConsumptionW;

    @Column(nullable = false)
    private int lengthMm;

    protected GraphicsCard() {}

    public GraphicsCard(String name, BigDecimal price, int lengthMm) {
        super(name, price);
        this.lengthMm = lengthMm;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public Integer getPowerConsumptionW() {
        return powerConsumptionW;
    }

    public void setPowerConsumptionW(Integer powerConsumptionW) {
        this.powerConsumptionW = powerConsumptionW;
    }

    public int getLengthMm() {
        return lengthMm;
    }

    public void setLengthMm(int lengthMm) {
        this.lengthMm = lengthMm;
    }
}
