package lt.kompiuteriai.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "motherboards")
public class Motherboard extends ComputerComponent {

    @ElementCollection
    @CollectionTable(
            name = "motherboard_storage_types",
            joinColumns = @JoinColumn(name = "motherboard_id"))
    @Column(name = "storage_type", nullable = false)
    private Set<String> supportedStorageTypes = new HashSet<>();

    @Column(nullable = false)
    private String socket;

    private String memoryType;

    protected Motherboard() {}

    public Motherboard(String name, BigDecimal price, String socket, String memoryType) {
        super(name, price);
        this.socket = socket;
        this.memoryType = memoryType;
    }

    public String getSocket() {
        return socket;
    }

    public void setSocket(String socket) {
        this.socket = socket;
    }

    public String getMemoryType() {
        return memoryType;
    }

    public void setMemoryType(String memoryType) {
        this.memoryType = memoryType;
    }

    public Set<String> getSupportedStorageTypes() {
        return supportedStorageTypes;
    }

    public void setSupportedStorageTypes(Set<String> supportedStorageTypes) {
        this.supportedStorageTypes = new HashSet<>(supportedStorageTypes);
    }
}
