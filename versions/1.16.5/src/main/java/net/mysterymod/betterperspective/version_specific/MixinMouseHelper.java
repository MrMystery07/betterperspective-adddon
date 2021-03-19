package net.mysterymod.betterperspective.version_specific;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHelper;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.util.MouseSmoother;
import net.minecraft.client.util.NativeUtil;
import net.mysterymod.betterperspective.BetterPerspectiveAddon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(MouseHelper.class)
public class MixinMouseHelper {

  @Shadow
  private final MouseSmoother xSmoother = new MouseSmoother();
  @Shadow
  private final MouseSmoother ySmoother = new MouseSmoother();
  @Shadow
  private double xVelocity;
  @Shadow
  private double yVelocity;
  @Shadow
  private double lastLookTime = Double.MIN_VALUE;
  @Shadow

  @Inject(method = "updatePlayerLook", at = @At("HEAD"), cancellable = true)
  public void updatePitchYaw(CallbackInfo callbackInfo) {
    Minecraft minecraft = Minecraft.getInstance();
    if (Minecraft.getInstance().currentScreen == null && Minecraft.getInstance().player != null && BetterPerspectiveAddon.INSTANCE.perspectiveEnabled) {
      BetterPerspectiveAddon.INSTANCE.cameraYaw = (float) (Minecraft.getInstance().mouseHelper.getMouseX() / 8.0F);
      BetterPerspectiveAddon.INSTANCE.cameraPitch = (float) (Minecraft.getInstance().mouseHelper.getMouseY() / 8.0F);

      if (Math.abs(BetterPerspectiveAddon.INSTANCE.cameraPitch) > 90.0F) {
        BetterPerspectiveAddon.INSTANCE.cameraPitch = BetterPerspectiveAddon.INSTANCE.cameraPitch > 0.0F ? 90.0F : -90.0F;
      }
    }

    double d0 = NativeUtil.getTime();
    double d1 = d0 - this.lastLookTime;
    this.lastLookTime = d0;
    if (Minecraft.getInstance().mouseHelper.isMouseGrabbed() && minecraft.isGameFocused()) {
      double d4 = minecraft.gameSettings.mouseSensitivity * (double)0.6F + (double)0.2F;
      double d5 = d4 * d4 * d4 * 8.0D;
      double d2;
      double d3;
      if (minecraft.gameSettings.smoothCamera) {
        double d6 = this.xSmoother.smooth(this.xVelocity * d5, d1 * d5);
        double d7 = this.ySmoother.smooth(this.yVelocity * d5, d1 * d5);
        d2 = d6;
        d3 = d7;
      } else {
        this.xSmoother.reset();
        this.ySmoother.reset();
        d2 = this.xVelocity * d5;
        d3 = this.yVelocity * d5;
      }

      this.xVelocity = 0.0D;
      this.yVelocity = 0.0D;
      int i = 1;
      if (minecraft.gameSettings.invertMouse) {
        i = -1;
      }

      minecraft.getTutorial().onMouseMove(d2, d3);
      if (minecraft.player != null) {

        if(BetterPerspectiveAddon.INSTANCE.perspectiveEnabled){
          //this.minecraft.player.rotateTowards(d2, d3 * (double)i);
        }else{
          minecraft.player.rotateTowards(d2, d3 * (double)i);
        }

      }

    } else {
      this.xVelocity = 0.0D;
      this.yVelocity = 0.0D;
    }
    callbackInfo.cancel();
  }
}
