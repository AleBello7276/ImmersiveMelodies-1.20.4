package ab.melodiesPort.network.c2s;

import ab.melodiesPort.cobalt.network.Message;
import ab.melodiesPort.item.InstrumentItem;
import ab.melodiesPort.resources.ServerMelodyManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class TrackToggleMessage extends Message {
    private final Identifier melody;
    private final int track;
    private final boolean enabled;

    public TrackToggleMessage(Identifier melody, int track, boolean enabled) {
        this.melody = melody;
        this.track = track;
        this.enabled = enabled;
    }

    public TrackToggleMessage(PacketByteBuf b) {
        melody = b.readIdentifier();
        track = b.readInt();
        enabled = b.readBoolean();
    }

    @Override
    public void encode(PacketByteBuf b) {
        b.writeIdentifier(melody);
        b.writeInt(track);
        b.writeBoolean(enabled);
    }

    @Override
    public void receive(PlayerEntity e) {
        e.getHandItems().forEach(stack -> {
            if (stack.getItem() instanceof InstrumentItem item) {
                ServerMelodyManager.MelodyTrackSettings settings = ServerMelodyManager.getSettings();
                String identifier = ServerMelodyManager.getIdentifier(e, item);
                if (enabled) {
                    settings.enableTrack(melody, identifier, track);
                } else {
                    settings.disableTrack(melody, identifier, track);
                }

                item.refreshTracks(stack, e);
            }
        });
    }
}