package io.github.johneliud.media_service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
public class OrderPlacedEvent {
    private String orderId;
    private String userId;
    private String sellerId;
    private List<OrderItemEvent> items;
    private BigDecimal totalAmount;
}
