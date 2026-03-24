package rh.maparthelper.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import rh.maparthelper.config.MapartHelperConfig;

//? if <= 1.21.8 {
import me.shedaniel.autoconfig.AutoConfig;
//?} else
//import me.shedaniel.autoconfig.AutoConfigClient;

@Environment(EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        //? if <=1.21.8 {
        return parent -> AutoConfig.getConfigScreen(MapartHelperConfig.class, parent).get();
        //?} else {
        /*return parent -> AutoConfigClient.getConfigScreen(MapartHelperConfig.class, parent).get();
        *///?}
    }
}