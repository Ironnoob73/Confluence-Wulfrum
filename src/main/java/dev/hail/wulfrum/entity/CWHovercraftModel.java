package dev.hail.wulfrum.entity;

import dev.hail.wulfrum.WulfrumMod;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class CWHovercraftModel extends GeoModel<CWHovercraft> {
    @Override
    public ResourceLocation getModelResource(CWHovercraft cwHovercraft) {
        return WulfrumMod.asResource("geo/entity/wulfrum_hovercraft.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CWHovercraft cwHovercraft) {
        return WulfrumMod.asResource("textures/entity/wulfrum_hovercraft.png");
    }

    @Override
    public ResourceLocation getAnimationResource(CWHovercraft cwHovercraft) {
        return WulfrumMod.asResource("animations/entity/wulfrum_hovercraft.animation.json");
    }
}
