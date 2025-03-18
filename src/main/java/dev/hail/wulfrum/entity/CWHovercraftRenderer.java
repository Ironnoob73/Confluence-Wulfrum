package dev.hail.wulfrum.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

@OnlyIn(Dist.CLIENT)
public class CWHovercraftRenderer extends GeoEntityRenderer<CWHovercraft> {
    public CWHovercraftRenderer(EntityRendererProvider.Context context) {
        super(context, new CWHovercraftModel());
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
