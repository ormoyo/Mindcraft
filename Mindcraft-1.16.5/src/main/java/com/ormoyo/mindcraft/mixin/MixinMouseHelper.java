package com.ormoyo.mindcraft.mixin;

import net.minecraft.client.MouseHelper;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MouseHelper.class)
public class MixinMouseHelper
{
//    @Redirect(method = "updatePlayerLook", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/player/ClientPlayerEntity;rotateTowards(DD)V"))
//    protected void updatePossessedLook(ClientPlayerEntity instance, double yaw, double pitch)
//    {
//        instance.rotateTowards(yaw, pitch);
//    }
}
