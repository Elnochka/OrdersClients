package orders.controller;

import lombok.extern.slf4j.Slf4j;
import orders.dto.ClientDto;
import orders.dto.CreateClientRequest;
import orders.dto.UpdateClientRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import orders.service.ClientService;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.UUID;



@Slf4j
@RestController
@RequestMapping("/api/clients")
@Tag(name = "Clients")

public class ClientController {
    private final ClientService service;
    public ClientController(ClientService service) { this.service = service; }

    @Operation(summary = "Створюємо клієнта")
    @PostMapping
    public ResponseEntity<ClientDto> create(@Valid @RequestBody CreateClientRequest req) {
        log.info("POST /api/clients - Creating client: {}", req.getName());
        ClientDto created = service.createClient(req);
        log.debug("Client created with id={}", created.getId());
        return ResponseEntity.created(URI.create("/api/clients/" + created.getId()))
                .body(created);
    }

    @Operation(summary = "Отримуємо клєнта по ID")
    @GetMapping("/{id}")
    public ResponseEntity<ClientDto> get(@PathVariable UUID id) {
        log.info("GET /api/clients/{}", id);
        return ResponseEntity.ok(service.getClient(id));
    }

    @Operation(summary = "Оновлюємо клієнта")
    @PutMapping("/{id}")
    public ResponseEntity<ClientDto> update(@PathVariable UUID id,
                                            @Valid @RequestBody UpdateClientRequest req) {
        log.info("Update /api/clients/{}", id);
        return ResponseEntity.ok(service.updateClient(id, req));
    }

    @Operation(summary = "Деактивуємо клієнта")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        log.info("Patch /api/clients - Deactivate client: {}", id);
        service.deactivateClient(id);
        log.debug("Client deactivated with id={}", id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Пошук клієнтів за допомогою ключового слова (>=3 симв)")
    @GetMapping("/search")
    public ResponseEntity<List<ClientDto>> search(@RequestParam("q") String q) {
        log.info("GET /api/clients - Searching with keyword={}", q);
        return ResponseEntity.ok(service.searchClients(q));
    }

    @Operation(summary = "Сумарна вигода клієнта")
    @GetMapping("/{id}/profit")
    public ResponseEntity<BigDecimal> profit(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getClientProfit(id));
    }

    @Operation(summary = "Пошук клієнтів у діапазоні вигоди")
    @GetMapping("/by-profit")
    public ResponseEntity<List<ClientDto>> byProfit(@RequestParam("min") BigDecimal min,
                                                    @RequestParam("max") BigDecimal max) {
        return ResponseEntity.ok(service.findClientsByProfitRange(min, max));
    }

}
