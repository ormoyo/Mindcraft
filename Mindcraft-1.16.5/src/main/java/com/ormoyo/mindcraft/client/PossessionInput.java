package com.ormoyo.mindcraft.client;

import com.ormoyo.ormoyoutil.OrmoyoUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Objects;

public class PossessionInput extends MovementInputFromOptions
{
    private final int possessionId;

    public PossessionInput(LivingEntity possession)
    {
        super(Minecraft.getInstance().gameSettings);
        this.possessionId = possession.getEntityId();
    }

    @Override
    public void tickMovement(boolean p_225607_1_)
    {
        super.tickMovement(p_225607_1_);

        LivingEntity possession = this.getPossession();

        possession.moveRelative(1f, new Vector3d(this.moveStrafe, 0.0, this.moveForward));

        OrmoyoUtil.LOGGER.debug("POSSESSION_INPUT = {} and {}", this.moveForward, this.moveStrafe);
        possession.setJumping(this.jump);
    }

    private LivingEntity getPossession()
    {
        return (LivingEntity) Objects.requireNonNull(Minecraft.getInstance().world).getEntityByID(this.possessionId);
    }
}
