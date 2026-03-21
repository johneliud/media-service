package io.github.johneliud.media_service.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "active_order_products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActiveOrderProduct {
    @Id
    private String orderId;
    private List<String> productIds;
}
