package ab.melodiesPort.network.s2c;

import ab.melodiesPort.network.FragmentedMessage;
import ab.melodiesPort.resources.ClientMelodyManager;
import ab.melodiesPort.resources.Melody;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class MelodyResponse extends FragmentedMessage {
    public MelodyResponse(PacketByteBuf b) {
        super(b);
    }

    public MelodyResponse(Identifier identifier, byte[] fragment, int length) {
        super(identifier.toString(), fragment, length);
    }

    @Override
    protected void finish(PlayerEntity e, String name, Melody melody) {
        ClientMelodyManager.setMelody(new Identifier(name), melody);
    }
}
