package com.ormoyo.mindcraft.client.render;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.ormoyo.mindcraft.entity.PlayerBodyEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.UseAction;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nonnull;
import java.util.Map;

public class PlayerBodyRenderer extends LivingRenderer<PlayerBodyEntity, PlayerModel<PlayerBodyEntity>>
{
    private final Map<String, PlayerModel<PlayerBodyEntity>> bodySkinMap = Maps.newHashMap();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public PlayerBodyRenderer(EntityRendererManager renderManager)
    {
        super(renderManager, new PlayerModel<>(0.0f, false), 0.5f);

        this.bodySkinMap.put("default", this.entityModel);
        this.bodySkinMap.put("slim", new PlayerModel<>(0.0f, true));

        this.addLayer(new BipedArmorLayer<>(this, new BipedModel(0.5F), new BipedModel(1.0F)));
        this.addLayer(new HeldItemLayer<>(this));
        this.addLayer(new ArrowLayer<>(this));
        this.addLayer(new BodyCapeLayer(this));
        this.addLayer(new HeadLayer<>(this));
        this.addLayer(new ElytraLayer<>(this));
        this.addLayer(new SpinAttackEffectLayer<>(this));
        this.addLayer(new BeeStingerLayer<>(this));
    }

    @Override
    public void render(PlayerBodyEntity entity, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn)
    {
        AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) entity.getOwner();
        this.entityModel = this.bodySkinMap.get(player.getSkinType());

        this.setModelVisibilities(player);
        super.render(entity, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    private void setModelVisibilities(AbstractClientPlayerEntity clientPlayer)
    {
        PlayerModel<PlayerBodyEntity> playermodel = this.getEntityModel();
        if (clientPlayer.isSpectator())
        {
            playermodel.setVisible(false);

            playermodel.bipedHead.showModel = true;
            playermodel.bipedHeadwear.showModel = true;

            return;
        }

        playermodel.setVisible(true);

        playermodel.bipedHeadwear.showModel = clientPlayer.isWearing(PlayerModelPart.HAT);
        playermodel.bipedBodyWear.showModel = clientPlayer.isWearing(PlayerModelPart.JACKET);
        playermodel.bipedLeftLegwear.showModel = clientPlayer.isWearing(PlayerModelPart.LEFT_PANTS_LEG);
        playermodel.bipedRightLegwear.showModel = clientPlayer.isWearing(PlayerModelPart.RIGHT_PANTS_LEG);
        playermodel.bipedLeftArmwear.showModel = clientPlayer.isWearing(PlayerModelPart.LEFT_SLEEVE);
        playermodel.bipedRightArmwear.showModel = clientPlayer.isWearing(PlayerModelPart.RIGHT_SLEEVE);
        playermodel.isSneak = false;

        BipedModel.ArmPose bipedmodel$armpose = getArmPose(clientPlayer, Hand.MAIN_HAND);
        BipedModel.ArmPose bipedmodel$armpose1 = getArmPose(clientPlayer, Hand.OFF_HAND);

        if (bipedmodel$armpose.func_241657_a_())
            bipedmodel$armpose1 = clientPlayer.getHeldItemOffhand().isEmpty() ? BipedModel.ArmPose.EMPTY : BipedModel.ArmPose.ITEM;

        if (clientPlayer.getPrimaryHand() == HandSide.RIGHT)
        {
            playermodel.rightArmPose = bipedmodel$armpose;
            playermodel.leftArmPose = bipedmodel$armpose1;

            return;
        }

        playermodel.rightArmPose = bipedmodel$armpose1;
        playermodel.leftArmPose = bipedmodel$armpose;
    }

    private static BipedModel.ArmPose getArmPose(AbstractClientPlayerEntity p_241741_0_, Hand p_241741_1_)
    {
        ItemStack itemstack = p_241741_0_.getHeldItem(p_241741_1_);

        if (itemstack.isEmpty())
            return BipedModel.ArmPose.EMPTY;

        if (p_241741_0_.getActiveHand() == p_241741_1_ && p_241741_0_.getItemInUseCount() > 0)
            switch (itemstack.getUseAction())
            {
                case BLOCK:
                    return BipedModel.ArmPose.BLOCK;
                case BOW:
                    return BipedModel.ArmPose.BOW_AND_ARROW;
                case SPEAR:
                    return BipedModel.ArmPose.THROW_SPEAR;
                case CROSSBOW:
                    if (p_241741_1_ == p_241741_0_.getActiveHand())
                        return BipedModel.ArmPose.CROSSBOW_CHARGE;
            }
        else if (!p_241741_0_.isSwingInProgress && itemstack.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(itemstack))
            return BipedModel.ArmPose.CROSSBOW_HOLD;

        return BipedModel.ArmPose.ITEM;
    }

    @Override
    protected void preRenderCallback(PlayerBodyEntity entity, MatrixStack matrixStackIn, float partialTickTime)
    {
        float f = 0.9375F;
        matrixStackIn.scale(0.9375F, 0.9375F, 0.9375F);
    }

    @Override
    protected void renderName(PlayerBodyEntity entity, ITextComponent displayNameIn, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn)
    {
        AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) entity.getOwner();

        double d0 = this.renderManager.squareDistanceTo(entity);
        matrixStackIn.push();

        if (d0 < 100.0D)
        {
            Scoreboard scoreboard = player.getWorldScoreboard();
            ScoreObjective scoreobjective = scoreboard.getObjectiveInDisplaySlot(2);

            if (scoreobjective != null)
            {
                Score score = scoreboard.getOrCreateScore(entity.getScoreboardName(), scoreobjective);
                super.renderName(entity, (new StringTextComponent(Integer.toString(score.getScorePoints()))).appendString(" ").appendSibling(scoreobjective.getDisplayName()), matrixStackIn, bufferIn, packedLightIn);

                matrixStackIn.translate(0.0D, (double)(9.0F * 1.15F * 0.025F), 0.0D);
            }
        }

        super.renderName(entity, player.getDisplayName(), matrixStackIn, bufferIn, packedLightIn);
        matrixStackIn.pop();
    }

