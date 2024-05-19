package ab.melodiesPort.network.c2s;

import ab.melodiesPort.cobalt.network.Message;
import ab.melodiesPort.network.PacketSplitter;
import ab.melodiesPort.resources.Melody;
import ab.melodiesPort.resources.ServerMelodyManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class MelodyRequest extends Message {
    private final Identifier identifier;

    public MelodyRequest(Identifier identifier) {
        this.identifier = identifier;
    }

    public MelodyRequest(PacketByteBuf b) {
        this.identifier = b.readIdentifier();
    }

    @Override
    public void encode(PacketByteBuf b) {
        b.writeIdentifier(identifier);
    }

    @Override
    public void receive(PlayerEntity e) {
        Melody melody = ServerMelodyManager.getMelody(identifier);
        PacketSplitter.sendToPlayer(identifier, melody, (ServerPlayerEntity) e);
    }
}
