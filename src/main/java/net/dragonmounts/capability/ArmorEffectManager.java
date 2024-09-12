package net.dragonmounts.capability;

import net.dragonmounts.api.IArmorEffect;
import net.dragonmounts.api.IArmorEffectSource;
import net.dragonmounts.registry.CooldownCategory;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

import static java.lang.System.arraycopy;
import static java.util.Arrays.fill;
import static net.dragonmounts.DragonMounts.MOD_ID;
import static net.dragonmounts.network.DMPackets.INIT_COOLDOWN_PACKET_ID;
import static net.dragonmounts.network.DMPackets.SYNC_COOLDOWN_PACKET_ID;

public final class ArmorEffectManager implements IArmorEffectManager {
    public static final String DATA_PARAMETER_KEY = MOD_ID + ":armor_effect_manager";
    private static ArmorEffectManager LOCAL_MANAGER = null;
    public static final int INITIAL_COOLDOWN_SIZE = 8;
    public static final int INITIAL_LEVEL_SIZE = 5;

    public static void onPlayerClone(PlayerEntity player, PlayerEntity priorPlayer) {
        ArmorEffectManager manager = ((IArmorEffectManager.Provider) player).dragonmounts$getManager();
        ArmorEffectManager priorManager = ((IArmorEffectManager.Provider) priorPlayer).dragonmounts$getManager();
        manager.cdRef = priorManager.cdRef;
        manager.cdKey = priorManager.cdKey;
        manager.cdDat = priorManager.cdDat;
        manager.cdMask = priorManager.cdMask;
        manager.cdN = priorManager.cdN;
    }

    public final PlayerEntity player;
    private int[] cdRef;
    private int[] cdKey;
    private int[] cdDat;
    private int cdMask;
    private int cdN;
    private int[] lvlRef;//active effects
    private IArmorEffect[] lvlKey;//all effects
    private int[] lvlDat;
    private int lvlSize;
    private int lvlN;
    private int activeN;

    public ArmorEffectManager(PlayerEntity player) {
        this.player = player;
        if (player.isMainPlayer()) {
            LOCAL_MANAGER = this;
        }
        this.cdMask = INITIAL_COOLDOWN_SIZE - 1;
        this.cdRef = new int[INITIAL_COOLDOWN_SIZE];
        fill(this.cdKey = new int[INITIAL_COOLDOWN_SIZE], -1);
        this.cdDat = new int[INITIAL_COOLDOWN_SIZE];
        this.lvlSize = INITIAL_LEVEL_SIZE;
        this.lvlRef = new int[INITIAL_LEVEL_SIZE];
        this.lvlKey = new IArmorEffect[INITIAL_LEVEL_SIZE];
        this.lvlDat = new int[INITIAL_LEVEL_SIZE];
    }

    public static ArmorEffectManager getLocal() {
        return LOCAL_MANAGER;
    }

    public static int getLocalCooldown(CooldownCategory category) {
        return LOCAL_MANAGER == null ? 0 : LOCAL_MANAGER.getCooldown(category);
    }

    public static void init(int[] data) {
        if (LOCAL_MANAGER == null) return;//?
        LOCAL_MANAGER.cdN = 0;
        final int length = data.length >> 1;
        if (length > LOCAL_MANAGER.cdMask) {
            int size = LOCAL_MANAGER.cdRef.length << 1;
            while (length >= size) size <<= 1;
            LOCAL_MANAGER.cdMask = size - 1;
            LOCAL_MANAGER.cdRef = new int[size];
            fill(LOCAL_MANAGER.cdKey = new int[size], -1);
            LOCAL_MANAGER.cdDat = new int[size];
        } else {
            fill(LOCAL_MANAGER.cdKey, -1);
        }
        for (int i = 0, j = 0, k; i < length; ++i) {
            if ((k = data[i++]) >= 0) {
                j = LOCAL_MANAGER.setCdImpl(k, data[i], j);
            }
        }
    }

    private void reassign(final int pos, final int arg) {
        final int[] cdRef = this.cdRef, cdKey = this.cdKey, cdDat = this.cdDat;
        for (int i = this.cdN - 1, j, k, mask = this.cdMask; i > arg; --i) {
            if (((k = cdKey[j = cdRef[i]]) & mask) == pos) {
                cdRef[i] = pos;
                cdKey[pos] = k;
                cdDat[pos] = cdDat[j];
                this.reassign(j, i);
                return;
            }
        }
        cdKey[pos] = -1;//it is unnecessary to reset `this.cdDat[pos]`
    }

