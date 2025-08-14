package orders.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class OrderDto {
    private UUID id;
    private String title;
    private UUID supplierId;
    private UUID consumerId;
    private BigDecimal price;
    private Instant processingStart;
    private Instant processingEnd;
    private Instant createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public UUID getSupplierId() { return supplierId; }
    public void setSupplierId(UUID supplierId) { this.supplierId = supplierId; }
    public UUID getConsumerId() { return consumerId; }
    public void setConsumerId(UUID consumerId) { this.consumerId = consumerId; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Instant getProcessingStart() { return processingStart; }
    public void setProcessingStart(Instant processingStart) { this.processingStart = processingStart; }
    public Instant getProcessingEnd() { return processingEnd; }
    public void setProcessingEnd(Instant processingEnd) { this.processingEnd = processingEnd; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

}
