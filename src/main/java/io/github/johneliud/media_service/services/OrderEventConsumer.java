package io.github.johneliud.media_service.services;

import io.github.johneliud.media_service.dto.OrderItemEvent;
import io.github.johneliud.media_service.dto.OrderPlacedEvent;
import io.github.johneliud.media_service.dto.OrderStatusChangedEvent;
import io.github.johneliud.media_service.models.ActiveOrderProduct;
import io.github.johneliud.media_service.repositories.ActiveOrderProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final ActiveOrderProductRepository activeOrderProductRepository;

    @KafkaListener(topics = "order-placed", groupId = "media-service")
    public void handleOrderPlaced(OrderPlacedEvent event) {
        List<String> productIds = event.getItems().stream()
                .map(OrderItemEvent::getProductId)
                .distinct()
                .collect(Collectors.toList());
        activeOrderProductRepository.save(new ActiveOrderProduct(event.getOrderId(), productIds));
        log.info("Tracked active order: orderId={}, products={}", event.getOrderId(), productIds);
    }

    @KafkaListener(topics = "order-status-changed", groupId = "media-service")
    public void handleOrderStatusChanged(OrderStatusChangedEvent event) {
        String status = event.getNewStatus();
        if ("CANCELLED".equals(status) || "DELIVERED".equals(status)) {
            activeOrderProductRepository.deleteById(event.getOrderId());
            log.info("Released order protection: orderId={}, status={}", event.getOrderId(), status);
        }
    }
}
