package ab.melodiesPort;

import ab.melodiesPort.client.animation.ItemAnimators;
import ab.melodiesPort.client.animation.animators.Animator;
import ab.melodiesPort.cobalt.registration.Registration;
import ab.melodiesPort.item.InstrumentItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public interface Items {
    List<Supplier<Item>> items = new LinkedList<>();
    List<Identifier> customInventoryModels = new LinkedList<>();

    Supplier<Item> BAGPIPE = register(Common.MOD_ID, "bagpipe", 200, new Vector3f(0.5f, 0.6f, 0.05f));
    Supplier<Item> DIDGERIDOO = register(Common.MOD_ID, "didgeridoo", 200, new Vector3f(0.0f, -0.45f, 1.0f));
    Supplier<Item> FLUTE = register(Common.MOD_ID, "flute", 100, new Vector3f(0.0f, 0.15f, 0.9f));
    Supplier<Item> LUTE = register(Common.MOD_ID, "lute", 200, new Vector3f(0.0f, 0.0f, 0.5f));
    Supplier<Item> PIANO = register(Common.MOD_ID, "piano", 300, new Vector3f(0.0f, 0.25f, 0.5f));
    Supplier<Item> TRIANGLE = register(Common.MOD_ID, "triangle", 300, new Vector3f(0.0f, 0.0f, 0.6f));
    Supplier<Item> TRUMPET = register(Common.MOD_ID, "trumpet", 100, new Vector3f(0.0f, 0.25f, 1.4f));
    Supplier<Item> TINY_DRUM = register(Common.MOD_ID, "tiny_drum", 300, new Vector3f(0.0f, 0.25f, 0.5f));

    /**
     * Open method to create custom items, for addons
     *
     * @param namespace Your addon's namespace
     * @param name      Your addon's item name
     * @param animator  Your item's animator. Allows to define how the entity
     *                  model should be animated when playing the instrument.
     * @param sustain   The instruments sustain, in ticks.
     * @param offset    Determines the offset from the player's location
     * @return The registered item's provider.
     */
    static @Nullable Supplier<Item> register(@NotNull String namespace, @NotNull String name, Animator animator,
                                             long sustain, Vector3f offset) {
        Identifier identifier = new Identifier(namespace, name);
        Supplier<Item> supplier = register(namespace, name, sustain, offset);
        ItemAnimators.register(identifier, animator);
        return supplier;
    }

    /**
     * Open method to create custom items, for addons.
     * If using this method, make sure to also register an {@link Animator} for your item.
     *
     * @param namespace Your addon's namespace
     * @param name      Your addon's item name
     * @param sustain   The instruments sustain, in ticks.
     * @param offset    Determines the offset from the player's location
     * @return The registered item's provider.
     */
    static @Nullable Supplier<Item> register(@NotNull String namespace, @NotNull String name,
                                             long sustain, Vector3f offset) {
        Identifier identifier = new Identifier(namespace, name);
        Sounds.Instrument instrument = new Sounds.Instrument(namespace, name);
        Supplier<Item> itemSupplier = () -> new InstrumentItem(baseProps(), instrument, sustain, offset);
        Supplier<Item> supplier = Registration.register(Registries.ITEM, identifier, itemSupplier);
        items.add(supplier);
        customInventoryModels.add(identifier);
        return supplier;
    }

    static void bootstrap() {
        // nop
    }

    static Item.Settings baseProps() {
        return new Item.Settings().maxCount(1);
    }

    static Collection<ItemStack> getSortedItems() {
        return items.stream().map(i -> i.get().getDefaultStack()).toList();
    }
}
