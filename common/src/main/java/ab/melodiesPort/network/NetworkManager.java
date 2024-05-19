package ab.melodiesPort.network;

import ab.melodiesPort.network.s2c.MelodyListMessage;
import ab.melodiesPort.network.s2c.OpenGuiRequest;

public interface NetworkManager {
    void handleOpenGuiRequest(OpenGuiRequest request);

    void handleMelodyListMessage(MelodyListMessage response);
}
