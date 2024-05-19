package ab.melodiesPort;


import ab.melodiesPort.cobalt.network.NetworkHandler;
import ab.melodiesPort.network.c2s.ItemActionMessage;
import ab.melodiesPort.network.c2s.MelodyDeleteRequest;
import ab.melodiesPort.network.c2s.MelodyRequest;
import ab.melodiesPort.network.c2s.UploadMelodyRequest;
import ab.melodiesPort.network.s2c.MelodyListMessage;
import ab.melodiesPort.network.s2c.MelodyResponse;
import ab.melodiesPort.network.s2c.OpenGuiRequest;

public class Messages {
    public static void bootstrap() {
        // nop
    }

    static {
        NetworkHandler.registerMessage(MelodyRequest.class, MelodyRequest::new);
        NetworkHandler.registerMessage(MelodyListMessage.class, MelodyListMessage::new);
        NetworkHandler.registerMessage(MelodyResponse.class, MelodyResponse::new);
        NetworkHandler.registerMessage(UploadMelodyRequest.class, UploadMelodyRequest::new);
        NetworkHandler.registerMessage(OpenGuiRequest.class, OpenGuiRequest::new);
        NetworkHandler.registerMessage(ItemActionMessage.class, ItemActionMessage::new);
        NetworkHandler.registerMessage(MelodyDeleteRequest.class, MelodyDeleteRequest::new);
    }
}
