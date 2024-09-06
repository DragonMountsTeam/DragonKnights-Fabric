package net.dragonmounts.api;

import net.dragonmounts.registry.DragonType;

public interface DragonTypified {
    DragonType getDragonType();

    interface Mutable extends DragonTypified {
        void setDragonType(DragonType type);
    }
}