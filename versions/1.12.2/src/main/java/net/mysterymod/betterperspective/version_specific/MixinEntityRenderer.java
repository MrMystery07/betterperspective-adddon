package net.mysterymod.betterperspective.version_specific;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.init.Blocks;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.mysterymod.betterperspective.BetterPerspectiveAddon;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({EntityRenderer.class})
public class MixinEntityRenderer{

  @Inject(method = {"updateCameraAndRender"}, at = {@At(value = "HEAD")})
  private void perspectiveCameraUpdatingSmooth(CallbackInfo  callbackInfo) {
    if(Minecraft.getMinecraft().inGameHasFocus && Display.isActive() && BetterPerspectiveAddon.INSTANCE.perspectiveEnabled) {
      Minecraft.getMinecraft().gameSettings.thirdPersonView = 1;
      Minecraft.getMinecraft().mouseHelper.mouseXYChange();
      float f1 = Minecraft.getMinecraft().gameSettings.mouseSensitivity * 0.6F + 0.2F;
      float f2 = f1 * f1 * f1 * 8.0F;
      float f3 = (float) Minecraft.getMinecraft().mouseHelper.deltaX * f2;
      float f4 = (float) Minecraft.getMinecraft().mouseHelper.deltaY * f2;

      BetterPerspectiveAddon.INSTANCE.cameraYaw += f3 * 0.15F;
      BetterPerspectiveAddon.INSTANCE.cameraPitch += f4 * 0.15F;

      if (BetterPerspectiveAddon.INSTANCE.cameraPitch > 90) BetterPerspectiveAddon.INSTANCE.cameraPitch = 90;
      if (BetterPerspectiveAddon.INSTANCE.cameraPitch < -90) BetterPerspectiveAddon.INSTANCE.cameraPitch = -90;

    }else{
      BetterPerspectiveAddon.INSTANCE.perspectiveEnabled = false;
    }
    if(BetterPerspectiveAddon.INSTANCE.mustReturnToPreviusPerpsective){
      Minecraft.getMinecraft().gameSettings.thirdPersonView = 0;
      BetterPerspectiveAddon.INSTANCE.mustReturnToPreviusPerpsective = false;
    }
  }

