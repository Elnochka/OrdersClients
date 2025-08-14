package orders.controller;

import lombok.extern.slf4j.Slf4j;
import orders.dto.CreateOrderRequest;
import orders.dto.OrderDto;
import orders.entity.OrderEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import orders.repository.ClientRepository;
import orders.repository.OrderRepository;
import orders.service.OrderService;

import java.net.URI;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders")

public class OrderController {
    private final OrderService orderService;
    private final ClientRepository clientRepo;
    private final OrderRepository orderRepo;

    public OrderController(OrderService orderService, ClientRepository clientRepo, OrderRepository orderRepo) {
        this.orderService = orderService; this.clientRepo = clientRepo; this.orderRepo = orderRepo;
    }

    @Operation(summary = "Створюємо замовлення (з емуляцією затримки збереження)")
    @PostMapping
    public ResponseEntity<OrderDto> create(@Valid @RequestBody CreateOrderRequest req) {
        log.info("POST /api/orders - Creating order: {}", req.getTitle());
        OrderEntity o = orderService.createOrder(req);
        OrderDto dto = toDto(o);
        log.debug("Order created with id={}", dto.getId());
        return ResponseEntity.created(URI.create("/api/orders/" + o.getId())).body(dto);
    }

    @Operation(summary = "Отримати замовлення по ID")
    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> get(@PathVariable UUID id) {
        log.info("GET /api/orders/{}", id);
        OrderEntity o = orderRepo.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
        return ResponseEntity.ok(toDto(o));
    }

    @Operation(summary = "Список замовлень клієнта-постачальника")
    @GetMapping("/by-supplier/{clientId}")
    public ResponseEntity<java.util.List<OrderDto>> bySupplier(@PathVariable UUID clientId) {
        log.info("GET /api/orders/client/(by supplier){}", clientId);
        var client = clientRepo.findById(clientId).orElseThrow();
        return ResponseEntity.ok(orderRepo.findBySupplier(client).stream().map(this::toDto).toList());
    }

    @Operation(summary = "Список замовлень клієнта-покупця")
    @GetMapping("/by-consumer/{clientId}")
    public ResponseEntity<java.util.List<OrderDto>> byConsumer(@PathVariable UUID clientId) {
        log.info("GET /api/orders/client/(by consumer){}", clientId);
        var client = clientRepo.findById(clientId).orElseThrow();
        return ResponseEntity.ok(orderRepo.findByConsumer(client).stream().map(this::toDto).toList());
    }

    private OrderDto toDto(OrderEntity e){
        OrderDto dto = new OrderDto();
        dto.setId(e.getId()); dto.setTitle(e.getTitle());
        dto.setSupplierId(e.getSupplier().getId()); dto.setConsumerId(e.getConsumer().getId());
        dto.setPrice(e.getPrice()); dto.setProcessingStart(e.getProcessingStart()); dto.setProcessingEnd(e.getProcessingEnd());
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }

}
