package net.dragonmounts.client.model.dragon;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;

public abstract class DragonLegModelPart extends ModelPart {
    public final int index;
    public final boolean left;
    public final ModelPart shank;
    public final ModelPart foot;
    public final ModelPart toe;

    public DragonLegModelPart(Model model, boolean hind, boolean left, DragonLegConfig config) {
        super(model);
        this.index = hind ? 1 : 0;
        this.left = left;
        this.buildThigh(config);
        this.addChild(this.shank = this.createShank(model, config));
        this.shank.addChild(this.foot = this.createFoot(model, config));
        this.foot.addChild(this.toe = this.createToe(model, config));
    }

    abstract protected void buildThigh(DragonLegConfig config);

    abstract protected ModelPart createShank(Model model, DragonLegConfig config);

    abstract protected ModelPart createFoot(Model model, DragonLegConfig config);

    abstract protected ModelPart createToe(Model model, DragonLegConfig config);

    public static class Fore extends DragonLegModelPart {
        public Fore(Model model, boolean left, DragonLegConfig config) {
            super(model, false, left, config);
        }

        @Override
        protected void buildThigh(DragonLegConfig config) {
            final int length = config.getThighLength(false);
            final int width = config.width;
            this.setPivot(-11, 18, 4);
            this.setTextureOffset(112, 0)
                    .addCuboid(config.defaultOffset, config.defaultOffset, config.defaultOffset, width, length, width);

        }

        @Override
        protected ModelPart createShank(Model model, DragonLegConfig config) {
            final int length = config.getShankLength(false);
            final ModelPart part = new ModelPart(model);
            part.setPivot(0, config.getThighLength(false) + config.defaultOffset, 0);
            return part.setTextureOffset(148, 0)
                    .addCuboid(config.shankOffset, config.shankOffset, config.shankOffset, config.shankWidth, length, config.shankWidth);
        }

        @Override
        protected ModelPart createFoot(Model model, DragonLegConfig config) {
            final int length = config.getFootLength(false);
            final ModelPart part = new ModelPart(model);
            part.setPivot(0, config.getShankLength(false) + config.shankOffset, 0);
            return part.setTextureOffset(210, 0)
                    .addCuboid(config.defaultOffset, config.footOffset, length * -0.75F, config.width, config.footHeight, length);
        }

        @Override
        protected ModelPart createToe(Model model, DragonLegConfig config) {
            final int length = config.getToeLength(false);
            final ModelPart part = new ModelPart(model);
            part.setPivot(0, 0, config.getFootLength(false) * -0.75F - config.footOffset / 2F);
            return part.setTextureOffset(176, 0)
                    .addCuboid(config.defaultOffset, config.footOffset, -length, config.width, config.footHeight, length);
        }
    }

    public static class Hind extends DragonLegModelPart {
        public Hind(Model model, boolean left, DragonLegConfig config) {
            super(model, true, left, config);
        }

        @Override
        protected void buildThigh(DragonLegConfig config) {
            final int length = config.getThighLength(true);
            final int width = config.width + 1;
            this.setPivot(-11, 13, 4);
            this.setTextureOffset(112, 29)
                    .addCuboid(config.defaultOffset, config.defaultOffset, config.defaultOffset, width, length, width);
        }

        @Override
        protected ModelPart createShank(Model model, DragonLegConfig config) {
            final int length = config.getShankLength(true);
            final ModelPart part = new ModelPart(model);
            part.setPivot(0, config.getThighLength(true) + config.defaultOffset, 0);
            return part.setTextureOffset(152, 29).addCuboid(config.shankOffset, config.shankOffset, config.shankOffset, config.shankWidth, length, config.shankWidth);
        }

        @Override
        protected ModelPart createFoot(Model model, DragonLegConfig config) {
            final int length = config.getFootLength(true);
            final ModelPart part = new ModelPart(model);

            part.setPivot(0, config.getShankLength(true) + config.shankOffset, 0);
            return part.setTextureOffset(180, 29).addCuboid(config.defaultOffset, config.footOffset, length * -0.75F, config.width, config.footHeight, length);
        }

        @Override
        protected ModelPart createToe(Model model, DragonLegConfig config) {
            final int length = config.getToeLength(true);
            final ModelPart part = new ModelPart(model);
            part.setPivot(0, 0, config.getFootLength(true) * -0.75F - config.footOffset / 2F);
            return part.setTextureOffset(215, 29).addCuboid(config.defaultOffset, config.footOffset, -length, config.width, config.footHeight, length);
        }
    }
}
