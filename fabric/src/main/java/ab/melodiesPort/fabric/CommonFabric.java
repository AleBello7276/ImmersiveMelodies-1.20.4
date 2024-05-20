package ab.melodiesPort.fabric;

import ab.melodiesPort.*;
import ab.melodiesPort.fabric.cobalt.network.NetworkHandlerImpl;
import ab.melodiesPort.fabric.cobalt.registration.RegistrationImpl;
import ab.melodiesPort.fabric.resources.FabricMelody;
import ab.melodiesPort.resources.ServerMelodyManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.ResourceType;

public final class CommonFabric implements ModInitializer {
    static {
        new NetworkHandlerImpl();
        new RegistrationImpl();
    }

    @Override
    public void onInitialize() {
        Items.bootstrap();
        Messages.bootstrap();
        Sounds.bootstrap();

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new FabricMelody());

        ServerLifecycleEvents.SERVER_STARTING.register(server -> ServerMelodyManager.server = server);

        ItemGroup group = FabricItemGroup.builder()
                .displayName(ItemGroups.getDisplayName())
                .icon(ItemGroups::getIcon)
                .entries((enabledFeatures, entries) -> entries.addAll(Items.getSortedItems()))
                .build();

        Registry.register(Registries.ITEM_GROUP, Common.locate("group"), group);
    }
}
