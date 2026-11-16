package lt.kompiuteriai.backend.repository;

import java.util.List;
import java.util.Optional;
import lt.kompiuteriai.backend.model.Processor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessorRepository extends JpaRepository<Processor, Long> {

    List<Processor> findByCompany_Id(Long companyId);

    Optional<Processor> findByIdAndCompany_Id(Long id, Long companyId);
}