  @Inject(method = "orientCamera", at = @At("HEAD"), cancellable = true)
  public void injectOrientCamera(float partialTicks, CallbackInfo callbackInfo) {
    if(Minecraft.getMinecraft().inGameHasFocus && Display.isActive() && BetterPerspectiveAddon.INSTANCE.perspectiveEnabled){

      Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
      float f = entity.getEyeHeight();
      double d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * (double)partialTicks;
      double d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * (double)partialTicks + (double)f;
      double d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double)partialTicks;

      if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).isPlayerSleeping())
      {
        f = (float)((double)f + 1.0D);
        GlStateManager.translate(0.0F, 0.3F, 0.0F);

        if (!Minecraft.getMinecraft().gameSettings.debugCamEnable)
        {
          BlockPos blockpos = new BlockPos(entity);
          IBlockState iblockstate = Minecraft.getMinecraft().world.getBlockState(blockpos);

          GlStateManager.rotate(BetterPerspectiveAddon.INSTANCE.cameraYaw+ (BetterPerspectiveAddon.INSTANCE.cameraYaw - BetterPerspectiveAddon.INSTANCE.cameraYaw) * partialTicks + 180.0F, 0.0F, -1.0F, 0.0F);
          GlStateManager.rotate(BetterPerspectiveAddon.INSTANCE.cameraPitch + (BetterPerspectiveAddon.INSTANCE.cameraPitch - BetterPerspectiveAddon.INSTANCE.cameraPitch) * partialTicks, -1.0F, 0.0F, 0.0F);
        }
      }
      else if (Minecraft.getMinecraft().gameSettings.thirdPersonView > 0)
      {
        double d3 = (double)(4.0F+ (4.0F - 4.0F) * partialTicks);

        if (Minecraft.getMinecraft().gameSettings.debugCamEnable)
        {
          GlStateManager.translate(0.0F, 0.0F, (float)(-d3));
        }
        else
        {
          float f1 = BetterPerspectiveAddon.INSTANCE.cameraYaw;
          float f2 = BetterPerspectiveAddon.INSTANCE.cameraPitch;

          if (Minecraft.getMinecraft().gameSettings.thirdPersonView == 2)
          {
            f2 += 180.0F;
          }

          double d4 = (double)(-MathHelper.sin(f1 * 0.017453292F) * MathHelper.cos(f2 * 0.017453292F)) * d3;
          double d5 = (double)(MathHelper.cos(f1 * 0.017453292F) * MathHelper.cos(f2 * 0.017453292F)) * d3;
          double d6 = (double)(-MathHelper.sin(f2 * 0.017453292F)) * d3;

          for (int i = 0; i < 8; ++i)
          {
            float f3 = (float)((i & 1) * 2 - 1);
            float f4 = (float)((i >> 1 & 1) * 2 - 1);
            float f5 = (float)((i >> 2 & 1) * 2 - 1);
            f3 = f3 * 0.1F;
            f4 = f4 * 0.1F;
            f5 = f5 * 0.1F;
            RayTraceResult raytraceresult = Minecraft.getMinecraft().world.rayTraceBlocks(new Vec3d(d0 + (double)f3, d1 + (double)f4, d2 + (double)f5), new Vec3d(d0 - d4 + (double)f3 + (double)f5, d1 - d6 + (double)f4, d2 - d5 + (double)f5));

            if (raytraceresult != null)
            {
              double d7 = raytraceresult.hitVec.distanceTo(new Vec3d(d0, d1, d2));

              if (d7 < d3)
              {
                d3 = d7;
              }
            }
          }

          if (Minecraft.getMinecraft().gameSettings.thirdPersonView == 2)
          {
            GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
          }

          GlStateManager.rotate(BetterPerspectiveAddon.INSTANCE.cameraPitch - f2, 1.0F, 0.0F, 0.0F);
          GlStateManager.rotate(BetterPerspectiveAddon.INSTANCE.cameraYaw - f1, 0.0F, 1.0F, 0.0F);
          GlStateManager.translate(0.0F, 0.0F, (float)(-d3));
          GlStateManager.rotate(f1 - BetterPerspectiveAddon.INSTANCE.cameraYaw, 0.0F, 1.0F, 0.0F);
          GlStateManager.rotate(f2 - BetterPerspectiveAddon.INSTANCE.cameraPitch, 1.0F, 0.0F, 0.0F);
        }
      }
      else
      {
        GlStateManager.translate(0.0F, 0.0F, 0.05F);
      }

      if (!Minecraft.getMinecraft().gameSettings.debugCamEnable)
      {
        float yaw = BetterPerspectiveAddon.INSTANCE.cameraYaw+ (entity.rotationYaw - BetterPerspectiveAddon.INSTANCE.cameraYaw) * partialTicks + 180.0F;
        float pitch = BetterPerspectiveAddon.INSTANCE.cameraPitch + (BetterPerspectiveAddon.INSTANCE.cameraPitch - BetterPerspectiveAddon.INSTANCE.cameraPitch) * partialTicks;
        float roll = 0.0F;
        if (entity instanceof EntityAnimal)
        {
          EntityAnimal entityanimal = (EntityAnimal)entity;
          yaw = entityanimal.prevRotationYawHead + (entityanimal.rotationYawHead - entityanimal.prevRotationYawHead) * partialTicks + 180.0F;
        }
        IBlockState state = ActiveRenderInfo.getBlockStateAtEntityViewpoint(Minecraft.getMinecraft().world, entity, partialTicks);
        GlStateManager.rotate(BetterPerspectiveAddon.INSTANCE.cameraPitch, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(BetterPerspectiveAddon.INSTANCE.cameraYaw, 0.0F, 1.0F, 0.0F);
      }

      GlStateManager.translate(0.0F, -f, 0.0F);
      d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * (double)partialTicks;
      d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * (double)partialTicks + (double)f;
      d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double)partialTicks;

      callbackInfo.cancel();
    }else{
      BetterPerspectiveAddon.INSTANCE.perspectiveEnabled = false;
    }
  }
}

