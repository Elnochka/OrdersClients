package orders.repository;

import orders.entity.Client;
import orders.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {
    List<OrderEntity> findBySupplier(Client supplier);
    List<OrderEntity> findByConsumer(Client consumer);

    @Query("select coalesce(sum(o.price),0) from OrderEntity o where o.supplier = :client")
    BigDecimal sumRevenueAsSupplier(@Param("client") Client client);

    @Query("select coalesce(sum(o.price),0) from OrderEntity o where o.consumer = :client")
    BigDecimal sumCostAsConsumer(@Param("client") Client client);

}
