package dev.hail.wulfrum.item;

import dev.hail.wulfrum.WulfrumMod;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import org.confluence.mod.common.init.item.ModItems;
import org.confluence.terra_curio.common.component.ModRarity;
import org.confluence.terra_curio.common.init.TCDataComponentTypes;
import org.confluence.terra_guns.api.IAmmo;
import org.confluence.terra_guns.api.IGun;
import org.confluence.terra_guns.common.entity.SimpleTrailProjectile;
import org.confluence.terra_guns.common.item.gun.GeoGunItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class BlunderbussItem<T extends Projectile> extends GeoGunItem<T> implements IAmmo<T> {
    protected ItemAttributeModifiers modifiers;

    public BlunderbussItem(Properties properties, ModRarity rarity, float damage, float weaponSpeed, int useDelay, float knockBack, float crit, float inaccuracy) {
        super(properties.component(TCDataComponentTypes.MOD_RARITY, rarity), damage, weaponSpeed, useDelay, knockBack, crit, inaccuracy);
    }

    public BlunderbussItem(ModRarity rarity, float damage, float weaponSpeed, int useDelay, float knockBack, float crit, float inaccuracy) {
        this(new Properties(), rarity, damage, weaponSpeed, useDelay, knockBack, crit, inaccuracy);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack gun = player.getItemInHand(hand);
        ItemStack ammo = player.getProjectile(gun);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(gun);
        }
        if (!player.getAbilities().instabuild && ammo.isEmpty() && gun.getDamageValue() == 0) {
            return InteractionResultHolder.fail(gun);
        }
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack gunStack, Level level, LivingEntity livingEntity) {
        if (livingEntity instanceof Player player) {

            ItemStack ammoStack = player.getProjectile(gunStack);

            IGun<T> gun = (IGun<T>) gunStack.getItem();
            IAmmo<T> ammo = (IAmmo<T>) gunStack.getItem();
            if (level.isClientSide) {
                gun.clientShoot((ClientLevel) level, player, gunStack, gunStack);
                ammo.clientShoot((ClientLevel) level, player, gunStack, gunStack);
            } else {
                serverShoot((ServerLevel) level, player, gunStack, gunStack, ammo, gun, ammoStack);
            }
        }
        return gunStack;
    }
    public void serverShoot(ServerLevel level, Player player, ItemStack gunStack, ItemStack ammoStack, IAmmo<T> ammo, IGun<T> gun, ItemStack trueAmmoStack) {
        for (int i = 0; i < 6 && !ammoStack.isEmpty(); i++) {
            T projectile = ammo.createAmmo(level, player, gunStack, ammoStack);
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, this.getRealAmmoSpeed(player, projectile, gunStack, ammoStack), this.getInaccuracy(player, projectile, gunStack, ammoStack));
            ammo.beforeAmmoShoot(player, projectile, gunStack, ammoStack);

            if (level.addFreshEntity(projectile) && i < 1) {
                if (gunStack.getDamageValue() == 0){
                    trueAmmoStack.consume(1, player);
                }
                if (gunStack.getDamageValue() < gunStack.getMaxDamage() - 1){
                    gunStack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(player.getUsedItemHand()));
                } else {
                    gunStack.setDamageValue(0);
                }
                player.getCooldowns().addCooldown(this, this.getUseDelay(player, gunStack, ammoStack));
            }
        }
    }
    @Override
    public void afterGunShoot(ItemStack gunStack, Player player) {
        if (gunStack.getDamageValue() > 0){
            gunStack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(player.getUsedItemHand()));
        }
    }

    @Override
    public @NotNull Predicate<ItemStack> getAllSupportedProjectiles() {
        return itemStack -> itemStack.getItem() == CWItems.WULFRUM_SCRAP.asItem()
                || itemStack.getItem() == ModItems.SILVER_COIN;
    }

    @Override
    public ItemStack getDefaultCreativeAmmo(@Nullable Player player, ItemStack projectileWeaponItem) {
        return getDefaultInstance();
    }

    @Override
    public boolean isInfinite(Player shooter, ItemStack ammoStack, ItemStack gunStack) {
        return false;
    }

    @Override
    public boolean isValidAmmo(ItemStack ammoStack) {
        return false;
    }

    @Override
    public float getAmmoSpeed(Player shooter, T projectile, ItemStack gunStack) {
        return 0;
    }

    @Override
    public float getInaccuracy(Player shooter, T projectile, ItemStack gunStack) {
        return inaccuracy;
    }

    @Override
    public float getKnockBack() {
        return knockBack;
    }

    @Override
    public float getBaseDamage(Player shooter, T projectile, ItemStack gunStack) {
        return 0;
    }

    @Override
    public float getDamageMultiplier(Player shooter, T projectile, ItemStack gunStack) {
        return 1;
    }

    @Override
    public void doPostHurtEffects(T projectile, Entity target) {}

    @Override
    public float getFinalDamage(float damage, Player shooter, T projectile, ItemStack gunStack, ItemStack ammoStack) {
        return damage;
    }

    public float getVelocityMultiplier(Player shooter, T ammoEntity, ItemStack gunStack) {
        return 1;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private GeoItemRenderer<GeoGunItem<SimpleTrailProjectile>> renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (renderer == null) {
                    String path = BuiltInRegistries.ITEM.getKey(BlunderbussItem.this).getPath();
                    this.renderer = new GeoItemRenderer<>(new DefaultedItemGeoModel<>(WulfrumMod.asResource("guns/" + path)));
                }
                return renderer;
            }
        });
    }

    public void addAttributeModifiers(Consumer<ItemAttributeModifiers.Builder> consumer) {
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        consumer.accept(builder);
        this.modifiers = builder.build();
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        return modifiers == null ? super.getDefaultAttributeModifiers(stack) : modifiers;
    }
}
