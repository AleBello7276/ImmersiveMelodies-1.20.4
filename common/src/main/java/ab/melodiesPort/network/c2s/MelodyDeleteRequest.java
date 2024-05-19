package ab.melodiesPort.network.c2s;

import ab.melodiesPort.cobalt.network.Message;
import ab.melodiesPort.cobalt.network.NetworkHandler;
import ab.melodiesPort.network.s2c.MelodyListMessage;
import ab.melodiesPort.resources.ServerMelodyManager;
import ab.melodiesPort.util.Utils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class MelodyDeleteRequest extends Message {
    private final Identifier identifier;

    public MelodyDeleteRequest(Identifier identifier) {
        this.identifier = identifier;
    }

    public MelodyDeleteRequest(PacketByteBuf b) {
        this.identifier = b.readIdentifier();
    }

    @Override
    public void encode(PacketByteBuf b) {
        b.writeIdentifier(identifier);
    }

    @Override
    public void receive(PlayerEntity e) {
        if (Utils.canDelete(identifier, e)) {
            ServerMelodyManager.deleteMelody(identifier);

            NetworkHandler.sendToPlayer(new MelodyListMessage(e), (ServerPlayerEntity) e);
        }
    }
}
