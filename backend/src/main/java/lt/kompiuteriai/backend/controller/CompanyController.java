package lt.kompiuteriai.backend.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lt.kompiuteriai.backend.model.Company;
import lt.kompiuteriai.backend.service.CompanyService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    public record CreateCompanyRequest(@NotBlank @Size(max = 100) String name) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Company createCompany(@Valid @RequestBody CreateCompanyRequest request) {
        return companyService.createCompany(request.name().strip());
    }

    @GetMapping
    public List<Company> getCompanies() {
        return companyService.getCompanies();
    }
}
