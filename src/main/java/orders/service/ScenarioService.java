package orders.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import orders.dto.CreateOrderRequest;
import orders.dto.ScenarioOrderResult;
import orders.entity.Client;
import orders.repository.ClientRepository;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScenarioService {

    private final OrderService orderService;
    private final ClientRepository clientRepository;

    public List<ScenarioOrderResult> scenarioDuplicateOrders(UUID supplierId,
                                                             UUID consumerId, int n) {

        List<ScenarioOrderResult> scenarioResult = new ArrayList<>();
        CreateOrderRequest req = new CreateOrderRequest();

        req.setTitle("ScenarioOrder");
        req.setPrice(BigDecimal.ONE);
        req.setSupplierId(supplierId);
        req.setConsumerId(consumerId);

        IntStream.range(0, n + 1).forEach(i -> {
            ScenarioOrderResult result = new ScenarioOrderResult();
            result.setTitle(req.getTitle());
            result.setPrice(req.getPrice());

            try {
                var order = orderService.createOrder(req);
                result.setSuccess(true);
                result.setOrderId(order.getId());
                result.setMessage("Created successfully");
                scenarioResult.add(result);
            } catch (Exception e) {
                result.setSuccess(false);
                result.setMessage(e.getMessage());
                scenarioResult.add(result);
            }
        });

        return scenarioResult;
    }


    public List<ScenarioOrderResult> scenarioDecreasingPriceWithChangingSupplier(
            UUID consumerId, List<UUID> supplierIds) {

        List<ScenarioOrderResult> scenarioResult = new ArrayList<>();

        int index = 0;
        for (int price = 100; price >= 10; price -= 10) {
            CreateOrderRequest req = new CreateOrderRequest();
            req.setTitle("DecreasingOrder");
            req.setPrice(BigDecimal.valueOf(price));
            req.setConsumerId(consumerId);

            // Выбираем нового поставщика
            UUID supplierId = supplierIds.get(index % supplierIds.size());
            req.setSupplierId(supplierId);

            ScenarioOrderResult result = new ScenarioOrderResult();
            result.setTitle(req.getTitle());
            result.setPrice(req.getPrice());

            try {
                var order = orderService.createOrder(req);
                result.setSuccess(true);
                result.setOrderId(order.getId());
                result.setMessage("Created successfully");
                scenarioResult.add(result);
            } catch (Exception e) {
                result.setSuccess(false);
                result.setMessage(e.getMessage());
                scenarioResult.add(result);
            }
            index++;
        }
    return scenarioResult;

    }

    public List<ScenarioOrderResult> scenarioClientDeactivationWithChangingSupplier(
            UUID consumerId, List<UUID> supplierIds, int n) {

        List<ScenarioOrderResult> scenarioResult = new ArrayList<>();
        Client consumer = clientRepository.findById(consumerId).orElseThrow();

        for (int i = 0; i <= n; i++) {
            CreateOrderRequest req = new CreateOrderRequest();
            req.setTitle("Order" + i);
            req.setPrice(BigDecimal.valueOf(10 + i));
            req.setConsumerId(consumerId);

            // Новый supplier для каждого заказа
            UUID supplierId = supplierIds.get(i % supplierIds.size());
            req.setSupplierId(supplierId);

            ScenarioOrderResult result = new ScenarioOrderResult();
            result.setTitle(req.getTitle());
            result.setPrice(req.getPrice());

            try {
                var order = orderService.createOrder(req);
                result.setSuccess(true);
                result.setOrderId(order.getId());
                result.setMessage("Created successfully");
                scenarioResult.add(result);
            } catch (Exception e) {
                result.setSuccess(false);
                result.setMessage(e.getMessage());
                scenarioResult.add(result);
            }

            // В середине последовательности делаем клиента неактивным
            if (i == n / 2) {
                consumer.setActive(false);
                clientRepository.save(consumer);
            }
        }

        return scenarioResult;

    }
}

