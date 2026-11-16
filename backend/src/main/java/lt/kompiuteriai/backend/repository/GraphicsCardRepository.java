package lt.kompiuteriai.backend.repository;

import java.util.List;
import java.util.Optional;
import lt.kompiuteriai.backend.model.GraphicsCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GraphicsCardRepository extends JpaRepository<GraphicsCard, Long> {

    @Query(
            """
            select card
            from GraphicsCard card
            where card.company.id = :companyId
              and card.published = true
            order by card.id
            """)
    List<GraphicsCard> findByCompany_Id(@Param("companyId") Long companyId);

    @Query(
            """
            select card
            from GraphicsCard card
            where card.id = :id
              and card.company.id = :companyId
              and card.published = true
            """)
    Optional<GraphicsCard> findByIdAndCompany_Id(
            @Param("id") Long id, @Param("companyId") Long companyId);

    List<GraphicsCard> findByCompany_IdOrderByIdAsc(Long companyId);

    Optional<GraphicsCard> findByCompany_IdAndId(Long companyId, Long id);
}
