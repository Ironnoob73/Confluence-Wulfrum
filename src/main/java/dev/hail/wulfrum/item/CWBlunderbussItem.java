package dev.hail.wulfrum.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.confluence.terra_curio.common.component.ModRarity;
import org.confluence.terra_guns.api.IGun;
import org.confluence.terra_guns.common.entity.SimpleTrailProjectile;

import java.util.List;

public class CWBlunderbussItem extends BlunderbussItem<SimpleTrailProjectile>{
    public CWBlunderbussItem(Properties properties, ModRarity rarity, float damage, float weaponSpeed, int useDelay, float knockBack, float crit, float inaccuracy) {
        super(properties, rarity, damage, weaponSpeed, useDelay, knockBack, crit, inaccuracy);
    }
    @Override
    public SimpleTrailProjectile createAmmo(Level level, Player shooter, ItemStack gunStack, ItemStack ammoStack) {
        return new SimpleTrailProjectile(shooter, 0xffff3f);
    }
    @Override
    public void beforeAmmoShoot(Player shooter, SimpleTrailProjectile projectile, ItemStack gunStack, ItemStack ammoStack) {
        float damage = ((IGun<SimpleTrailProjectile>) gunStack.getItem()).getGunDamage(shooter, projectile, gunStack, ammoStack);
        projectile.damageAndKnockback(getFinalDamage(damage, shooter, projectile, gunStack, ammoStack), getKnockBack());
    }
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.confluence_wulfrum.wulfrum_blunderbuss.tooltip"));
        super.appendHoverText(stack,context,tooltipComponents,tooltipFlag);
    }
}
