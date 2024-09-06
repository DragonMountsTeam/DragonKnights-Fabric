package net.dragonmounts.init;

import io.netty.buffer.ByteBuf;
import net.dragonmounts.client.DragonMountsClient;
import net.dragonmounts.entity.dragon.TameableDragonEntity;
import net.dragonmounts.inventory.DragonCoreHandler;
import net.dragonmounts.inventory.DragonInventoryHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;

import static net.dragonmounts.DragonMounts.makeId;

public class DMScreenHandlers {
    public static final ExtendedScreenHandlerType<DragonCoreHandler, BlockPos> DRAGON_CORE = register("dragon_core", DragonCoreHandler::new, BlockPos.STREAM_CODEC);
    public static final ExtendedScreenHandlerType<DragonInventoryHandler, TameableDragonEntity> DRAGON_INVENTORY = register("dragon_inventory", DragonInventoryHandler::new, new StreamCodec<ByteBuf, TameableDragonEntity>() {
        public @NotNull TameableDragonEntity decode(ByteBuf buffer) {
            if (DragonMountsClient.getLevel().getEntity(VarInt.read(buffer)) instanceof TameableDragonEntity dragon) {
                return dragon;
            }
            throw new NullPointerException();
        }

        public void encode(ByteBuf buffer, TameableDragonEntity dragon) {
            VarInt.write(buffer, dragon.getId());
        }
    });

    static <T extends AbstractContainerMenu, D> ExtendedScreenHandlerType<T, D> register(
            String identified,
            ExtendedScreenHandlerType.ExtendedFactory<T, D> factory,
            StreamCodec<? super RegistryFriendlyByteBuf, D> codec
    ) {
        return Registry.register(BuiltInRegistries.MENU, makeId(identified), new ExtendedScreenHandlerType<>(factory, codec));
    }

    public static void init() {}
}
