package rh.maparthelper.gui.input;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import rh.maparthelper.conversion.schematic.MapartSchematicBuilder;

import java.util.function.Predicate;

/// Predicates for checking whether the input is valid
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
            if (delimiterInd != -1 && !Identifier.isValidNamespace(s.substring(0, delimiterInd))
                    || !Identifier.isValidPath(s.substring(delimiterInd + 1))
            ) {
                return false;
            }
            Identifier id = Identifier.parse(s);
            Block block = BuiltInRegistries.BLOCK.getValue(id);
            return !onlyAuxBlocks || !block.defaultBlockState().isAir() && !MapartSchematicBuilder.needsAuxBlock(block);
        };
    }
}
