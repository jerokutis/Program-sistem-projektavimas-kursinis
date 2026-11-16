package lt.kompiuteriai.backend.repository;

import java.util.List;
import java.util.Optional;
import lt.kompiuteriai.backend.model.Memory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemoryRepository extends JpaRepository<Memory, Long> {

    List<Memory> findByCompany_Id(Long companyId);

    Optional<Memory> findByIdAndCompany_Id(Long id, Long companyId);
}
