package io.github.johneliud.media_service.repositories;

import io.github.johneliud.media_service.models.ActiveOrderProduct;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ActiveOrderProductRepository extends MongoRepository<ActiveOrderProduct, String> {
    boolean existsByProductIdsContaining(String productId);
}
