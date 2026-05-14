package rh.maparthelper.util;

import com.google.common.base.Predicates;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class InventoryItemsCounter {
    private final Map<Item, Integer> counter = new HashMap<>();
    private final boolean countAll;

    public InventoryItemsCounter() {
        this.countAll = true;
    }

    public InventoryItemsCounter(@NotNull Collection<Item> countingItems) {
        countingItems.forEach(block -> counter.put(block, 0));
        this.countAll = false;
    }

    public void count(@NotNull Container inventory) {
        Predicate<ItemStack> doCount = countAll ? Predicates.alwaysTrue()
                : stack -> counter.containsKey(stack.getItem());
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack itemStack = inventory.getItem(slot);
            if (itemStack.isEmpty()) continue;
            if (doCount.test(itemStack)) {
                counter.merge(
                        itemStack.getItem(),
                        itemStack.getCount(),
                        Integer::sum
                );
            }
            if (itemStack.is(ItemTags.SHULKER_BOXES)) {
                ItemContainerContents container = itemStack.getOrDefault(
                        DataComponents.CONTAINER,
                        ItemContainerContents.EMPTY
                );
                container.nonEmptyStream()
                        .filter(doCount)
                        .forEach(stack ->
                                counter.merge(
                                        stack.getItem(),
                                        stack.getCount(),
                                        Integer::sum
                                )
                        );
            }
        }
    }

    public Map<Item, Integer> getCounts() {
        return new HashMap<>(counter);
    }
}