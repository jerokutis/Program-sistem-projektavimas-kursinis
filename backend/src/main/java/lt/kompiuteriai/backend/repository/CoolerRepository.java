package lt.kompiuteriai.backend.repository;

import java.util.List;
import java.util.Optional;
import lt.kompiuteriai.backend.model.Cooler;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoolerRepository extends JpaRepository<Cooler, Long> {

    @EntityGraph(attributePaths = "supportedSockets")
    List<Cooler> findByCompany_Id(Long companyId);

    @EntityGraph(attributePaths = "supportedSockets")
    Optional<Cooler> findByIdAndCompany_Id(Long id, Long companyId);
}
