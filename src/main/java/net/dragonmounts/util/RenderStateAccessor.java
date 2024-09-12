package net.dragonmounts.util;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

public abstract class RenderStateAccessor extends RenderPhase {
    public static RenderLayer createGlowRenderLayer(Identifier location) {
        return RenderLayer.of("glow", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, 7, 256, false, true, RenderLayer.MultiPhaseParameters.builder()
                .texture(new RenderPhase.Texture(location, false, false))
                .transparency(TRANSLUCENT_TRANSPARENCY)
                .writeMaskState(COLOR_MASK)
                .build(false)
        );
    }

    public static RenderLayer createGlowDecalRenderLayer(Identifier location) {
        return RenderLayer.of("glow_decal", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, 7, 256, false, true, RenderLayer.MultiPhaseParameters.builder()
                .texture(new RenderPhase.Texture(location, false, false))
                .diffuseLighting(ENABLE_DIFFUSE_LIGHTING)
                .alpha(ONE_TENTH_ALPHA)
                .depthTest(EQUAL_DEPTH_TEST)
                .cull(DISABLE_CULLING)
                .writeMaskState(COLOR_MASK)
                .lightmap(ENABLE_LIGHTMAP)
                .overlay(ENABLE_OVERLAY_COLOR)
                .build(false)
        );
    }

    private RenderStateAccessor(String a, Runnable b, Runnable c) {
        super(a, b, c);
    }
}
