package com.ormoyo.mindcraft.ability;

import com.ormoyo.mindcraft.Mindcraft;
import com.ormoyo.mindcraft.capability.Possessed;
import com.ormoyo.mindcraft.capability.PossessedCapabilityProvider;
import com.ormoyo.mindcraft.capability.PossessedImpl;
import com.ormoyo.mindcraft.client.PossessionInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.util.Objects;
import java.util.Optional;

class AbilityMethods
{
    @CapabilityInject(Possessed.class)
    public static final Capability<Possessed> POSSESSED_CAPABILITY = null;

    public static class ClientMethods
    {
        static LivingEntity setPossessionEntityArg;
        public static void setPossessionEntity()
        {
            if (setPossessionEntityArg == null)
                return;

            Minecraft.getInstance().setRenderViewEntity(setPossessionEntityArg);
            setPossessionEntityArg = null;
        }


    }

    @EventBusSubscriber(modid = Mindcraft.MODID)
    public static class EventMethods
    {
        @SubscribeEvent
        public static void onEntityDeath(LivingDeathEvent event)
        {
            Optional<Possessed> possessedOpt = event.getEntityLiving().getCapability(PossessionAbility.getPossessedCapability()).resolve();

            if (!possessedOpt.isPresent())
                return;

            Possessed possessed = possessedOpt.get();
            if (possessed.getPossessor() != null)
            {
                possessed.getPossessionAbility().unpossessEntity();
            }
        }

        @SubscribeEvent
        public static void attachCapability(AttachCapabilitiesEvent<Entity> event)
        {
            if (event.getObject() instanceof LivingEntity)
            {
                PossessedCapabilityProvider<PossessedImpl> provider = new PossessedCapabilityProvider<>(new PossessedImpl());

                event.addCapability(new ResourceLocation(Mindcraft.MODID, "possessable"), provider);
                event.addListener(provider::invalidate);
            }
        }
    }
}
