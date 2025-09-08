package co.com.bancolombia.api.enums;

import java.util.UUID;

public enum RolEnum {
    ADMIN(UUID.fromString("80e86d27-20a4-44be-b90d-44eeb378d409")),
    ASSESSOR(UUID.fromString("3a371249-a1f0-4eb3-b06c-5a670ab6eca9")),
    CLIENT(UUID.fromString("b71ed6c9-1dd9-4c14-8a4a-fe06166d5cdb"));
    
    private final UUID id;

    RolEnum(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }
}