    private int setCdImpl(final int category, final int cooldown, int cursor) {
        final int[] cdRef = this.cdRef, cdKey = this.cdKey, cdDat = this.cdDat;
        int mask = this.cdMask, pos = category & mask;
        do {
            int key = cdKey[pos];
            if (key == -1) {
                if (cooldown > 0) {
                    cdRef[this.cdN++] = pos;
                    cdKey[pos] = category;
                    cdDat[pos] = cooldown;
                }
                return cursor == pos ? pos + 1 : cursor;
            } else if (key == category) {
                if (cooldown > 0) {
                    cdDat[pos] = cooldown;
                } else for (int i = 0; i < this.cdN; ++i) {
                    if (cdRef[i] == pos) {
                        arraycopy(cdRef, i + 1, cdRef, i, --this.cdN - i);
                        this.reassign(pos, i - 1);
                        return cursor == pos ? pos + 1 : cursor;
                    }
                }
                return cursor == pos ? pos + 1 : cursor;
            }
        } while ((pos = cursor++) <= mask);
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void setCooldown(final CooldownCategory category, final int cooldown) {
        final int id = category.id;
        if (id < 0) return;
        if (this.cdN == this.cdRef.length) {
            final int[] ref = this.cdRef, key = this.cdKey, dat = this.cdDat;
            final int n = this.cdN;
            int temp = n << 1;//temp: new array size
            this.cdMask = temp - 1;
            this.cdN = 0;
            this.cdRef = new int[temp];
            fill(this.cdKey = new int[temp], -1);
            this.cdDat = new int[temp];
            for (int i = temp = 0, j; i < n; ++i) {//temp: cursor
                temp = this.setCdImpl(key[j = ref[i]], dat[j], temp);
            }
            this.setCdImpl(id, cooldown, temp);
        } else {
            this.setCdImpl(id, cooldown, 0);
        }
        if (!this.player.world.isClient) {
            ServerPlayNetworking.send(
                    (ServerPlayerEntity) this.player,
                    SYNC_COOLDOWN_PACKET_ID,
                    PacketByteBufs.create().writeVarInt(id).writeVarInt(cooldown)
            );
        }
    }

    @Override
    public NbtCompound saveNBT() {
        final int[] cdRef = this.cdRef, cdKey = this.cdKey, cdDat = this.cdDat;
        NbtCompound tag = new NbtCompound();
        for (int i = 0, j, v; i < this.cdN; ++i) {
            if ((v = cdDat[j = cdRef[i]]) > 0) {
                CooldownCategory category = CooldownCategory.REGISTRY.get(cdKey[j]);
                if (category != null) {
                    tag.putInt(category.identifier.toString(), v);
                }
            }
        }
        return tag;
    }


    @Override
    public void readNBT(NbtCompound tag) {
        for (CooldownCategory category : CooldownCategory.REGISTRY) {
            String name = category.identifier.toString();
            if (tag.contains(name)) {
                if (this.cdN == this.cdRef.length) {
                    final int[] ref = this.cdRef, key = this.cdKey, dat = this.cdDat;
                    final int n = this.cdN;
                    int temp = n << 1;//temp: new array size
                    this.cdMask = temp - 1;
                    this.cdN = 0;
                    this.cdRef = new int[temp];
                    fill(this.cdKey = new int[temp], -1);
                    this.cdDat = new int[temp];
                    for (int i = temp = 0, j; i < n; ++i) {//temp: cursor
                        temp = this.setCdImpl(key[j = ref[i]], dat[j], temp);
                    }
                    this.setCdImpl(category.id, tag.getInt(name), temp);
                } else {
                    this.setCdImpl(category.id, tag.getInt(name), 0);
                }
            }
        }
    }

    @Override
    public void sendInitPacket() {
        final int n = this.cdN;
        if (n == 0) return;
        final int[] cdRef = this.cdRef, cdKey = this.cdKey, cdDat = this.cdDat;
        PacketByteBuf buffer = PacketByteBufs.create()
                .writeVarInt(n << 1);
        for (int i = 0, j, k; i < n; ++i) {
            if ((k = cdKey[j = cdRef[i]]) != -1) {
                buffer.writeVarInt(k).writeVarInt(cdDat[j]);
            }
        }
        ServerPlayNetworking.send((ServerPlayerEntity) this.player, INIT_COOLDOWN_PACKET_ID, buffer);
    }

    @Override
    public int getCooldown(final CooldownCategory category) {
        final int id = category.id;
        if (id < 0) return 0;
        int pos = id & this.cdMask, key = this.cdKey[pos];
        if (key == -1) return 0;
        if (key == id) return this.cdDat[pos];
        final int[] cdRef = this.cdRef, cdKey = this.cdKey, cdDat = this.cdDat;
        for (int i = 0, n = this.cdN; i < n; ++i) {
            if (cdRef[i] == pos) {
                while (++i < n) {
                    if (cdKey[pos = cdRef[i]] == id) {
                        return cdDat[pos];
                    }
                }
                return 0;
            }
        }
        return 0;
    }

    @Override
    public boolean isAvailable(final CooldownCategory category) {
        final int id = category.id;
        if (id < 0) return true;
        int pos = id & this.cdMask, key = this.cdKey[pos];
        if (key == -1) return true;
        if (key == id) return this.cdDat[pos] <= 0;
        final int[] cdRef = this.cdRef, cdKey = this.cdKey, cdDat = this.cdDat;
        for (int i = 0, n = this.cdN; i < n; ++i) {
            if (cdRef[i] == pos) {
                while (++i < n) {
                    if (cdKey[pos = cdRef[i]] == id) {
                        return cdDat[pos] <= 0;
                    }
                }
                return true;
            }
        }
        return true;
    }

    private void validateLvlSize() {
        if (this.lvlN == this.lvlSize) {
            this.lvlSize += 4;
            final IArmorEffect[] key = new IArmorEffect[this.lvlSize];
            final int[] dat = new int[this.lvlSize];
            arraycopy(this.lvlKey, 0, key, 0, this.lvlN);
            arraycopy(this.lvlDat, 0, dat, 0, this.lvlN);
            this.lvlKey = key;
            this.lvlDat = dat;
        }
    }

    @Override
    public int setLevel(final IArmorEffect effect, final int level) {
        final IArmorEffect[] lvlKey = this.lvlKey;
        final int n = this.lvlN;
        for (int i = 0; i < n; ++i) {
            if (lvlKey[i] == effect) {
                return this.lvlDat[i] = level;
            }
        }
        this.validateLvlSize();//may assign new array to `this.lvlKey`
        this.lvlKey[n] = effect;
        return this.lvlDat[this.lvlN++] = level;
    }

    @Override
    public int stackLevel(final IArmorEffect effect) {
        final IArmorEffect[] lvlKey = this.lvlKey;
        final int n = this.lvlN;
        for (int i = 0; i < n; ++i) {
            if (lvlKey[i] == effect) {
                return ++this.lvlDat[i];
            }
        }
        this.validateLvlSize();//may assign new array to `this.lvlKey`
        this.lvlKey[n] = effect;
        return this.lvlDat[this.lvlN++] = 1;
    }

    @Override
    public boolean isActive(final IArmorEffect effect) {
        final IArmorEffect[] lvlKey = this.lvlKey;
        final int[] lvlRef = this.lvlRef;
        for (int i = 0, n = this.activeN; i < n; ++i) {
            if (lvlKey[lvlRef[i]] == effect) {
                return true;
            }
        }
        return false;
    }


    @Override
    public int getLevel(final IArmorEffect effect, final boolean filtered) {
        final IArmorEffect[] lvlKey = this.lvlKey;
        if (filtered) {
            final int[] lvlRef = this.lvlRef;
            for (int i = 0, j, n = this.activeN; i < n; ++i) {
                if (lvlKey[j = lvlRef[i]] == effect) {
                    return this.lvlDat[j];
                }
            }
        } else for (int i = 0; i < this.lvlN; ++i) {
            if (lvlKey[i] == effect) {
                return this.lvlDat[i];
            }
        }
        return 0;
    }

    @Override
    public void tick() {
        final int[] cdRef = this.cdRef, cdDat = this.cdDat, lvlDat = this.lvlDat;
        final PlayerEntity player = this.player;
        for (int i = 0, j; i < this.cdN; ++i) {
            if (--cdDat[j = cdRef[i]] < 1) {
                arraycopy(cdRef, i + 1, cdRef, i, --this.cdN - i);
                this.reassign(j, --i);
            }
        }
        int sum = this.activeN = this.lvlN = 0;
        for (ItemStack stack : player.getArmorItems()) {
            Item item = stack.getItem();
            if (item instanceof IArmorEffectSource) {
                ((IArmorEffectSource) item).affect(this, player, stack);
            }
        }
        final IArmorEffect[] lvlKey = this.lvlKey;
        int[] lvlRef = this.lvlRef;
        for (int i = 0, end = this.lvlN; i < end; ++i) {
            final IArmorEffect effect = lvlKey[i];
            if (effect.activate(this, player, lvlDat[i])) {
                if (sum == lvlRef.length) {
                    arraycopy(this.lvlRef, 0, lvlRef = new int[sum + 4], 0, sum);
                    this.lvlRef = lvlRef;
                }
                lvlRef[sum++] = i;
            }
        }
        this.activeN = sum;
    }
}
