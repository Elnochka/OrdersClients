package orders.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import orders.dto.ScenarioOrderResult;
import orders.service.ScenarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/scenarios")
@RequiredArgsConstructor
public class ScenarioController {
    private final ScenarioService scenarioService;

    @PostMapping("/duplicate-orders")
    @Operation(summary = "перевірка на дублікати")
    public ResponseEntity<List<ScenarioOrderResult>> runDuplicateOrders(@RequestParam UUID supplierId,
                                                                        @RequestParam UUID consumerId,
                                                                        @RequestParam(defaultValue = "3") int n) {
        return ResponseEntity.ok(scenarioService.scenarioDuplicateOrders(supplierId,
                        consumerId, n).stream().toList());

    }

    @PostMapping("/decreasing-price-changing-suppliers")
    @Operation(summary = "декілька замовлень зі знижувальною ціною")
    public ResponseEntity<List<ScenarioOrderResult>> decreasingPriceChangingSuppliers(
            @RequestParam UUID consumerId,
            @RequestParam(defaultValue = "11") int n,
            @RequestBody List<UUID> supplierIds) {

        return ResponseEntity.ok(scenarioService.
                scenarioDecreasingPriceWithChangingSupplier(consumerId,
                        supplierIds, n).stream().toList());

    }

    @PostMapping("/deactivation-changing-suppliers")
    @Operation(summary = "деіклька замовлень, у середині — деактівація кліента")
    public ResponseEntity<List<ScenarioOrderResult>> deactivationChangingSuppliers(
            @RequestParam UUID consumerId,
            @RequestParam(defaultValue = "10") int n,
            @RequestBody List<UUID> supplierIds) {
        return ResponseEntity.ok(scenarioService.
                scenarioClientDeactivationWithChangingSupplier(consumerId,
                        supplierIds, n).stream().toList());

    }

}
