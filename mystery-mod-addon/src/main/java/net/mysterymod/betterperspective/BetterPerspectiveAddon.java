package net.mysterymod.betterperspective;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.mysterymod.api.event.EventHandler;
import net.mysterymod.api.event.game.GameTickEvent;
import net.mysterymod.api.input.Keyboard;
import net.mysterymod.api.listener.ListenerChannel;
import net.mysterymod.api.minecraft.KeyCode;
import net.mysterymod.mod.MysteryMod;
import net.mysterymod.mod.addon.Addon;

import java.util.logging.Logger;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class BetterPerspectiveAddon extends Addon {
  private final Logger logger;
  public static BetterPerspectiveAddon INSTANCE;

  public boolean pauseKey = false;

  public Keyboard keyBoard;

  public boolean perspectiveEnabled = false;

  public static float cameraYaw = 0F;
  public static float cameraPitch = 0F;
  public static int previousPerspective = 0;

  public static boolean mustReturnToPreviusPerpsective = false;

  public static float deltaX;
  public static float deltaY;
  public static float mouseSensitivity;
  public static boolean mouseXYChange;
  public static boolean isIngame = false;
  public static BetterPerspectiveConfig config = MysteryMod.getInjector().getInstance(BetterPerspectiveConfig.class);
  public static float oldCameraYaw = 0;
  public static float oldCameraPitch = 0;
  @Override
  public void onEnable() {
    INSTANCE = this;
    keyBoard = MysteryMod.getInjector().getInstance(Keyboard.class);
    ListenerChannel listenerChannel = MysteryMod.getInjector().getInstance(ListenerChannel.class);
    listenerChannel.registerListener(this);
    setSettingsProvider(MysteryMod.getInjector().getInstance(BetterPerspectiveProvider.class));
  }
  @EventHandler
  public void onTick(GameTickEvent e){
    try {
      if (e.isIngame()) {
        isIngame = true;
        if (config.isEnabled()) {
          if (!config.isHold()) {
            if (keyBoard.isKeyDown(config.getHotkey().getValue()) && pauseKey == false) {
              perspectiveEnabled = !perspectiveEnabled;
              if (!perspectiveEnabled) {
                mustReturnToPreviusPerpsective = true;
              }
              pauseKey = true;
              System.out.println(perspectiveEnabled);
            } else if (pauseKey == true) {
              pauseKey = false;
            }
          } else {
            if (keyBoard.isKeyDown(config.getHotkey().getValue())) {
              perspectiveEnabled = true;
            } else {
              if (perspectiveEnabled) {
                mustReturnToPreviusPerpsective = true;
              }
              perspectiveEnabled = false;
            }
          }
        }
      } else {
        isIngame = false;
      }
    }catch (Exception e1){

    }
  }

  public static float getCameraYaw()
  {
    return BetterPerspectiveAddon.INSTANCE.perspectiveEnabled ? cameraYaw : oldCameraYaw;
  }

  public static float getCameraPitch()
  {
    return BetterPerspectiveAddon.INSTANCE.perspectiveEnabled ? cameraPitch : oldCameraPitch;
  }

}
