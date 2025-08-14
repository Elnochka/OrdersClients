package orders.repository;

import orders.entity.Client;
import orders.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {
    java.util.List<OrderEntity> findBySupplier(Client supplier);
    java.util.List<OrderEntity> findByConsumer(Client consumer);

    @Query("select coalesce(sum(o.price),0) from OrderEntity o where o.supplier = :client")
    java.math.BigDecimal sumRevenueAsSupplier(@Param("client") Client client);

    @Query("select coalesce(sum(o.price),0) from OrderEntity o where o.consumer = :client")
    java.math.BigDecimal sumCostAsConsumer(@Param("client") Client client);

}
