package ab.melodiesPort.cobalt.network;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.Nullable;

//import javax.annotation.Nullable;

public abstract class Message {
    protected Message() {

    }

    public abstract void encode(PacketByteBuf b);

    public abstract void receive(@Nullable PlayerEntity e);
}
