package net.mysterymod.betterperspective;

import javafx.scene.control.Toggle;
import net.mysterymod.mod.MysteryMod;
import net.mysterymod.mod.addon.AddonSettingsProvider;
import net.mysterymod.mod.gui.settings.SettingsGui;
import net.mysterymod.mod.gui.settings.component.SettingsComponent;
import net.mysterymod.mod.gui.settings.component.SettingsComponentProvider;
import net.mysterymod.mod.gui.settings.component.input.KeybindComponent;
import net.mysterymod.mod.gui.settings.component.toggle.ToggleComponent;

import java.util.List;

public class BetterPerspectiveProvider implements AddonSettingsProvider {

  public BetterPerspectiveConfig config;

  @Override
  public void addSettings(SettingsGui settingsGui, SettingsComponentProvider settingsComponentProvider, List<SettingsComponent> list) {
    config = MysteryMod.getInjector().getInstance(BetterPerspectiveConfig.class);
    list.add(
      ToggleComponent.create(
        "Better Perspective",
        null,
        toggleState -> {
          // Toggle listener
          config.setEnabled(toggleState);
          config.saveConfig();
        },
        config.isEnabled()
      ));
    list.add(new KeybindComponent("Hotkey", null,
      config.getHotkey(), keyCode -> {
      config.setHotkey(keyCode);
      config.saveConfig();
    }));
    list.add(
      ToggleComponent.create(
        "Hotkey halten?",
        null,
        toggleState -> {
          // Toggle listener
          config.setHold(toggleState);
          config.saveConfig();
        },
        config.isHold()
      ));
  }
}

