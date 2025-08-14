package orders.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public class CreateOrderRequest {
    @NotBlank
    private String title;
    @NotNull
    private UUID supplierId;
    @NotNull
    private UUID consumerId;
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal price;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public UUID getSupplierId() { return supplierId; }
    public void setSupplierId(UUID supplierId) { this.supplierId = supplierId; }
    public UUID getConsumerId() { return consumerId; }
    public void setConsumerId(UUID consumerId) { this.consumerId = consumerId; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

}
