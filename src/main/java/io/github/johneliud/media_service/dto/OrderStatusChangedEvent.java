package io.github.johneliud.media_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusChangedEvent {
    private String orderId;
    private String userId;
    private String sellerId;
    private String oldStatus;
    private String newStatus;
}
