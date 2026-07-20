package rh.maparthelper.mixin;

import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetTooltipHolder;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rh.maparthelper.gui.tooltip.MultilineTooltip;
import rh.maparthelper.gui.tooltip.StandardTooltipPositioner;

@Mixin(WidgetTooltipHolder.class)
public class WidgetTooltipHolderMixin {
    @Shadow
    private @Nullable Tooltip tooltip;

    @Inject(method = "createTooltipPositioner", at = @At("HEAD"), cancellable = true)
    private void createTooltipPositioner(ScreenRectangle screenRectangle, boolean hovering, boolean focused, CallbackInfoReturnable<ClientTooltipPositioner> cir) {
        if (tooltip instanceof MultilineTooltip) {
            cir.setReturnValue(StandardTooltipPositioner.INSTANCE);
        }
    }
}
