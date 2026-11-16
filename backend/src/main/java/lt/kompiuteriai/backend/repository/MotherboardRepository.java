package lt.kompiuteriai.backend.repository;

import java.util.List;
import java.util.Optional;
import lt.kompiuteriai.backend.model.Motherboard;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MotherboardRepository extends JpaRepository<Motherboard, Long> {

    @EntityGraph(attributePaths = "supportedStorageTypes")
    List<Motherboard> findByCompany_Id(Long companyId);

    @EntityGraph(attributePaths = "supportedStorageTypes")
    Optional<Motherboard> findByIdAndCompany_Id(Long id, Long companyId);
}
