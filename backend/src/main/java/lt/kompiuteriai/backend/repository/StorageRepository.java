package lt.kompiuteriai.backend.repository;

import java.util.List;
import java.util.Optional;
import lt.kompiuteriai.backend.model.Storage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StorageRepository extends JpaRepository<Storage, Long> {

    List<Storage> findByCompany_Id(Long companyId);

    Optional<Storage> findByIdAndCompany_Id(Long id, Long companyId);
}
