package net.mysterymod.betterperspective.version_specific;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.PointOfView;
import net.mysterymod.betterperspective.BetterPerspectiveAddon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {
  @Inject(method = "runTick", at = @At(value = "HEAD"))
  private void injectRunTick(CallbackInfo callbackInfo){
    if(BetterPerspectiveAddon.INSTANCE.mustReturnToPreviusPerpsective){
      Minecraft.getInstance().gameSettings.setPointOfView(PointOfView.FIRST_PERSON);
      BetterPerspectiveAddon.INSTANCE.mustReturnToPreviusPerpsective = false;
    }
  }

}
