package ab.melodiesPort.network;

import ab.melodiesPort.cobalt.network.Message;
import ab.melodiesPort.resources.Melody;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class FragmentedMessage extends Message {
    private final String name;
    private final byte[] fragment;
    private final int length;

    private static final Map<String, Queue<byte[]>> buffer = new ConcurrentHashMap<>();

    public FragmentedMessage(String name, byte[] fragment, int length) {
        this.name = name;
        this.fragment = fragment;
        this.length = length;
    }

    public FragmentedMessage(PacketByteBuf b) {
        this.name = b.readString();
        this.fragment = b.readByteArray();
        this.length = b.readInt();
    }

    @Override
    public void encode(PacketByteBuf b) {
        b.writeString(name);
        b.writeByteArray(fragment);
        b.writeInt(length);
    }

    @Override
    public void receive(PlayerEntity e) {
        String identifier = (e == null ? "local" : e.getUuidAsString()) + ":" + name;
        Queue<byte[]> byteBuffer = buffer.computeIfAbsent(identifier, k -> new ConcurrentLinkedQueue<>());
        byteBuffer.add(fragment);

        if (byteBuffer.stream().mapToInt(f -> f.length).sum() >= length) {
            // Assemble
            PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
            for (byte[] b : byteBuffer) {
                buffer.writeBytes(b);
            }

            finish(e, name, new Melody(buffer));

            FragmentedMessage.buffer.remove(identifier);
        }
    }

    protected abstract void finish(PlayerEntity e, String name, Melody melody);
}