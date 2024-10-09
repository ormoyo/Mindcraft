package com.ormoyo.mindcraft;

import com.ormoyo.mindcraft.ability.PossessionAbility;
import com.ormoyo.mindcraft.capability.Possessed;
import com.ormoyo.mindcraft.capability.PossessedCapabilityStorage;
import com.ormoyo.mindcraft.capability.PossessedImpl;
import com.ormoyo.mindcraft.client.render.PlayerBodyRenderer;
import com.ormoyo.mindcraft.entity.EntityHandler;
import com.ormoyo.ormoyoutil.ability.AbilityEntry;
import com.ormoyo.ormoyoutil.ability.AbilityEntryBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Mindcraft.MODID)
public class Mindcraft
{
    public static final String MODID = "mindcraft";
    public static final Logger LOGGER = LogManager.getLogger();

    public Mindcraft()
    {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        modBus.addListener(this::setup);
        modBus.addListener(this::clientSetup);

        modBus.addGenericListener(AbilityEntry.class, this::registerAbilities);
        modBus.addGenericListener(EntityType.class, EntityHandler::registerEntries);

        modBus.addListener(EntityHandler::registerAttributes);
    }

    private void setup(FMLCommonSetupEvent event)
    {
        CapabilityManager.INSTANCE.register(Possessed.class, new PossessedCapabilityStorage(), PossessedImpl::new);
    }

    private void clientSetup(FMLClientSetupEvent event)
    {
        RenderingRegistry.registerEntityRenderingHandler(EntityHandler.PLAYER_BODY, PlayerBodyRenderer::new);
    }

    private void registerAbilities(RegistryEvent.Register<AbilityEntry> event)
    {
        event.getRegistry().register(AbilityEntryBuilder.create()
                .ability(PossessionAbility.class)
                .id(new ResourceLocation(MODID, "possession_ability"))
                .build());
    }
}
