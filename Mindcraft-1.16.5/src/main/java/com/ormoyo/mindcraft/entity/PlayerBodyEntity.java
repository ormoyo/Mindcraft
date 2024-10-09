package com.ormoyo.mindcraft.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.HandSide;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Optional;

public class PlayerBodyEntity extends LivingEntity
{
    private static final DataParameter<Integer> OWNER_ID = EntityDataManager.createKey(PlayerBodyEntity.class, DataSerializers.VARINT);

    public PlayerBodyEntity(EntityType<PlayerBodyEntity> type, World world)
    {
        super(EntityHandler.PLAYER_BODY, world);
    }

    public PlayerBodyEntity(PlayerEntity owner)
    {
        this(EntityHandler.PLAYER_BODY, owner.getEntityWorld());
        this.dataManager.set(OWNER_ID, owner.getEntityId());

        this.setLocationAndAngles(owner.getPosX(), owner.getPosY(), owner.getPosZ(), owner.rotationYawHead, owner.rotationPitch);
    }

    @Override
    protected void registerData()
    {
        super.registerData();
        this.dataManager.register(OWNER_ID, 0);
    }

    public PlayerEntity getOwner()
    {
        int id = this.dataManager.get(OWNER_ID);
        return (PlayerEntity) this.getEntityWorld().getEntityByID(id);
    }

    @Nonnull
    @Override
    public Iterable<ItemStack> getArmorInventoryList()
    {
        return this.getOwner().getArmorInventoryList();
    }

    @Nonnull
    @Override
    public ItemStack getItemStackFromSlot(@Nonnull EquipmentSlotType slotIn)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemStackToSlot(EquipmentSlotType slotIn, @Nonnull ItemStack stack)
    {
    }

    @Nonnull
    @Override
    public HandSide getPrimaryHand()
    {
        return HandSide.RIGHT;
    }

    @Override
    public boolean attackEntityFrom(@Nonnull DamageSource source, float amount)
    {
        return super.attackEntityFrom(source, amount);
    }

    @Override
    protected void collideWithEntity(@Nonnull Entity entityIn)
    {
        super.collideWithEntity(entityIn);
    }

    @Override
    public void readAdditional(@Nonnull CompoundNBT compound)
    {
        super.readAdditional(compound);
        if (compound.contains("player"))
        {
            String name = compound.getString("player");
            Optional<? extends PlayerEntity> player = this.getEntityWorld().getPlayers().stream().filter(p -> name.equals(p.getName().getString())).findAny();

            player.ifPresent(playerEntity -> this.dataManager.set(OWNER_ID, playerEntity.getEntityId()));
        }
    }
}
