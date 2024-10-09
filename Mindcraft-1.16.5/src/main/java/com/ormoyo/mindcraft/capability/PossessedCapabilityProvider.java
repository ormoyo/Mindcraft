package com.ormoyo.mindcraft.capability;

import com.ormoyo.mindcraft.ability.PossessionAbility;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PossessedCapabilityProvider<T extends Possessed> implements ICapabilitySerializable<INBT>
{
    private final T instance;

    private final Direction direction;
    private final LazyOptional<T> instanceOptional;

    public PossessedCapabilityProvider(T instance)
    {
        this(instance, null);
    }

    public PossessedCapabilityProvider(T instance, Direction direction)
    {
        this.instance = instance;
        this.direction = direction;

        this.instanceOptional = LazyOptional.of(() -> instance);
    }

    @Nonnull
    @Override
    public <U> LazyOptional<U> getCapability(@Nonnull Capability<U> cap, @Nullable Direction side)
    {
        if (PossessionAbility.getPossessedCapability() == cap && this.direction == side)
            return this.instanceOptional.cast();

        return LazyOptional.empty();
    }

    @Override
    public INBT serializeNBT()
    {
        return PossessionAbility.getPossessedCapability().getStorage().writeNBT(PossessionAbility.getPossessedCapability(), this.instance, this.direction);
    }

    @Override
    public void deserializeNBT(INBT nbt)
    {
        PossessionAbility.getPossessedCapability().getStorage().readNBT(PossessionAbility.getPossessedCapability(), this.instance, this.direction, nbt);
    }

    public void invalidate()
    {
        this.instanceOptional.invalidate();
    }
}
