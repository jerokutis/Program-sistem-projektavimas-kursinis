package lt.kompiuteriai.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "coolers")
public class Cooler extends ComputerComponent {

    @ElementCollection
    @CollectionTable(name = "cooler_sockets", joinColumns = @JoinColumn(name = "cooler_id"))
    @Column(name = "socket", nullable = false)
    private Set<String> supportedSockets = new HashSet<>();

    @Column(nullable = false)
    private int heightMm;

    protected Cooler() {}

    public Cooler(String name, BigDecimal price, Set<String> supportedSockets, int heightMm) {
        super(name, price);
        this.supportedSockets = new HashSet<>(supportedSockets);
        this.heightMm = heightMm;
    }

    public Set<String> getSupportedSockets() {
        return supportedSockets;
    }

    public void setSupportedSockets(Set<String> supportedSockets) {
        this.supportedSockets = supportedSockets;
    }

    public int getHeightMm() {
        return heightMm;
    }

    public void setHeightMm(int heightMm) {
        this.heightMm = heightMm;
    }
}
