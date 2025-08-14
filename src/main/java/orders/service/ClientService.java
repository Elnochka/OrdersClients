package orders.service;

import lombok.extern.slf4j.Slf4j;
import orders.dto.ClientDto;
import orders.dto.CreateClientRequest;
import orders.dto.UpdateClientRequest;
import orders.entity.Client;
import orders.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import orders.repository.ClientRepository;
import orders.repository.OrderRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ClientService {
    private final ClientRepository clientRepo;
    private final OrderRepository orderRepo;

    public ClientService(ClientRepository clientRepo, OrderRepository orderRepo) {
        this.clientRepo = clientRepo;
        this.orderRepo = orderRepo;
    }

    public ClientDto createClient(CreateClientRequest req) {
        log.info("Saving new client: {}", req.getName());
        clientRepo.findByEmailIgnoreCase(req.getEmail()).ifPresent(c -> { throw new BusinessException("Email already exists"); });
        Client client = new Client();
        client.setName(req.getName());
        client.setEmail(req.getEmail().toLowerCase());
        client.setAddress(req.getAddress());
        client.setActive(true);
        log.debug("Saved client with ID: {}", client.getId());
        return toDto(clientRepo.save(client));
    }

    public ClientDto getClient(UUID id) {
        log.info("Fetching client by ID: {}", id);
        return toDto(clientRepo.findById(id).orElseThrow(() -> {
            log.warn("Client with id={} not found", id);
            return new BusinessException("Client not found");
        }));
    }

    public ClientDto updateClient(UUID id, UpdateClientRequest req) {
        Client client = clientRepo.findById(id).orElseThrow(() -> new BusinessException("Client not found"));
        if (!client.getEmail().equalsIgnoreCase(req.getEmail())) {
            clientRepo.findByEmailIgnoreCase(req.getEmail()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) throw new BusinessException("Email already in use");
            });
        }
        client.setName(req.getName());
        client.setEmail(req.getEmail().toLowerCase());
        client.setAddress(req.getAddress());
        return toDto(clientRepo.save(client));
    }

    @Transactional
    public void deactivateClient(UUID id) {
        Client client = clientRepo.findById(id).orElseThrow(() -> new BusinessException("Client not found"));
        if (!client.isActive()) return;
        client.setActive(false);
        client.setDeactivatedAt(Instant.now());
        clientRepo.save(client);
    }

    public List<ClientDto> searchClients(String keyword) {
        log.info("Searching clients with keyword: {}", keyword);
        if (keyword == null || keyword.trim().length() < 3) throw new BusinessException("Search keyword must be at least 3 characters");
        return clientRepo.searchByKeyword(keyword.trim().toLowerCase()).stream().map(this::toDto).collect(Collectors.toList());
    }

    public java.math.BigDecimal getClientProfit(UUID id) {
        Client c = clientRepo.findById(id).orElseThrow(() -> new BusinessException("Client not found"));
        java.math.BigDecimal revenue = orderRepo.sumRevenueAsSupplier(c);
        java.math.BigDecimal costs = orderRepo.sumCostAsConsumer(c);
        return revenue.subtract(costs == null ? java.math.BigDecimal.ZERO : costs);
    }

    public List<ClientDto> findClientsByProfitRange(java.math.BigDecimal min, java.math.BigDecimal max) {
        List<Client> all = clientRepo.findAll();
        return all.stream().filter(c -> {
            java.math.BigDecimal profit = orderRepo.sumRevenueAsSupplier(c).subtract(orderRepo.sumCostAsConsumer(c));
            return profit.compareTo(min) >= 0 && profit.compareTo(max) <= 0;
        }).map(this::toDto).collect(Collectors.toList());
    }

    private ClientDto toDto(Client c) {
        ClientDto dto = new ClientDto();
        dto.setId(c.getId()); dto.setName(c.getName()); dto.setEmail(c.getEmail()); dto.setAddress(c.getAddress());
        dto.setActive(c.isActive()); dto.setDeactivatedAt(c.getDeactivatedAt());
        return dto;
    }

}
