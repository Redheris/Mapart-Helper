package rh.maparthelper.gui.tooltip;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector2ic;

public class StandardTooltipPositioner implements ClientTooltipPositioner {
    public static final ClientTooltipPositioner INSTANCE = new StandardTooltipPositioner();

    private StandardTooltipPositioner() {}

    @Override
    public @NotNull Vector2ic positionTooltip(int screenWidth, int screenHeight, int mouseX, int mouseY, int tooltipWidth, int tooltipHeight) {
        Vector2i vector2i = new Vector2i(mouseX, mouseY).add(12, -(tooltipHeight / 2) + 4);
        this.positionTooltip(screenWidth, screenHeight, vector2i, tooltipWidth, tooltipHeight);
        return vector2i;
    }

    private void positionTooltip(int screenWidth, int screenHeight, Vector2i tooltipPos, int tooltipWidth, int tooltipHeight) {
        if (tooltipPos.x + tooltipWidth > screenWidth) {
            tooltipPos.x = Math.max(tooltipPos.x - 24 - tooltipWidth, 4);
        }

        if (tooltipPos.y < 4) {
            tooltipPos.y = 4;
        }

        int i = tooltipHeight + 3;
        if (tooltipPos.y + i > screenHeight) {
            tooltipPos.y = screenHeight - i;
        }
    }
}
