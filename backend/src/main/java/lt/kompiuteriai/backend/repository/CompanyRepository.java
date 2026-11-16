package lt.kompiuteriai.backend.repository;

import lt.kompiuteriai.backend.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {}
