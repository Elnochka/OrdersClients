package orders.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders",
        uniqueConstraints = @UniqueConstraint(name = "uk_order_business_key",
                columnNames = {"title","supplier_id","consumer_id"}))

public class OrderEntity {
    @Id
    @GeneratedValue
    private UUID id;
    @Column(nullable = false)
    private String title;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Client supplier;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "consumer_id", nullable = false)
    private Client consumer;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;
    private Instant processingStart;
    private Instant processingEnd;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    @PrePersist public void prePersist(){ createdAt = Instant.now(); }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Client getSupplier() { return supplier; }
    public void setSupplier(Client supplier) { this.supplier = supplier; }
    public Client getConsumer() { return consumer; }
    public void setConsumer(Client consumer) { this.consumer = consumer; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Instant getProcessingStart() { return processingStart; }
    public void setProcessingStart(Instant processingStart) { this.processingStart = processingStart; }
    public Instant getProcessingEnd() { return processingEnd; }
    public void setProcessingEnd(Instant processingEnd) { this.processingEnd = processingEnd; }
    public Instant getCreatedAt() { return createdAt; }

}
