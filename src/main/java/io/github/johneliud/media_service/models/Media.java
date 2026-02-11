package io.github.johneliud.media_service.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "media")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Media {
    @Id
    private String id;
    
    private String imagePath;
    
    @Indexed
    private String productId;
}
