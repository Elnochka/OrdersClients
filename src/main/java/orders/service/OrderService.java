package orders.service;

import lombok.extern.slf4j.Slf4j;
import orders.dto.CreateOrderRequest;
import orders.entity.Client;
import orders.entity.OrderEntity;
import orders.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import orders.repository.ClientRepository;
import orders.repository.OrderRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderService {
    private final OrderRepository orderRepo;
    private final ClientRepository clientRepo;
    private final Random random = new Random();

    public OrderService(OrderRepository orderRepo, ClientRepository clientRepo) {
        this.orderRepo = orderRepo;
        this.clientRepo = clientRepo;
    }

    @Transactional
    public OrderEntity createOrder(CreateOrderRequest req) {
        log.info("Saving new order: {}", req.getTitle());
        if (req.getPrice().compareTo(BigDecimal.ZERO) <= 0) throw new BusinessException("Price must be > 0");
        UUID sId = req.getSupplierId();
        UUID cId = req.getConsumerId();
        if (sId.equals(cId)) throw new BusinessException("Supplier and consumer must be different");

        // Стабильный порядок блокировок, чтобы избежать дедлоков
        java.util.List<UUID> ids = java.util.stream.Stream.of(sId, cId).sorted().collect(Collectors.toList());
        Client first = clientRepo.findByIdForUpdate(ids.get(0)).orElseThrow(() -> new BusinessException("Client not found"));
        Client second = clientRepo.findByIdForUpdate(ids.get(1)).orElseThrow(() -> new BusinessException("Client not found"));
        Client supplier = first.getId().equals(sId) ? first : second;
        Client consumer = first.getId().equals(cId) ? first : second;

        if (!supplier.isActive() || !consumer.isActive()) throw new BusinessException("Cannot create order for inactive client");

        // Проверяем будущую выгоду: для продавца +price, для покупателя -price
        BigDecimal supplierCurrent = orderRepo.sumRevenueAsSupplier(supplier).subtract(orderRepo.sumCostAsConsumer(supplier));
        BigDecimal consumerCurrent = orderRepo.sumRevenueAsSupplier(consumer).subtract(orderRepo.sumCostAsConsumer(consumer));
        BigDecimal supplierFuture = supplierCurrent.add(req.getPrice());
        BigDecimal consumerFuture = consumerCurrent.subtract(req.getPrice());
        if (supplierFuture.compareTo(new BigDecimal("-1000")) < 0 || consumerFuture.compareTo(new BigDecimal("-1000")) < 0) {
            throw new BusinessException("Creating this order would make a client's total profit < -1000");
        }

        // Эмуляция задержки обработки 1..10 сек перед записью
        try { Thread.sleep((1 + random.nextInt(10)) * 1000L); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        OrderEntity order = new OrderEntity();
        order.setTitle(req.getTitle());
        order.setSupplier(supplier);
        order.setConsumer(consumer);
        order.setPrice(req.getPrice());
        order.setProcessingStart(Instant.now());
        order.setProcessingEnd(Instant.now());
        log.debug("Saved order with ID: {}", order.getId());
        return orderRepo.save(order);
    }

}
