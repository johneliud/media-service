package io.github.johneliud.media_service.services;

import io.github.johneliud.media_service.dto.OrderItemEvent;
import io.github.johneliud.media_service.dto.OrderPlacedEvent;
import io.github.johneliud.media_service.dto.OrderStatusChangedEvent;
import io.github.johneliud.media_service.models.ActiveOrderProduct;
import io.github.johneliud.media_service.repositories.ActiveOrderProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderEventConsumerTest {

    @Mock
    private ActiveOrderProductRepository activeOrderProductRepository;

    @InjectMocks
    private OrderEventConsumer orderEventConsumer;

    @Test
    void handleOrderPlaced_savesRecord() {
        OrderItemEvent item1 = new OrderItemEvent("product1", "Product One", new BigDecimal("10.00"), 2);
        OrderItemEvent item2 = new OrderItemEvent("product2", "Product Two", new BigDecimal("5.00"), 1);
        OrderPlacedEvent event = new OrderPlacedEvent();
        event.setOrderId("order123");
        event.setUserId("user123");
        event.setSellerId("seller123");
        event.setItems(List.of(item1, item2));
        event.setTotalAmount(new BigDecimal("25.00"));

        orderEventConsumer.handleOrderPlaced(event);

        ArgumentCaptor<ActiveOrderProduct> captor = ArgumentCaptor.forClass(ActiveOrderProduct.class);
        verify(activeOrderProductRepository).save(captor.capture());
        ActiveOrderProduct saved = captor.getValue();
        assertEquals("order123", saved.getOrderId());
        assertTrue(saved.getProductIds().contains("product1"));
        assertTrue(saved.getProductIds().contains("product2"));
        assertEquals(2, saved.getProductIds().size());
    }

    @Test
    void handleOrderStatusChanged_cancelledOrder_deletesRecord() {
        OrderStatusChangedEvent event = new OrderStatusChangedEvent(
                "order123", "user123", "seller123", "PENDING", "CANCELLED");

        orderEventConsumer.handleOrderStatusChanged(event);

        verify(activeOrderProductRepository).deleteById("order123");
    }

    @Test
    void handleOrderStatusChanged_deliveredOrder_deletesRecord() {
        OrderStatusChangedEvent event = new OrderStatusChangedEvent(
                "order123", "user123", "seller123", "SHIPPED", "DELIVERED");

        orderEventConsumer.handleOrderStatusChanged(event);

        verify(activeOrderProductRepository).deleteById("order123");
    }

    @Test
    void handleOrderStatusChanged_pendingStatus_doesNotDelete() {
        OrderStatusChangedEvent event = new OrderStatusChangedEvent(
                "order123", "user123", "seller123", "PENDING", "CONFIRMED");

        orderEventConsumer.handleOrderStatusChanged(event);

        verify(activeOrderProductRepository, never()).deleteById(any());
    }
}
