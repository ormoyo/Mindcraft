package com.ormoyo.mindcraft.ability;

import com.ormoyo.mindcraft.Mindcraft;
import com.ormoyo.mindcraft.capability.Possessed;
import com.ormoyo.mindcraft.entity.PlayerBodyEntity;
import com.ormoyo.ormoyoutil.abilities.ToggleAbility;
import com.ormoyo.ormoyoutil.ability.AbilityHolder;
import com.ormoyo.ormoyoutil.ability.util.ClientAbility;
import com.ormoyo.ormoyoutil.network.datasync.AbilityDataParameter;
import com.ormoyo.ormoyoutil.network.datasync.AbilitySyncManager;
import com.ormoyo.ormoyoutil.util.ASMUtils;
import com.ormoyo.ormoyoutil.util.EntityUtils;
import com.ormoyo.ormoyoutil.util.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;

import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

/**
 * TODO Create a fake entity that will mimic the player and redirect player movements into possessions
 */
@ClientAbility(share = true)
public class PossessionAbility extends ToggleAbility
{
	private static final UUID registerGoals = new UUID(MathUtils.randomInt(-999999, 99999), MathUtils.randomInt(-99999, 99999));

	private static final AbilityDataParameter<Integer> POSSESSION_ENTITY_ID = AbilitySyncManager.createKey(PossessionAbility.class, DataSerializers.VARINT);
	private static final AbilityDataParameter<Integer> BODY_ENTITY_ID = AbilitySyncManager.createKey(PossessionAbility.class, DataSerializers.VARINT);

	public PossessionAbility(AbilityHolder owner)
	{
		super(owner);
	}

	@Override
	public void abilityInit()
	{
		super.abilityInit();

		this.syncManager.register(POSSESSION_ENTITY_ID, 0);
		this.syncManager.register(BODY_ENTITY_ID, 0);
	}

	private int testCount = 0;
	@Override
	public void tick()
	{
		super.tick();
		if (testCount == 0)
			Mindcraft.LOGGER.debug("Ping every sec " + Minecraft.getInstance().getRenderViewEntity());

		testCount++;
		testCount %= 20;
	}

	@Override
	public boolean toggle(String keybind, boolean toggled)
	{
		if (toggled)
		{
			if (this.isPossessing())
				this.unpossessEntity();

			return true;
		}
		LivingEntity entity = EntityUtils.raytraceEntityFromEntity(LivingEntity.class, this.getOwner(), 5f);
		Mindcraft.LOGGER.debug("Testing possession: Entity = {}", entity);
		if (entity != null)
		{
			this.possessEntity(entity);
			return true;
		}

		return false;
	}

	@ParametersAreNonnullByDefault
	public void possessEntity(LivingEntity possession)
	{
		Optional<Possessed> possessedOpt = possession.getCapability(PossessionAbility.getPossessedCapability()).resolve();

		if (!possessedOpt.isPresent())
			return;

		Possessed possessed = possessedOpt.get();

		if (possessed.getPossessor() != null)
			return;

		possessed.setPossessor(this.getOwner());
		this.getOwner().startRiding(possession, true);

		if (this.getOwner().getEntityWorld().isRemote())
			return;

		this.setPossessionEntity(possession);

		if (possession instanceof MobEntity)
		{
			MobEntity mob = (MobEntity) possession;

			mob.goalSelector.goals.clear();
			mob.targetSelector.goals.clear();
		}

		this.getOwner().setLeftShoulderEntity(new CompoundNBT());
		this.getOwner().setRightShoulderEntity(new CompoundNBT());

		PlayerBodyEntity body = new PlayerBodyEntity(this.getOwner());
		this.getOwner().getEntityWorld().addEntity(body);

		ForgeChunkManager.forceChunk((ServerWorld) body.getEntityWorld(), Mindcraft.MODID, body, body.chunkCoordX, body.chunkCoordZ, true, true);
		this.setBodyEntity(body);
	}

