package ab.melodiesPort.fabric.resources;

import ab.melodiesPort.Common;
import ab.melodiesPort.resources.MelodyLoader;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.util.Identifier;

public class FabricMelody extends MelodyLoader implements IdentifiableResourceReloadListener {
    private static final Identifier ID = Common.locate("melodies");

    @Override
    public Identifier getFabricId() {
        return ID;
    }
}
