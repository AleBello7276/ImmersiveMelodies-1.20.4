package ab.melodiesPort.network.c2s;

import ab.melodiesPort.cobalt.network.NetworkHandler;
import ab.melodiesPort.network.FragmentedMessage;
import ab.melodiesPort.network.PacketSplitter;
import ab.melodiesPort.network.s2c.MelodyListMessage;
import ab.melodiesPort.resources.Melody;
import ab.melodiesPort.resources.ServerMelodyManager;
import ab.melodiesPort.util.Utils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class UploadMelodyRequest extends FragmentedMessage {
    public UploadMelodyRequest(String name, byte[] fragment, int length) {
        super(name, fragment, length);
    }

    public UploadMelodyRequest(PacketByteBuf b) {
        super(b);
    }

    @Override
    protected void finish(PlayerEntity e, String name, Melody melody) {
        String id = Utils.getPlayerName(e) + "/" + Utils.escapeString(name);
        Identifier identifier = new Identifier("player", id);

        // Register
        ServerMelodyManager.registerMelody(
                identifier,
                melody
        );

        // Update the index
        NetworkHandler.sendToPlayer(new MelodyListMessage(e), (ServerPlayerEntity) e);

        // Send the melody to all players
        e.getWorld().getPlayers().forEach(player -> {
            PacketSplitter.sendToPlayer(identifier, melody, (ServerPlayerEntity) player);
        });
    }
}
