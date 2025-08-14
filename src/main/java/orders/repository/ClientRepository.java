package orders.repository;

import orders.entity.Client;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID>, JpaSpecificationExecutor<Client> {
    Optional<Client> findByEmailIgnoreCase(String email);

    @Query("select c from Client c where (lower(c.name) like lower(concat('%',:kw,'%')) " +
            "or lower(c.email) like lower(concat('%',:kw,'%')) " +
            "or lower(c.address) like lower(concat('%',:kw,'%')))")
    List<Client> searchByKeyword(@Param("kw") String keyword);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Client c where c.id = :id")
    Optional<Client> findByIdForUpdate(@Param("id") UUID id);

}
