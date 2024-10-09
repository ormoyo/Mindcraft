package com.ormoyo.mindcraft.capability;

import com.ormoyo.mindcraft.ability.PossessionAbility;
import com.ormoyo.ormoyoutil.ability.Ability;
import com.ormoyo.ormoyoutil.ability.AbilityHolder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class PossessedImpl implements Possessed
{
    private World world;
    private int possessor;

    public PossessedImpl()
    {
    }

    @Override
    public void setPossessor(PlayerEntity possessor)
    {
        if (possessor == null)
        {
            this.world = null;
            this.possessor = 0;

            return;
        }

        this.world = possessor.getEntityWorld();
        this.possessor = possessor.getEntityId();
    }

    @Override
    public PlayerEntity getPossessor()
    {
        if (this.possessor <= 0)
            return null;

        return (PlayerEntity) this.world.getEntityByID(this.possessor);
    }

    @Override
    public PossessionAbility getPossessionAbility()
    {
        PlayerEntity possessor = this.getPossessor();

        if (possessor == null)
            return null;

        AbilityHolder abilityHolder = Ability.getAbilityHolder(possessor);

        if (abilityHolder == null)
            return null;

        return abilityHolder.getAbility(PossessionAbility.class);
    }
}
