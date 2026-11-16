package lt.kompiuteriai.backend.service;

import java.util.List;
import lt.kompiuteriai.backend.model.Company;
import lt.kompiuteriai.backend.repository.CompanyRepository;
import org.springframework.stereotype.Service;

@Service
public class CompanyService {

    private CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Company createCompany(String name) {
        Company company = new Company(name);

        return companyRepository.save(company);
    }

    public List<Company> getCompanies() {
        return companyRepository.findAll();
    }
}
