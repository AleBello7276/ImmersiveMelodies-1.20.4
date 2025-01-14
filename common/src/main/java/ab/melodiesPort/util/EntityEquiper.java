package ab.melodiesPort.util;

import ab.melodiesPort.Config;
import ab.melodiesPort.Items;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

public class EntityEquiper {
    public static void equip(Entity entity) {
        if (shouldEquip(entity)) {
            Item item = Items.items.get(entity.getWorld().getRandom().nextInt(Items.items.size())).get();
            entity.equipStack(EquipmentSlot.MAINHAND, new ItemStack(item));
        }
    }

    public static boolean shouldEquip(Entity entity) {
        String id = Registries.ENTITY_TYPE.getId(entity.getType()).toString();
        return Config.getInstance().mobInstrumentFactors.containsKey(id) && entity.getWorld().getRandom().nextFloat() < Config.getInstance().mobInstrumentFactors.get(id);
    }

    public static boolean canPickUp(Entity entity) {
        String id = Registries.ENTITY_TYPE.getId(entity.getType()).toString();
        return Config.getInstance().mobInstrumentFactors.containsKey(id) && Config.getInstance().mobInstrumentFactors.get(id) > 0;
    }
}
