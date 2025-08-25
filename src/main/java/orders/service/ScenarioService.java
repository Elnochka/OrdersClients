package orders.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import orders.dto.ClientDto;
import orders.dto.CreateClientRequest;
import orders.dto.CreateOrderRequest;
import orders.dto.ScenarioOrderResult;
import orders.entity.Client;
import orders.repository.ClientRepository;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScenarioService {

    private final OrderService orderService;
    private final ClientRepository clientRepository;
    private final ClientService service;
    private final Random random = new Random();

    public List<ScenarioOrderResult> scenarioDuplicateOrders(UUID supplierId,
                                                             UUID consumerId, int n) {

        if(consumerId.equals(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"))){
            consumerId = getListUUID(1).get(0);
        }

        if(supplierId.equals(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"))){
            supplierId = getListUUID(1).get(0);
        }

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

        if(consumerId.equals(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"))) {
            consumerId = getListUUID(1).get(0);
        }

        if(supplierIds.get(0).equals(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"))) {
            supplierIds = getListUUID(10);
        }

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

        if(consumerId.equals(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"))) {
            consumerId = getListUUID(1).get(0);
        }

        if(supplierIds.get(0).equals(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"))) {
            supplierIds = getListUUID(n);
        }

        List<ScenarioOrderResult> scenarioResult = new ArrayList<>();
        Client consumer = clientRepository.findById(consumerId).orElseThrow();

        for (int i = 1; i <= n; i++) {
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

    private List<UUID> getListUUID(int n) {
        List<UUID> uuidList = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            ClientDto clientDto = getClient(n);
            uuidList.add(clientDto.getId());
        }
        return uuidList;
    }

    private ClientDto getClient(int n){
        CreateClientRequest req = new CreateClientRequest();
        int nRandom = (n + random.nextInt(1000));
        req.setName("clientDuplicateName" + nRandom);
        req.setAddress("addressDuplicate" + nRandom);
        req.setEmail("emailDuplicate" + nRandom + "@ukr.net");
        ClientDto created = service.createClient(req);
        return created;
    }

}

