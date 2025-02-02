package ab.melodiesPort.network.s2c;

import ab.melodiesPort.Common;
import ab.melodiesPort.cobalt.network.Message;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;

public class OpenGuiRequest extends Message {
    public final Type gui;

    public OpenGuiRequest(Type gui) {
        this.gui = gui;
    }

    public OpenGuiRequest(PacketByteBuf b) {
        this.gui = b.readEnumConstant(Type.class);
    }

    @Override
    public void encode(PacketByteBuf b) {
        b.writeEnumConstant(gui);
    }

    @Override
    public void receive(PlayerEntity e) {
        Common.networkManager.handleOpenGuiRequest(this);
    }

    public enum Type {
        SELECTOR,
    }
}
