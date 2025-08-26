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

        List<ScenarioOrderResult> listScenarioOrderResult = new ArrayList<>();

        String title = "ScenarioOrder";
        BigDecimal price = BigDecimal.ONE;        
        CreateOrderRequest req = createdRequest(title, consumerId,
                supplierId, price);

        IntStream.range(0, n).forEach(i -> {
            ScenarioOrderResult scenarioOrderResult = createdResult(req);
            listScenarioOrderResult.add(scenarioOrderResult);

        });

        return listScenarioOrderResult;
    }


    public List<ScenarioOrderResult> scenarioDecreasingPriceWithChangingSupplier(
            UUID consumerId, List<UUID> supplierIds, int n) {

        if(consumerId.equals(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"))) {
            consumerId = getListUUID(1).get(0);
        }

        if(supplierIds.get(0).equals(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"))) {
            supplierIds = getListUUID(n);
        }

        List<ScenarioOrderResult> listScenarioOrderResult = new ArrayList<>();

        String title = "DecreasingOrder";
        BigDecimal price = BigDecimal.valueOf(970);
        UUID supplierId = supplierIds.get(0);
        CreateOrderRequest req = createdRequest(title, consumerId,
                supplierId, price);

        ScenarioOrderResult scenarioOrderResult = createdResult(req);
        listScenarioOrderResult.add(scenarioOrderResult);
        
        int index = 1;
        for (int priceId = 100; priceId >= 10; priceId -= 10) {
            
            title = "DecreasingOrder" + index;
            price = BigDecimal.valueOf(priceId);
            supplierId = supplierIds.get(index % supplierIds.size());
            req = createdRequest(title, consumerId,
                    supplierId, price);

            scenarioOrderResult = createdResult(req);
            listScenarioOrderResult.add(scenarioOrderResult);

            index++;
        }
    return listScenarioOrderResult;

    }

    public List<ScenarioOrderResult> scenarioClientDeactivationWithChangingSupplier(
            UUID consumerId, List<UUID> supplierIds, int n) {

        if(consumerId.equals(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"))) {
            consumerId = getListUUID(1).get(0);
        }

        if(supplierIds.get(0).equals(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"))) {
            supplierIds = getListUUID(n);
        }

        List<ScenarioOrderResult> listScenarioOrderResult = new ArrayList<>();
        Client consumer = clientRepository.findById(consumerId).orElseThrow();

        for (int i = 1; i <= n; i++) {
            
            String title = "Order" + i; 
            BigDecimal price = BigDecimal.valueOf(10 + i);
            UUID supplierId = supplierIds.get(i % supplierIds.size());
            CreateOrderRequest req = createdRequest(title, consumerId, 
                    supplierId, price);
            
            ScenarioOrderResult scenarioOrderResult = createdResult(req);
            listScenarioOrderResult.add(scenarioOrderResult);

            // В середине последовательности делаем клиента неактивным
            if (i == n / 2) {
                consumer.setActive(false);
                clientRepository.save(consumer);
            }
        }

        return listScenarioOrderResult;

    }

    private CreateOrderRequest createdRequest(String title, UUID consumerId,
                                              UUID supplierId, BigDecimal price) {

        CreateOrderRequest req = new CreateOrderRequest();
        req.setTitle(title);
        req.setPrice(price);
        req.setConsumerId(consumerId);
        req.setSupplierId(supplierId);
        return req;

    }

    private ScenarioOrderResult createdResult(CreateOrderRequest req) {

        ScenarioOrderResult result = new ScenarioOrderResult();
        result.setTitle(req.getTitle());
        result.setPrice(req.getPrice());

        try {
            var order = orderService.createOrder(req);
            result.setSuccess(true);
            result.setOrderId(order.getId());
            result.setMessage("Created successfully");

        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage(e.getMessage());

        }
        return result;

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
        req.setName("clientNameAuto" + nRandom);
        req.setAddress("addressAuto" + nRandom);
        req.setEmail("emailAuto" + nRandom + "@ukr.net");
        ClientDto client = service.createClient(req);
        return client;
    }

}

