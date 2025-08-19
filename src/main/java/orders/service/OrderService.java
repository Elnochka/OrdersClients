package orders.service;


import lombok.extern.slf4j.Slf4j;
import orders.dto.CreateOrderRequest;
import orders.entity.Client;
import orders.entity.OrderEntity;
import orders.exception.BusinessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import orders.repository.ClientRepository;
import orders.repository.OrderRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;
    private final Random random = new Random();

    public OrderService(OrderRepository orderRepo, ClientRepository clientRepo) {
        this.orderRepository = orderRepo;
        this.clientRepository = clientRepo;
    }

    @Transactional
    public OrderEntity createOrder(CreateOrderRequest req) {
        UUID sId = req.getSupplierId();
        UUID cId = req.getConsumerId();

            // 1. Проверка цены
            if (req.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("Price must be > 0");
            }

            // 2. Нельзя создать заказ самому себе
            if (sId.equals(cId)) {
                throw new BusinessException("Supplier and consumer must be different");
            }

            // 3. Загружаем клиентов с блокировкой (чтобы избежать гонок)
            List<UUID> ids = Stream.of(sId, cId).sorted().collect(Collectors.toList());
            Client first = clientRepository.findByIdForUpdate(ids.get(0)).orElseThrow(() ->
                    new BusinessException("Client not found"));
            Client second = clientRepository.findByIdForUpdate(ids.get(1)).orElseThrow(() ->
                    new BusinessException("Client not found"));
            Client supplier = first.getId().equals(sId) ? first : second;
            Client consumer = first.getId().equals(cId) ? first : second;

            // 4. Клиенты должны быть активны в момент обработки
            if (!supplier.isActive() || !consumer.isActive()) {
                throw new BusinessException("Cannot create order for inactive client");
            }

            // 5. Проверка дубликата заказа
            boolean exists = orderRepository.existsDuplicateOrder(
                    req.getTitle(), supplier.getId(), consumer.getId(), req.getPrice());
            if (exists) {
                throw new BusinessException("Duplicate order detected");
            }

            // 6. Проверка будущей выгоды
            BigDecimal supplierCurrent = orderRepository.sumRevenueAsSupplier(supplier)
                    .subtract(orderRepository.sumCostAsConsumer(supplier));
            BigDecimal consumerCurrent = orderRepository.sumRevenueAsSupplier(consumer)
                    .subtract(orderRepository.sumCostAsConsumer(consumer));

            BigDecimal supplierFuture = supplierCurrent.add(req.getPrice());
            BigDecimal consumerFuture = consumerCurrent.subtract(req.getPrice());

            if (supplierFuture.compareTo(new BigDecimal("-1000")) < 0 ||
                    consumerFuture.compareTo(new BigDecimal("-1000")) < 0) {
                throw new BusinessException("Creating this order would make a client's total profit < -1000");
            }

            // 7. Эмуляция задержки обработки 1..10 сек
            try {
                Thread.sleep((1 + random.nextInt(10)) * 1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // 8. Повторная проверка активности клиента (вдруг его отключили во время сна)
            if (!supplier.isActive() || !consumer.isActive()) {
                throw new BusinessException("Client became inactive during processing");
            }

            // 9. Сохраняем заказ
            try {
                OrderEntity order = new OrderEntity();
                order.setTitle(req.getTitle());
                order.setSupplier(supplier);
                order.setConsumer(consumer);
                order.setPrice(req.getPrice());
                order.setProcessingStart(Instant.now());
                order.setProcessingEnd(Instant.now());

                order = orderRepository.save(order);
                log.debug("Saved order with ID: {}", order.getId());
                return order;
            } catch (DataIntegrityViolationException e) {
                // Сработает если БД поймает дубликат по unique index
                throw new BusinessException("Duplicate order detected (DB constraint)");
            }

    }

}
