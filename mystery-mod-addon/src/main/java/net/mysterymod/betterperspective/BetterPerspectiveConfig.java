package net.mysterymod.betterperspective;

import com.google.inject.Singleton;
import com.sun.org.apache.xpath.internal.objects.XBoolean;
import lombok.Getter;
import lombok.Setter;
import net.mysterymod.api.minecraft.KeyCode;
import net.mysterymod.mod.config.GsonConfig;

import java.io.File;
@Setter
@Getter
@Singleton
public class BetterPerspectiveConfig extends GsonConfig {

  public boolean enabled;
  public boolean hold;
  public KeyCode hotkey;

  public BetterPerspectiveConfig() {
    super(new File("MysteryMod/better_perspective.json"));
    this.initialize();
  }
}

