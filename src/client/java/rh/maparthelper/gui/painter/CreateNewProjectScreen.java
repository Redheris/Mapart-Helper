package rh.maparthelper.gui.painter;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import org.jetbrains.annotations.NotNull;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.conversion.CurrentConversionSettings;
import rh.maparthelper.gui.widget.input.IntegerFieldWidget;
import rh.maparthelper.state.painter.MapartPainterState;
import rh.maparthelper.util.RenderUtils;

public class CreateNewProjectScreen extends Screen {
    private final Screen parent;
    private final NativeImage currentMapartImage;
    private Button createProjectBtn;
    private LinearLayout content;
    private IntegerFieldWidget xSizeField;
    private IntegerFieldWidget ySizeField;
    private boolean createEmptyProject;

    public CreateNewProjectScreen(Screen parent) {
        super(Component.translatable("maparthelper.gui.screen.create_new_painter_project"));
        this.parent = parent;
        DynamicTexture mapartTexture = CurrentConversionSettings.guiMapartImage;
        this.currentMapartImage = mapartTexture == null ? null : mapartTexture.getPixels();
        this.createEmptyProject = currentMapartImage == null;
    }

    @Override
    protected void init() {
        content = LinearLayout.vertical().spacing(-4);
        content.defaultCellSetting().padding(4);

        final int maxSize = MapartHelper.commonConfig().maxMapartSize;

        LinearLayout createTypeLayout = LinearLayout.horizontal();
        createTypeLayout.defaultCellSetting().alignVerticallyMiddle();

        Component emptyProjectOption = Component.translatable("maparthelper.gui.mapart_painter.project.empty_projectoption");
        Component withCurrentMapartOption = Component.translatable("maparthelper.gui.mapart_painter.project.with_current_mapartoption");
        createTypeLayout.addChild(labelWidget(Component.translatable("maparthelper.gui.mapart_painter.project.label_create")));
        Button createTypeOption = Button.builder(
                createEmptyProject ? emptyProjectOption : withCurrentMapartOption,
                btn -> {
                    createEmptyProject = !createEmptyProject;
                    btn.setMessage(createEmptyProject ? emptyProjectOption : withCurrentMapartOption);
                    updateFieldsState();
                }
        ).size(160, 20).build();
        createTypeLayout.addChild(createTypeOption);

        LinearLayout xSizeLayout = LinearLayout.horizontal().spacing(2);
        xSizeLayout.defaultCellSetting().alignVerticallyMiddle();
        xSizeLayout.addChild(labelWidget(Component.literal("x:")));
        xSizeField = new IntegerFieldWidget(
                font, 40, 20,
                Math.max(1, CurrentConversionSettings.getMapartWidth()),
                1, maxSize
        );
        xSizeLayout.addChild(xSizeField);

        LinearLayout ySizeLayout = LinearLayout.horizontal().spacing(2);
        ySizeLayout.defaultCellSetting().alignVerticallyMiddle();
        ySizeLayout.addChild(labelWidget(Component.literal("y:")));
        ySizeField = new IntegerFieldWidget(
                font, 40, 20,
                Math.max(1, CurrentConversionSettings.getMapartHeight()),
                1, maxSize
        );
        ySizeLayout.addChild(ySizeField);

        content.addChild(createTypeLayout, content.newCellSettings().paddingBottom(16));
        content.addChild(labelWidget(Component.translatable("maparthelper.gui.mapart_painter.project.size_in_maps")));
        content.addChild(xSizeLayout);
        content.addChild(ySizeLayout);
        content.arrangeElements();
        GridLayout submitButtons = createSubmitButtonsLayout(createTypeLayout.getWidth() - 8);
        content.addChild(submitButtons, content.newCellSettings().paddingTop(16));
        content.arrangeElements();

        updateFieldsState();
        content.visitWidgets(this::addRenderableWidget);

        content.setPosition(
                (width - content.getWidth()) / 2,
                (height - content.getHeight()) / 2
        );
    }

    private GridLayout createSubmitButtonsLayout(int width) {
        GridLayout submitButtons = new GridLayout().columnSpacing(4);
        submitButtons.defaultCellSetting().alignVerticallyMiddle().alignHorizontallyCenter();
        GridLayout.RowHelper buttonsAdder = submitButtons.createRowHelper(2);
        buttonsAdder.addChild(SpacerElement.width(width), 2);

        Button cancelBtn = Button.builder(
                Component.translatable("maparthelper.gui.mapart_painter.project.cancel"),
                btn -> Minecraft.getInstance().setScreen(parent)
        ).size(60, 20).build();
        createProjectBtn = Button.builder(
                Component.translatable("maparthelper.gui.mapart_painter.project.create_project"),
                btn -> {
                    if (createEmptyProject) {
                        MapartPainterState.getInstance().newPainterProject(
                                xSizeField.getIntValue(), ySizeField.getIntValue()
                        );
                    } else {
                        MapartPainterState.getInstance().newPainterProject(currentMapartImage);
                    }
                    Minecraft.getInstance().setScreen(new PainterScreen());
                }
        ).size(100, 20).build();

        buttonsAdder.addChild(cancelBtn, submitButtons.newCellSettings().alignHorizontallyLeft());
        buttonsAdder.addChild(createProjectBtn, submitButtons.newCellSettings().alignHorizontallyRight());

        return submitButtons;
    }

    private void updateFieldsState() {
        if (createEmptyProject) {
            xSizeField.active = true;
            ySizeField.active = true;
            createProjectBtn.active = true;

            xSizeField.setTooltip(null);
            ySizeField.setTooltip(null);
            createProjectBtn.setTooltip(null);

            xSizeField.setIntValue(1);
            ySizeField.setIntValue(1);

            xSizeField.setTextColor(-1);
            ySizeField.setTextColor(-1);
        } else {
            xSizeField.active = false;
            ySizeField.active = false;
            Tooltip tooltip = Tooltip.create(Component.translatable("maparthelper.gui.mapart_painter.project.size_is_locked_to_mapart"));
            xSizeField.setTooltip(tooltip);
            ySizeField.setTooltip(tooltip);
            xSizeField.setIntValue(Math.max(1, CurrentConversionSettings.getMapartWidth()));
            ySizeField.setIntValue(Math.max(1, CurrentConversionSettings.getMapartHeight()));
            xSizeField.setTextColor(CommonColors.LIGHT_GRAY);
            ySizeField.setTextColor(CommonColors.LIGHT_GRAY);

            createProjectBtn.active = currentMapartImage != null;
            if (!createProjectBtn.active) {
                createProjectBtn.setTooltip(Tooltip.create(
                        Component.translatable("maparthelper.gui.mapart_has_no_converted_image")
                ));
            }
        }
        if (createProjectBtn.active && MapartPainterState.getInstance().painterProjectExists()) {
            createProjectBtn.setTooltip(Tooltip.create(
                    Component.translatable("maparthelper.gui.mapart_painter.project.create_project.tooltip")
            ));
        }
    }

    //~ gui_rendering
    @Override
    public void renderBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);

        graphics.fill(
                content.getX(),
                content.getY(),
                content.getX() + content.getWidth(),
                content.getY() + content.getHeight(),
                ARGB.color(0.467f, 0)
        );

        RenderUtils.renderOutline(graphics,
                content.getX() - 1,
                content.getY() - 1,
                content.getWidth() + 2,
                content.getHeight() + 2,
                ARGB.white(0.134f)
        );
    }
    //~ !gui_rendering

    private StringWidget labelWidget(Component text) {
        return new StringWidget(text, font);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }
}
