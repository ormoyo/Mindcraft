package com.ormoyo.mindcraft.capability;

import com.ormoyo.mindcraft.ability.PossessionAbility;
import net.minecraft.entity.player.PlayerEntity;

public interface Possessed
{
    void setPossessor(PlayerEntity possessor);

    PlayerEntity getPossessor();
    PossessionAbility getPossessionAbility();
}
