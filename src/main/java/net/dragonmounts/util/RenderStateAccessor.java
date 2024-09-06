package net.dragonmounts.util;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public abstract class RenderStateAccessor extends RenderStateShard {
    public static Function<ResourceLocation, RenderType> ENTITY_TRANSLUCENT_EMISSIVE_DECAL = Util.memoize((location) -> RenderType.create("entity_decal", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, RenderType.CompositeState.builder()
            .setShaderState(RENDERTYPE_ENTITY_DECAL_SHADER)
            .setTextureState(new RenderStateShard.TextureStateShard(location, false, false))
            .setDepthTestState(EQUAL_DEPTH_TEST)
            .setCullState(NO_CULL)
            .setWriteMaskState(COLOR_WRITE)
            .setLightmapState(LIGHTMAP)
            .setOverlayState(OVERLAY)
            .createCompositeState(false)
    ));

    private RenderStateAccessor(String a, Runnable b, Runnable c) {
        super(a, b, c);
    }
}
