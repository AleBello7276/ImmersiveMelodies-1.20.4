package ab.melodiesPort.client.gui;

import ab.melodiesPort.Config;
import ab.melodiesPort.client.gui.widget.MelodyListWidget;
import ab.melodiesPort.client.gui.widget.TexturedButtonWidget;
import ab.melodiesPort.cobalt.network.NetworkHandler;
import ab.melodiesPort.mixin.ScreenMixin;
import ab.melodiesPort.network.PacketSplitter;
import ab.melodiesPort.network.c2s.ItemActionMessage;
import ab.melodiesPort.network.c2s.MelodyDeleteRequest;
import ab.melodiesPort.resources.ClientMelodyManager;
import ab.melodiesPort.resources.Melody;
import ab.melodiesPort.resources.MelodyDescriptor;
import ab.melodiesPort.util.MidiConverter;
import ab.melodiesPort.util.MidiParser;
import ab.melodiesPort.util.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;


import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ImmersiveMelodiesScreen extends Screen {
    public static final Identifier BACKGROUND_TEXTURE = new Identifier("immersive_melodies:textures/gui/paper.png");
    private MelodyListWidget list;
    private TextFieldWidget search;

    private Text error;
    private long lastError;

    public static boolean shouldRenderGUI = false;
    public static  int _y;
    public static  int _x;

    private void setError(Text error) {
        this.error = error;
        this.lastError = System.currentTimeMillis();
        this.search.setText("");
        this.search.setSuggestion(null);
    }

    @Nullable
    private Identifier selected = null;

    public ImmersiveMelodiesScreen() {
        super(Text.translatable("itemGroup.immersive_melodies.immersive_melodies_tab"));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    protected void init() {
        this.search = new TextFieldWidget(this.textRenderer, this.width / 2 - 70, this.height / 2 - 103, 140, 20, Text.translatable("immersive_melodies.search"));
        this.search.setMaxLength(128);
        this.search.setChangedListener(a -> {
            this.refreshPage();
            this.search.setSuggestion(null);
            this.list.setScrollAmount(0);
        });
        this.search.setDrawsBackground(false);
        this.search.setEditableColor(0x808080);
        this.search.setSuggestion("Search");
        setInitialFocus(this.search);

        list = new MelodyListWidget(this.client, this);

        refreshPage();
    }

    private void openHelp() {
        try {
            Util.getOperatingSystem().open(URI.create("https://github.com/Luke100000/ImmersiveMelodies/wiki/Custom-Melodies"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void filesDragged(List<Path> paths) {
        PathMatcher midiMatcher = FileSystems.getDefault().getPathMatcher("glob:*{.mid,.midi,.MID,.MIDI}");
        PathMatcher abcMatcher = FileSystems.getDefault().getPathMatcher("glob:*{.abc,.ABC}");
        for (Path path : paths) {
            try {
                String rawName = path.getFileName().toString();
                String name = rawName.substring(0, rawName.lastIndexOf('.'));
                if (midiMatcher.matches(path.getFileName())) {
                    // This is a midi file, parse it
                    InputStream inputStream = new FileInputStream(path.toFile());
                    parseMidi(name, inputStream);
                } else if (abcMatcher.matches(path.getFileName())) {
                    // This is an abc file, convert it to midi and then parse it
                    byte[] bytes = Files.readAllBytes(path);
                    MinecraftClient.getInstance().execute(() -> {
                        try {
                            MidiConverter.Response request = MidiConverter.request(bytes);
                            ByteArrayInputStream inputStream = new ByteArrayInputStream(request.getBody());
                            parseMidi(name, inputStream);
                        } catch (Exception e) {
                            e.printStackTrace();
                            setError(Text.translatable("immersive_melodies.error.empty"));
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void parseMidi(String name, InputStream inputStream) {
        if (name.isEmpty()) {
            name = "empty";
        }

        Melody melody = MidiParser.parseMidi(inputStream, name);
        if (!melody.getTracks().isEmpty()) {
            PacketSplitter.sendToServer(name, melody);
            search.setText(name);
            list.setScrollAmount(0);
        } else {
            setError(Text.translatable("immersive_melodies.error.empty"));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        int x = (this.width - 192) / 2;
        int y = (this.height - 230) / 2;
        _x = x;
        _y = y;


        //context.drawTexture(BACKGROUND_TEXTURE, x, y, 0, 0, 192, 215);
        // Print help for noobs
        if (!Config.getInstance().clickedHelp) {
            context.drawTooltip(textRenderer, Text.translatable("immersive_melodies.read"), width / 2 + 55, height / 2 + 69 + 17);
        }

        // Print error
        if (error != null && System.currentTimeMillis() - lastError < 5000) {
            context.drawCenteredTextWithShadow(textRenderer, error, width / 2, this.height / 2 - 103, 0xFF0000);
        }
        shouldRenderGUI = true;
        super.render(context, mouseX, mouseY, delta);
        shouldRenderGUI = false;
        //context.drawTexture(BACKGROUND_TEXTURE, x, y, 0, 0, 192, 215);
    }



    public void refreshPage() {
        clearChildren();

        addDrawableChild(search);
        addDrawableChild(list);

        list.clearEntries();


        int x = (this.width - 192) / 2;
        int y2 = (this.height - 230) / 2;


        //addDrawableChild(new BackGroundTexture(x, y2, BACKGROUND_TEXTURE));

        String lastPath = "";
        for (Map.Entry<Identifier, MelodyDescriptor> entry : ClientMelodyManager.getMelodiesList().entrySet().stream()
                .filter(e -> this.search.getText().isEmpty() || e.getValue().getName().toLowerCase(Locale.ROOT).contains(this.search.getText().toLowerCase(Locale.ROOT)))
                .sorted((a, b) -> {
                    int primarySortA = getSortIndex(a);
                    int primarySortB = getSortIndex(b);
                    if (primarySortA != primarySortB) {
                        return primarySortB - primarySortA;
                    } else {
                        return a.getValue().getName().compareTo(b.getValue().getName());
                    }
                })
                .toList()) {

            String dir = Utils.removeLastPart(entry.getKey().getPath(), "/");
            String path = entry.getKey().getNamespace() + "/" + dir;

            if (!path.equals(lastPath)) {
                list.addEntry(new Identifier(path), Text.literal(dir).formatted(Formatting.ITALIC).formatted(Formatting.GRAY), null);
                lastPath = path;
            }

            list.addEntry(entry.getKey(), Text.literal(entry.getValue().getName()), () -> {
                NetworkHandler.sendToServer(new ItemActionMessage(ItemActionMessage.State.PLAY, entry.getKey()));
                selected = entry.getKey();
                refreshPage();
            });
        }

        int y = this.height / 2 + 69;



        // Close
        addDrawableChild(new TexturedButtonWidget(width / 2 - 75, y, 16, 16, BACKGROUND_TEXTURE, 256 - 16, 0, 256, 256, Text.of((String) null), button -> {
            close();
        }, () -> List.of(Text.translatable("immersive_melodies.close").asOrderedText())));

        // Delete
        if (selected != null && (Utils.canDelete(selected, MinecraftClient.getInstance().player))) {
            addDrawableChild(new TexturedButtonWidget(width / 2 + 30, y, 16, 16, BACKGROUND_TEXTURE, 256 - 16, 16, 256, 256, Text.of((String) null), button -> {
                NetworkHandler.sendToServer(new MelodyDeleteRequest(selected));
                selected = null;
            }, () -> List.of(Text.translatable("immersive_melodies.delete").asOrderedText())));
        }

        // Pause
        addDrawableChild(new TexturedButtonWidget(width / 2 - 10 - 8, y, 16, 16, BACKGROUND_TEXTURE, 256 - 32, 32, 256, 256, Text.of((String) null), button -> {
            NetworkHandler.sendToServer(new ItemActionMessage(ItemActionMessage.State.PAUSE));
        }, () -> List.of(Text.translatable("immersive_melodies.pause").asOrderedText())));

        // Play
        addDrawableChild(new TexturedButtonWidget(width / 2, y, 16, 16, BACKGROUND_TEXTURE, 256 - 16, 32, 256, 256, Text.of((String) null), button -> {
            NetworkHandler.sendToServer(new ItemActionMessage(ItemActionMessage.State.CONTINUE));
        }, () -> List.of(Text.translatable("immersive_melodies.play").asOrderedText())));

        // Help
        addDrawableChild(new TexturedButtonWidget(width / 2 + 50, y, 16, 16, BACKGROUND_TEXTURE, 256 - 48, 32, 256, 256, Text.of((String) null), button -> {
            openHelp();
            if (!Config.getInstance().clickedHelp) {
                Config.getInstance().clickedHelp = true;
                Config.getInstance().save();
            }
        }, () -> List.of(Text.translatable("immersive_melodies.help").asOrderedText())));
    }

    private static int getSortIndex(Map.Entry<Identifier, MelodyDescriptor> entry) {
        return Utils.ownsMelody(entry.getKey(), MinecraftClient.getInstance().player) ? 2 : Utils.isPlayerMelody(entry.getKey()) ? 0 : 1;
    }

    public TextRenderer getTextRenderer() {
        return this.textRenderer;
    }
}


