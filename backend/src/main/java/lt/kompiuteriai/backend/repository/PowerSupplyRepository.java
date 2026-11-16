package lt.kompiuteriai.backend.repository;

import java.util.List;
import java.util.Optional;
import lt.kompiuteriai.backend.model.PowerSupply;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PowerSupplyRepository extends JpaRepository<PowerSupply, Long> {

    List<PowerSupply> findByCompany_Id(Long companyId);

    Optional<PowerSupply> findByIdAndCompany_Id(Long id, Long companyId);
}
