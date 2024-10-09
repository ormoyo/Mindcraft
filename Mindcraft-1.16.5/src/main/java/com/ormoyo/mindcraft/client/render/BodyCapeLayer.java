package com.ormoyo.mindcraft.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.ormoyo.mindcraft.entity.PlayerBodyEntity;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

public class BodyCapeLayer extends LayerRenderer<PlayerBodyEntity, PlayerModel<PlayerBodyEntity>>
{
    public BodyCapeLayer(IEntityRenderer<PlayerBodyEntity, PlayerModel<PlayerBodyEntity>> entityRendererIn)
    {
        super(entityRendererIn);
    }

    @Override
    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, PlayerBodyEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
    {
        PlayerEntity owner = entity.getOwner();
        AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) owner;

        if (player.hasPlayerInfo() && !player.isInvisible() && player.isWearing(PlayerModelPart.CAPE) && player.getLocationCape() != null)
        {
            ItemStack itemstack = player.getItemStackFromSlot(EquipmentSlotType.CHEST);
            if (itemstack.getItem() != Items.ELYTRA)
            {
                matrixStackIn.push();

                matrixStackIn.translate(0.0D, 0.0D, 0.125D);

                double d0 = 0;
                double d1 = 0;
                double d2 = 0;

                float f = entity.prevRenderYawOffset + (entity.renderYawOffset - entity.prevRenderYawOffset);
                double d3 = MathHelper.sin(f * ((float)Math.PI / 180F));
                double d4 = -MathHelper.cos(f * ((float)Math.PI / 180F));

                float f1 = (float)d1 * 10.0F;
                f1 = MathHelper.clamp(f1, -6.0F, 32.0F);
                float f2 = (float)(d0 * d3 + d2 * d4) * 100.0F;
                f2 = MathHelper.clamp(f2, 0.0F, 150.0F);
                float f3 = (float)(d0 * d4 - d2 * d3) * 100.0F;
                f3 = MathHelper.clamp(f3, -20.0F, 20.0F);

                if (f2 < 0.0F)
                    f2 = 0.0F;

                float f4 = entity.rotationYawHead;
                f1 = 0;

                matrixStackIn.rotate(Vector3f.XP.rotationDegrees(6.0F + f2 / 2.0F + f1));
                matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(f3 / 2.0F));
                matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180.0F - f3 / 2.0F));

                IVertexBuilder ivertexbuilder = bufferIn.getBuffer(RenderType.getEntitySolid(player.getLocationCape()));
                this.getEntityModel().renderCape(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY);

                matrixStackIn.pop();
            }
        }
    }
}