    public void renderRightArm(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, PlayerBodyEntity entity)
    {
        this.renderItem(matrixStackIn, bufferIn, combinedLightIn, entity, (this.entityModel).bipedRightArm, (this.entityModel).bipedRightArmwear);
    }

    public void renderLeftArm(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, PlayerBodyEntity entity)
    {
        this.renderItem(matrixStackIn, bufferIn, combinedLightIn, entity, (this.entityModel).bipedLeftArm, (this.entityModel).bipedLeftArmwear);
    }

    private void renderItem(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, PlayerBodyEntity entity, ModelRenderer rendererArmIn, ModelRenderer rendererArmwearIn)
    {
        AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) entity.getOwner();
        PlayerModel<PlayerBodyEntity> playermodel = this.getEntityModel();

        this.setModelVisibilities(player);

        playermodel.swingProgress = 0.0F;
        playermodel.isSneak = false;
        playermodel.swimAnimation = 0.0F;
        playermodel.setRotationAngles(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);

        rendererArmIn.rotateAngleX = 0.0F;
        rendererArmIn.render(matrixStackIn, bufferIn.getBuffer(RenderType.getEntitySolid(player.getLocationSkin())), combinedLightIn, OverlayTexture.NO_OVERLAY);
        rendererArmwearIn.rotateAngleX = 0.0F;
        rendererArmwearIn.render(matrixStackIn, bufferIn.getBuffer(RenderType.getEntityTranslucent(player.getLocationSkin())), combinedLightIn, OverlayTexture.NO_OVERLAY);
    }

    protected void applyRotations(PlayerBodyEntity entityLiving, MatrixStack matrixStackIn, float ageInTicks, float rotationYaw, float partialTicks)
    {
        float f = entityLiving.getSwimAnimation(partialTicks);
        if (entityLiving.isElytraFlying())
        {
            super.applyRotations(entityLiving, matrixStackIn, ageInTicks, rotationYaw, partialTicks);

            float f1 = (float)entityLiving.getTicksElytraFlying() + partialTicks;
            float f2 = MathHelper.clamp(f1 * f1 / 100f, 0f, 1f);

            if (!entityLiving.isSpinAttacking())
                matrixStackIn.rotate(Vector3f.XP.rotationDegrees(f2 * (-90f - entityLiving.rotationPitch)));

            Vector3d vector3d = entityLiving.getLook(partialTicks);
            Vector3d vector3d1 = entityLiving.getMotion();

            double d0 = Entity.horizontalMag(vector3d1);
            double d1 = Entity.horizontalMag(vector3d);

            if (d0 > 0.0 && d1 > 0.0)
            {
                double d2 = (vector3d1.x * vector3d.x + vector3d1.z * vector3d.z) / Math.sqrt(d0 * d1);
                double d3 = vector3d1.x * vector3d.z - vector3d1.z * vector3d.x;
                matrixStackIn.rotate(Vector3f.YP.rotation((float)(Math.signum(d3) * Math.acos(d2))));
            }

            return;
        }

        if (f > 0.0F)
        {
            super.applyRotations(entityLiving, matrixStackIn, ageInTicks, rotationYaw, partialTicks);

            float f3 = entityLiving.isInWater() ? -90f - entityLiving.rotationPitch : -90f;
            float f4 = MathHelper.lerp(f, 0.0F, f3);

            matrixStackIn.rotate(Vector3f.XP.rotationDegrees(f4));

            if (entityLiving.isActualySwimming())
            {
                matrixStackIn.translate(0.0, -1.0, 0.3);
            }

            return;
        }

        super.applyRotations(entityLiving, matrixStackIn, ageInTicks, rotationYaw, partialTicks);
    }

    @Nonnull
    @Override
    public ResourceLocation getEntityTexture(PlayerBodyEntity entity)
    {
        AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) entity.getOwner();
        return player.getLocationSkin();
    }
}
