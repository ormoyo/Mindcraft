package com.ormoyo.mindcraft.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public abstract class MixinMobEntity extends MixinLivingEntity
{
    protected MixinMobEntity(EntityType<? extends LivingEntity> type, World worldIn)
    {
        super(type, worldIn);
    }

    @Inject(method = "canBeSteered", at = @At(value = "HEAD"), cancellable = true)
    protected void onCanBeSteered(CallbackInfoReturnable<Boolean> callback)
    {
        if (this.mindCraft_1_16_5$getPossessor() == null)
            return;

        callback.setReturnValue(true);
    }
}
