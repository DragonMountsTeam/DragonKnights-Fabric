package net.dragonmounts.init;

import net.dragonmounts.entity.dragon.ServerDragonEntity;
import net.dragonmounts.registry.FluteCommand;

import static net.dragonmounts.DragonMounts.makeId;

public class FluteCommands {
    public static final FluteCommand SIT = new FluteCommand(makeId("sit"), makeId("flute_command/sit")) {
        @Override
        public void accept(ServerDragonEntity dragon) {
            dragon.setOrderedToSit(true);
        }
    };
    public static final FluteCommand STAND = new FluteCommand(makeId("stand"), makeId("flute_command/stand")) {
        @Override
        public void accept(ServerDragonEntity dragon) {
            dragon.setOrderedToSit(false);
        }
    };
    public static final FluteCommand GATHER = new FluteCommand(makeId("gather"), makeId("flute_command/gather")) {
        @Override
        public void accept(ServerDragonEntity dragon) {
            dragon.setOrderedToSit(false);
            //TODO: impl
        }
    };

    public static void init() {}
}
