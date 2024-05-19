package ab.melodiesPort.network;

import ab.melodiesPort.client.gui.ImmersiveMelodiesScreen;
import ab.melodiesPort.network.s2c.MelodyListMessage;
import ab.melodiesPort.network.s2c.OpenGuiRequest;
import ab.melodiesPort.resources.ClientMelodyManager;
import net.minecraft.client.MinecraftClient;

public class ClientNetworkManager implements NetworkManager {
    @Override
    public void handleOpenGuiRequest(OpenGuiRequest request) {
        if (request.gui == OpenGuiRequest.Type.SELECTOR) {
            MinecraftClient.getInstance().setScreen(new ImmersiveMelodiesScreen());
        }
    }

    @Override
    public void handleMelodyListMessage(MelodyListMessage response) {
        ClientMelodyManager.getMelodiesList().clear();
        ClientMelodyManager.getMelodiesList().putAll(response.getMelodies());

        if (MinecraftClient.getInstance().currentScreen instanceof ImmersiveMelodiesScreen screen) {
            screen.refreshPage();
        }
    }
}
