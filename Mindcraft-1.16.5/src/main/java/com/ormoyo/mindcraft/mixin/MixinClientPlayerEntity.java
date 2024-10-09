package com.ormoyo.mindcraft.mixin;

import com.mojang.authlib.GameProfile;
import com.ormoyo.mindcraft.ability.PossessionAbility;
import com.ormoyo.ormoyoutil.ability.Ability;
import com.ormoyo.ormoyoutil.ability.AbilityHolder;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity extends AbstractClientPlayerEntity
{
    protected MixinClientPlayerEntity(ClientWorld world, GameProfile profile)
    {
        super(world, profile);
    }

    @Inject(method = "isCurrentViewEntity", at = @At("HEAD"), cancellable = true)
    protected void onIsCurrentViewEntity(CallbackInfoReturnable<Boolean> callback)
    {
        if (!this.mindcraft_1_16_5$isPossessing())
            return;

        callback.setReturnValue(true);
    }

    @Unique
    private boolean mindcraft_1_16_5$isPossessing()
    {
        AbilityHolder abilityHolder = Ability.getAbilityHolder(this);
        if (abilityHolder == null)
            return false;

        PossessionAbility ability = abilityHolder.getAbility(PossessionAbility.class);
        return ability != null && ability.isPossessing();
    }
}
