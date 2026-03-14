package rh.maparthelper.gui.input;

import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import rh.maparthelper.conversion.schematic.MapartSchematicBuilder;

import java.util.function.Predicate;

public class TextFieldValidators {
    public static Predicate<String> anyBlockIdentifier() {
        return blockIdentifier(false);
    }

    public static Predicate<String> auxBlockIdentifier() {
        return blockIdentifier(true);
    }

    private static Predicate<String> blockIdentifier(boolean onlyAuxBlocks) {
        return s -> {
            int delimiterInd = s.indexOf(':');
            if (delimiterInd != -1 && !Identifier.isNamespaceValid(s.substring(0, delimiterInd))
                    || !Identifier.isPathValid(s.substring(delimiterInd + 1))
            ) {
                return false;
            }
            Identifier id = Identifier.of(s);
            Block block = Registries.BLOCK.get(id);
            return !onlyAuxBlocks || !block.getDefaultState().isAir() && !MapartSchematicBuilder.needsAuxBlock(block);
        };
    }
}
