package ab.melodiesPort;

import ab.melodiesPort.client.sound.SoundManagerImpl;
import ab.melodiesPort.network.ClientNetworkManager;
import net.minecraft.client.MinecraftClient;

public class Client {
    public static void postLoad() {
        MinecraftClient client = MinecraftClient.getInstance();
        Common.networkManager = new ClientNetworkManager();
        Common.soundManager = new SoundManagerImpl(client);
    }
}
