package com.ormoyo.mindcraft.entity;

import com.google.common.collect.Lists;
import com.ormoyo.mindcraft.Mindcraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;

import java.util.Collection;

public class EntityHandler
{
    private static final Collection<EntityType<?>> ENTITY_TYPES = Lists.newArrayList();

    public static final EntityType<PlayerBodyEntity> PLAYER_BODY = register("player_body", PlayerBodyEntity::new, EntityClassification.AMBIENT, 1f, 2f);

    private static<E extends Entity> EntityType<E> register(String name, EntityType.IFactory<E> factory, EntityClassification classification, float width, float height)
    {
        EntityType<E> entityType = EntityType.Builder
                .create(factory, classification)
                .size(width, height)
                .disableSerialization()
                .build(new ResourceLocation(Mindcraft.MODID, name).toString());

        entityType.setRegistryName(new ResourceLocation(Mindcraft.MODID, name));
        ENTITY_TYPES.add(entityType);

        return entityType;
    }

    public static void registerEntries(RegistryEvent.Register<EntityType<?>> event)
    {
        event.getRegistry().registerAll(ENTITY_TYPES.toArray(new EntityType[0]));
    }

    public static void registerAttributes(EntityAttributeCreationEvent event)
    {
        event.put(PLAYER_BODY, LivingEntity.registerAttributes().create());
    }
}
