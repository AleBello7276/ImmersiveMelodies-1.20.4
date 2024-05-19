package ab.melodiesPort.network.s2c;

import ab.melodiesPort.Common;
import ab.melodiesPort.Config;
import ab.melodiesPort.cobalt.network.Message;
import ab.melodiesPort.resources.MelodyDescriptor;
import ab.melodiesPort.resources.MelodyLoader;
import ab.melodiesPort.resources.ServerMelodyManager;
import ab.melodiesPort.util.Utils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class MelodyListMessage extends Message {
    private final Map<Identifier, MelodyDescriptor> melodies = new HashMap<>();

    public MelodyListMessage(PlayerEntity receiver) {
        //datapack melodies
        for (Map.Entry<Identifier, MelodyLoader.LazyMelody> lazyMelodyEntry : ServerMelodyManager.getDatapackMelodies().entrySet()) {
            melodies.put(lazyMelodyEntry.getKey(), lazyMelodyEntry.getValue().getDescriptor());
        }

        //custom melodies
        if (Config.getInstance().showOtherPlayersMelodies) {
            this.melodies.putAll(ServerMelodyManager.getIndex().getMelodies());
        } else {
            ServerMelodyManager.getIndex().getMelodies().forEach((id, desc) -> {
                if (Utils.ownsMelody(id, receiver)) {
                    this.melodies.put(id, desc);
                }
            });
        }
    }

    @Override
    public void encode(PacketByteBuf b) {
        b.writeInt(melodies.size());
        for (Map.Entry<Identifier, MelodyDescriptor> entry : melodies.entrySet()) {
            b.writeIdentifier(entry.getKey());
            entry.getValue().encodeLite(b);
        }
    }

    public MelodyListMessage(PacketByteBuf b) {
        int size = b.readInt();
        for (int i = 0; i < size; i++) {
            melodies.put(b.readIdentifier(), new MelodyDescriptor(b));
        }
    }

    @Override
    public void receive(PlayerEntity e) {
        Common.networkManager.handleMelodyListMessage(this);
    }

    public Map<Identifier, MelodyDescriptor> getMelodies() {
        return melodies;
    }
}
