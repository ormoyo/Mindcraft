package com.ormoyo.mindcraft.mixin;

import com.ormoyo.mindcraft.ability.PossessionAbility;
import com.ormoyo.ormoyoutil.ability.Ability;
import com.ormoyo.ormoyoutil.ability.AbilityHolder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity extends LivingEntity
{
    protected MixinPlayerEntity(EntityType<? extends LivingEntity> type, World worldIn)
    {
        super(type, worldIn);
    }

    @Inject(method = "collideWithPlayer", at = @At(value = "HEAD"), cancellable = true)
    protected void onCollideWithEntity(Entity entityIn, CallbackInfo callback)
    {
        if (!this.mindcraft$isPossessing())
            return;

        callback.cancel();
    }

    @Inject(method = "wantsToStopRiding", at = @At("HEAD"), cancellable = true)
    protected void onWantsToStopRiding(CallbackInfoReturnable<Boolean> callback)
    {
        if (!this.mindcraft$isPossessing())
            return;

        callback.setReturnValue(false);
    }

    @Override
    protected void collideWithEntity(Entity entityIn)
    {
        if (this.mindcraft$isPossessing())
            return;

        super.collideWithEntity(entityIn);
    }

    @Override
    public void applyEntityCollision(Entity entityIn)
    {
        if (this.mindcraft$isPossessing())
            return;

        super.collideWithEntity(entityIn);
    }

    @Override
    public void recalculateSize()
    {
        PlayerEntity player = (PlayerEntity)(Object) this;
        AbilityHolder abilityHolder = Ability.getAbilityHolder(player);

        if (abilityHolder == null)
            return;

        PossessionAbility ability = abilityHolder.getAbility(PossessionAbility.class);
        if (ability == null || !ability.isPossessing())
        {
            super.recalculateSize();
            return;
        }

        this.setBoundingBox(ability.getPossession().getBoundingBox());
    }

    @Unique
    private boolean mindcraft$isPossessing()
    {
        PlayerEntity player = (PlayerEntity)(Object) this;
        AbilityHolder abilityHolder = Ability.getAbilityHolder(player);

        if (abilityHolder == null)
            return false;

        PossessionAbility ability = abilityHolder.getAbility(PossessionAbility.class);
        return ability != null && ability.isPossessing();
    }
}
