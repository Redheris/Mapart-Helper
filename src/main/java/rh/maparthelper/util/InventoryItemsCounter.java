package rh.maparthelper.util;

import com.google.common.base.Predicates;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
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

    public void count(@NotNull Inventory inventory) {
        Predicate<ItemStack> doCount = countAll ? Predicates.alwaysTrue()
                : stack -> counter.containsKey(stack.getItem());
        for (ItemStack itemStack : inventory) {
            if (itemStack.isEmpty()) continue;
            if (doCount.test(itemStack)) {
                counter.merge(
                        itemStack.getItem(),
                        itemStack.getCount(),
                        Integer::sum
                );
            }
            if (itemStack.isIn(ItemTags.SHULKER_BOXES)) {
                ContainerComponent container = itemStack.getOrDefault(
                        DataComponentTypes.CONTAINER,
                        ContainerComponent.DEFAULT
                );
                container.streamNonEmpty()
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