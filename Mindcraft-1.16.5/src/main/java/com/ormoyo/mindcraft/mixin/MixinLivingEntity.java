package com.ormoyo.mindcraft.mixin;

import com.ormoyo.mindcraft.Mindcraft;
import com.ormoyo.mindcraft.ability.PossessionAbility;
import com.ormoyo.mindcraft.capability.Possessed;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;

import java.util.Optional;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity
{
    @Shadow public float renderYawOffset;
    @Shadow public float rotationYawHead;

    @Shadow public abstract void setAIMoveSpeed(float speedIn);
    @Shadow public abstract double getAttributeValue(Attribute attribute);

    @Shadow public abstract void func_233629_a_(LivingEntity p_233629_1_, boolean p_233629_2_);

    public MixinLivingEntity(EntityType<?> entityTypeIn, World worldIn)
    {
        super(entityTypeIn, worldIn);
    }

    @Override
    public Entity getControllingPassenger()
    {
        return this.mindCraft_1_16_5$getPossessor();
    }

    @Redirect(method = "livingTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;travel(Lnet/minecraft/util/math/vector/Vector3d;)V"))
    protected void travelProxy(LivingEntity instance, Vector3d travelVector)
    {
        travelVector = this.mindCraft_1_16_5$modifiedTravel(travelVector);
        instance.travel(travelVector);

//        if (this.mindCraft_1_16_5$getPossessor() != null)
//            Mindcraft.LOGGER.debug("Traveling {}", travelVector);
    }

    @Unique
    protected Vector3d mindCraft_1_16_5$modifiedTravel(Vector3d travelVector)
    {
        if (!this.isAlive())
            return travelVector;

        PlayerEntity possessor = this.mindCraft_1_16_5$getPossessor();
        if (possessor == null)
            return travelVector;

        this.rotationYaw = possessor.rotationYaw % 360;
        this.renderYawOffset = this.rotationYaw;

        this.rotationPitch = possessor.rotationPitch % 360;

        this.renderYawOffset = this.rotationYaw;
        this.rotationYawHead = possessor.rotationYawHead;

        this.setAIMoveSpeed((float)this.getAttributeValue(Attributes.MOVEMENT_SPEED));
        this.func_233629_a_((LivingEntity)(Object)this, false);

        return new Vector3d(possessor.moveStrafing, travelVector.y, possessor.moveForward);
    }

    @ModifyVariable(method = "travel", at = @At(value = "HEAD", shift = At.Shift.AFTER), argsOnly = true, ordinal = 0)
    protected Vector3d modifyTravelVector(Vector3d travelVector)
    {
//        if (!this.isAlive())
//            return travelVector;
//
//        PlayerEntity possessor = this.getPossessor();
//        if (this.getPossessor() == null)
//            return travelVector;

        return travelVector;
    }

    @Unique
    protected PlayerEntity mindCraft_1_16_5$getPossessor()
    {
        Optional<Possessed> possessedOpt = this.getCapability(PossessionAbility.getPossessedCapability()).resolve();
        if (!possessedOpt.isPresent())
            return null;

        Possessed possessed = possessedOpt.get();
        if (possessed.getPossessor() == null)
            return null;

        return possessed.getPossessor();
    }
}
