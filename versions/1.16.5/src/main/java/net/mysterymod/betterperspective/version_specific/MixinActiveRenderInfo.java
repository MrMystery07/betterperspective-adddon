package net.mysterymod.betterperspective.version_specific;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockReader;
import net.mysterymod.betterperspective.BetterPerspectiveAddon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(ActiveRenderInfo.class)
public class MixinActiveRenderInfo {
  @Shadow
  private boolean valid;
  @Shadow
  private IBlockReader world;
  @Shadow
  private Entity renderViewEntity;
  @Shadow
  private Vector3d pos = Vector3d.ZERO;
  @Shadow
  private final BlockPos.Mutable blockPos = new BlockPos.Mutable();
  @Shadow
  private final Vector3f look = new Vector3f(0.0F, 0.0F, 1.0F);
  @Shadow
  private final Vector3f up = new Vector3f(0.0F, 1.0F, 0.0F);
  @Shadow
  private final Vector3f left = new Vector3f(1.0F, 0.0F, 0.0F);
  @Shadow
  private float pitch;
  @Shadow
  private float yaw;
  @Shadow
  private final Quaternion rotation = new Quaternion(0.0F, 0.0F, 0.0F, 1.0F);
  @Shadow
  private boolean thirdPerson;
  @Shadow
  private boolean thirdPersonReverse;
  @Shadow
  private float height;
  @Shadow
  private float previousHeight;


  @Inject(method = "update", at = @At(value = "HEAD"), cancellable = true)
  private void perspectiveUpdatePitchYaw(IBlockReader worldIn, Entity renderViewEntity, boolean thirdPersonIn, boolean thirdPersonReverseIn, float partialTicks, CallbackInfo info) {
    if (BetterPerspectiveAddon.INSTANCE.perspectiveEnabled) {
      Minecraft.getInstance().gameSettings.setPointOfView(PointOfView.THIRD_PERSON_BACK);
      this.valid = true;
      this.world = worldIn;
      this.renderViewEntity = renderViewEntity;
      this.thirdPerson = thirdPersonIn;
      this.thirdPersonReverse = thirdPersonReverseIn;
      this.setDirection(BetterPerspectiveAddon.cameraYaw, BetterPerspectiveAddon.cameraPitch);
      this.setPosition(MathHelper.lerp((double)partialTicks, renderViewEntity.prevPosX, renderViewEntity.getPosX()), MathHelper.lerp((double)partialTicks, renderViewEntity.prevPosY, renderViewEntity.getPosY()) + (double)MathHelper.lerp(partialTicks, this.previousHeight, this.height), MathHelper.lerp((double)partialTicks, renderViewEntity.prevPosZ, renderViewEntity.getPosZ()));
      if (thirdPersonIn) {
        if (thirdPersonReverseIn) {
          this.setDirection(this.yaw + 180.0F, -this.pitch);
        }

        this.movePosition(-this.calcCameraDistance(4.0D), 0.0D, 0.0D);
      } else if (renderViewEntity instanceof LivingEntity && ((LivingEntity)renderViewEntity).isSleeping()) {
        Direction direction = ((LivingEntity)renderViewEntity).getBedDirection();
        this.setDirection(direction != null ? direction.getHorizontalAngle() - 180.0F : 0.0F, 0.0F);
        this.movePosition(0.0D, 0.3D, 0.0D);
      }
      info.cancel();
    }
  }
  protected double calcCameraDistance(double startingDistance) {
    for(int i = 0; i < 8; ++i) {
      float f = (float)((i & 1) * 2 - 1);
      float f1 = (float)((i >> 1 & 1) * 2 - 1);
      float f2 = (float)((i >> 2 & 1) * 2 - 1);
      f = f * 0.1F;
      f1 = f1 * 0.1F;
      f2 = f2 * 0.1F;
      Vector3d vector3d = this.pos.add((double)f, (double)f1, (double)f2);
      Vector3d vector3d1 = new Vector3d(this.pos.x - (double)this.look.getX() * startingDistance + (double)f + (double)f2, this.pos.y - (double)this.look.getY() * startingDistance + (double)f1, this.pos.z - (double)this.look.getZ() * startingDistance + (double)f2);
      RayTraceResult raytraceresult = this.world.rayTraceBlocks(new RayTraceContext(vector3d, vector3d1, RayTraceContext.BlockMode.VISUAL, RayTraceContext.FluidMode.NONE, this.renderViewEntity));
      if (raytraceresult.getType() != RayTraceResult.Type.MISS) {
        double d0 = raytraceresult.getHitVec().distanceTo(this.pos);
        if (d0 < startingDistance) {
          startingDistance = d0;
        }
      }
    }

    return startingDistance;
  }
  protected void movePosition(double distanceOffset, double verticalOffset, double horizontalOffset) {
    double d0 = (double)this.look.getX() * distanceOffset + (double)this.up.getX() * verticalOffset + (double)this.left.getX() * horizontalOffset;
    double d1 = (double)this.look.getY() * distanceOffset + (double)this.up.getY() * verticalOffset + (double)this.left.getY() * horizontalOffset;
    double d2 = (double)this.look.getZ() * distanceOffset + (double)this.up.getZ() * verticalOffset + (double)this.left.getZ() * horizontalOffset;
    this.setPosition(new Vector3d(this.pos.x + d0, this.pos.y + d1, this.pos.z + d2));
  }

  protected void setDirection(float pitchIn, float yawIn) {
    this.pitch = yawIn;
    this.yaw = pitchIn;
    this.rotation.set(0.0F, 0.0F, 0.0F, 1.0F);
    this.rotation.multiply(Vector3f.YP.rotationDegrees(-pitchIn));
    this.rotation.multiply(Vector3f.XP.rotationDegrees(yawIn));
    this.look.set(0.0F, 0.0F, 1.0F);
    this.look.transform(this.rotation);
    this.up.set(0.0F, 1.0F, 0.0F);
    this.up.transform(this.rotation);
    this.left.set(1.0F, 0.0F, 0.0F);
    this.left.transform(this.rotation);
  }
  protected void setPosition(double x, double y, double z) {
    this.setPosition(new Vector3d(x, y, z));
  }

  protected void setPosition(Vector3d posIn) {
    this.pos = posIn;
    this.blockPos.setPos(posIn.x, posIn.y, posIn.z);
  }

}
