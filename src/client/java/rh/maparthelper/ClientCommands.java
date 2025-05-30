package rh.maparthelper;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.block.MapColor;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import rh.maparthelper.conversion.BlocksPalette;

import java.util.Arrays;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;


public class ClientCommands {

    public static void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(literal("mart")
                .executes(ctx -> {
                    // Commands list
                    return 1;
                })
                .then(literal("save")
                    .then(literal("hand")
                        .executes(ctx -> {
                            // Save image from the held FilledMapItem
                            assert ctx.getSource().getEntity() instanceof ClientPlayerEntity;
                            ClientPlayerEntity player = (ClientPlayerEntity) ctx.getSource().getEntity();
                            ItemStack itemStack = player.getMainHandStack();
                            if (!(itemStack.getItem() instanceof FilledMapItem))
                                itemStack = player.getOffHandStack();
                            if (!(itemStack.getItem() instanceof FilledMapItem)) {
                                player.sendMessage(Text.translatable(
                                        "maparthelper.is_holding_filled_map").withColor(Colors.LIGHT_RED),
                                        true);
                                return 0;
                            }
                            MapState mapState = FilledMapItem.getMapState(itemStack, player.getWorld());
                            MapartToFile.saveImageFromMapState(mapState);
                            return 1;
                        }))
                    .then(literal("frame")
                        .executes(ctx -> {
                            // Save image from the item frames the player is looking at
                            assert ctx.getSource().getEntity() instanceof ClientPlayerEntity;
                            ClientPlayerEntity player = (ClientPlayerEntity) ctx.getSource().getEntity();
                            MapState state = MapartToFile.getMapStateFromItemFrame();
                            if (state == null) {
                                player.sendMessage(Text.translatable(
                                        "maparthelper.is_looking_at_frame_with_map").withColor(Colors.LIGHT_RED),
                                        true);
                                return 0;
                            }
                            MapartToFile.saveImageFromMapState(state);
                            return 1;
                        }))
                    .then(literal("selection")
                        .executes(ctx -> {
                            // Save image from the selected area of item frames
                            // Check if the area selected

                            // Right now it's a blank code just for testing

                            assert ctx.getSource().getEntity() instanceof ClientPlayerEntity;
                            ClientPlayerEntity player = (ClientPlayerEntity) ctx.getSource().getEntity();
                            ItemStack itemStack = player.getMainHandStack();
                            if (!(itemStack.getItem() instanceof FilledMapItem))
                                itemStack = player.getOffHandStack();
                            if (!(itemStack.getItem() instanceof FilledMapItem)) {
                                player.sendMessage(Text.translatable(
                                        "maparthelper.is_holding_filled_map").withColor(Colors.LIGHT_RED),
                                        true);
                                return 0;
                            }
                            MapState mapState = FilledMapItem.getMapState(itemStack, player.getWorld());

                            if (mapState != null) {
                                int i = 0;
                                while (i < 128 * 128) {
//                                    System.out.println(Arrays.toString(mapState.colors));
                                    System.out.print(mapState.colors[i++]);
                                    if (i % 128 == 0)
                                        System.out.println();
                                }
                            }
                            return 1;
                        })))
                .then(literal("palette")
                        .then(literal("generate")
                            .executes(ctx -> {
                                // Regenerates blocks palette to correspond to the configs and game's blocks list
                                BlocksPalette.initColors();
                                return 1;
                            }))
                        .then(literal("test")
                            .executes(ctx -> {
                                // Also just a temporary testing code :)
                                MapColor color = MapColor.BLACK;
                                String list = Arrays.toString(BlocksPalette.getBlocksOfColor(color));
                                System.out.println(list);
                                ClientPlayerEntity player = (ClientPlayerEntity) ctx.getSource().getEntity();
                                player.sendMessage(Text.literal(list), false);
                                return 1;
                            }))
                )
            ));
    }
}
