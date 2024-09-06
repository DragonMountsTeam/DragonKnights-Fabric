package net.dragonmounts.client.model.dragon;

public class DragonLegConfig {
    public static final int LEG_BASE_LENGTH = 26;
    public static final int LEG_BASE_WIDTH = 9;
    public static final int FOOT_HEIGHT = 4;
    public static final DragonLegConfig DEFAULT = new DragonLegConfig(LEG_BASE_LENGTH, LEG_BASE_WIDTH, FOOT_HEIGHT);
    public static final DragonLegConfig SKELETON = new DragonLegConfig(LEG_BASE_LENGTH, LEG_BASE_WIDTH, FOOT_HEIGHT);
    public final int length;
    public final int width;
    public final int footHeight;
    public final int shankWidth;
    public final float defaultOffset;
    public final float shankOffset;
    public final float footOffset;

    public DragonLegConfig(int length, int width, int footHeight) {
        this.length = length;
        this.width = width;
        this.footHeight = footHeight;
        this.shankWidth = width - 2;
        this.defaultOffset = width / -2F;
        this.shankOffset = this.shankWidth / -2F;
        this.footOffset = this.footHeight / -2F;
    }

    public int getThighLength(boolean hind) {
        return (int) (this.length * (hind ? 0.90F : 0.77F));
    }

    public int getShankLength(boolean hind) {
        return (int) (hind ? this.length * 0.70F - 2 : this.length * 0.80F);
    }

    public int getFootLength(boolean hind) {
        return (int) (this.length * (hind ? 0.67F : 0.34F));
    }

    public int getToeLength(boolean hind) {
        return (int) (this.length * (hind ? 0.27F : 0.33F));
    }
}
