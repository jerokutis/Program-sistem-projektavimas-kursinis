package lt.kompiuteriai.backend.repository;

import java.util.List;
import java.util.Optional;
import lt.kompiuteriai.backend.model.ComputerCase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComputerCaseRepository extends JpaRepository<ComputerCase, Long> {
    List<ComputerCase> findByCompany_Id(Long companyId);

    Optional<ComputerCase> findByIdAndCompany_Id(Long id, Long companyId);
}
