package com.ormoyo.mindcraft.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class PossessedCapabilityStorage implements Capability.IStorage<Possessed>
{
    @Nullable
    @Override
    public INBT writeNBT(Capability<Possessed> capability, Possessed instance, Direction side)
    {
        return new CompoundNBT();
    }

    @Override
    public void readNBT(Capability<Possessed> capability, Possessed instance, Direction side, INBT nbt)
    {
    }
}