	public void unpossessEntity()
	{
		LivingEntity possession = this.getPossession();
		Optional<Possessed> possessedOpt = possession.getCapability(PossessionAbility.getPossessedCapability()).resolve();

		if (!possessedOpt.isPresent())
			return;

		Possessed possessed = possessedOpt.get();
		possessed.setPossessor(null);

		this.getOwner().stopRiding();

		if (this.getOwner().getEntityWorld().isRemote())
			return;

		this.setPossessionEntity(null);
		if (possession instanceof MobEntity)
		{
            try
            {
				Method method = MobEntity.class.getDeclaredMethod("registerGoals");
				method.setAccessible(true);
                method.invoke(possession);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e)
            {
                throw new RuntimeException(e);
            }
        }

		int bodyEntityID = this.syncManager.get(BODY_ENTITY_ID);

		Entity body = this.getOwner().getEntityWorld().getEntityByID(bodyEntityID);
		if (body == null)
			return;

		Vector3d pos = body.getPositionVec();
		float yaw = body.rotationYaw;
		float yawHead = body.getRotationYawHead();
		float pitch = body.rotationPitch;

		ForgeChunkManager.forceChunk((ServerWorld) body.getEntityWorld(), Mindcraft.MODID, body, body.chunkCoordX, body.chunkCoordZ, false, true);

		this.setBodyEntity(null);
		body.remove();

		this.getOwner().setPositionAndRotation(pos.x, pos.y, pos.z, yaw, pitch);
		this.getOwner().setRotationYawHead(yawHead);
	}

	public boolean isPossessing()
	{
		return this.syncManager.get(POSSESSION_ENTITY_ID) > 0;
	}

	public LivingEntity getPossession()
	{
		int possessionID = this.syncManager.get(POSSESSION_ENTITY_ID);

		if (possessionID <= 0)
			return null;

		return (LivingEntity) this.getOwner().getEntityWorld().getEntityByID(possessionID);
	}

	@Override
	public void notifySyncManagerChange(AbilityDataParameter<?> parameter)
	{
		super.notifySyncManagerChange(parameter);
		if (POSSESSION_ENTITY_ID.equals(parameter) && this.getOwner().getEntityWorld().isRemote())
		{
			AbilityMethods.ClientMethods.setPossessionEntityArg = this.isPossessing() ? this.getPossession() : this.getOwner();
			DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> AbilityMethods.ClientMethods::setPossessionEntity);
		}
	}

	protected void setPossessionEntity(LivingEntity entity)
	{
		this.syncManager.set(POSSESSION_ENTITY_ID, entity != null ? entity.getEntityId() : 0);
	}

	protected void setBodyEntity(Entity entity)
	{
		this.syncManager.set(BODY_ENTITY_ID, entity != null ? entity.getEntityId() : 0);
	}

	@SubscribeEvent
	public void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event)
	{
		if (!this.isPossessing())
			return;

		this.unpossessEntity();
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onPlayerClientLeave(ClientPlayerNetworkEvent.LoggedOutEvent event)
	{
		if (!this.isPossessing())
			return;

		this.unpossessEntity();
	}

	@SubscribeEvent
	public void onHandRender(RenderHandEvent event)
	{
		if (!this.isPossessing())
			return;

		event.setCanceled(true);
	}

	@SubscribeEvent
	public void onPlayerRender(RenderPlayerEvent.Pre event)
	{
		if (!this.isPossessing())
			return;

		event.setCanceled(true);
	}

	@Override
	public int getCooldown(String keybind)
	{
		return 0;
	}


	@Override
	public int getKeyCode()
	{
		return KeyEvent.VK_O;
	}

	public static Capability<Possessed> getPossessedCapability()
	{
		return AbilityMethods.POSSESSED_CAPABILITY;
	}

	static {
        try
        {
            ASMUtils.cacheMethod(registerGoals, MobEntity.class.getDeclaredMethod("registerGoals"));
        } catch (NoSuchMethodException e)
        {
            throw new RuntimeException(e);
        }
    }
}
