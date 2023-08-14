package immersive_melodies.client.gui;

import immersive_melodies.client.gui.widget.MelodyListWidget;
import immersive_melodies.client.gui.widget.TexturedButtonWidget;
import immersive_melodies.cobalt.network.NetworkHandler;
import immersive_melodies.network.c2s.ItemActionMessage;
import immersive_melodies.resources.ClientMelodyManager;
import immersive_melodies.resources.MelodyDescriptor;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ImmersiveMelodiesScreen extends Screen {
    public static final Identifier BACKGROUND_TEXTURE = new Identifier("immersive_melodies:textures/gui/paper.png");
    private MelodyListWidget list;
    private TextFieldWidget search;

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
        this.search = addDrawableChild(new TextFieldWidget(this.textRenderer, this.width / 2 - 70, this.height / 2 - 103, 140, 20, Text.translatable("immersive_melodies.search")));
        this.search.setMaxLength(128);
        this.search.setChangedListener(a -> {
            this.refreshPage();
            this.search.setSuggestion(null);
        });
        this.search.setDrawsBackground(false);
        this.search.setEditableColor(0x808080);
        this.search.setSuggestion("Search");

        list = addDrawableChild(new MelodyListWidget(this.client, this));

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
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);

        int x = (this.width - 192) / 2;
        int y = (this.height - 230) / 2;
        context.drawTexture(BACKGROUND_TEXTURE, x, y, 0, 0, 192, 215);

        super.render(context, mouseX, mouseY, delta);
    }

    public void refreshPage() {
        list.clearEntries();
        for (Map.Entry<Identifier, MelodyDescriptor> entry : ClientMelodyManager.getMelodiesList().entrySet().stream()
                .filter(e -> this.search.getText().isEmpty() || e.getValue().getName().contains(this.search.getText()))
                .sorted(Comparator.comparing(a -> a.getKey().getPath()))
                .toList()) {
            list.addEntry(entry.getKey(), entry.getValue(), () -> {
                selected = entry.getKey();
                refreshPage();
            });
        }

        // Close
        addDrawableChild(new TexturedButtonWidget(width / 2 - 80, 200, 16, 16, BACKGROUND_TEXTURE, 256 - 16, 0, 256, 256, Text.of(null), button -> {
            close();
        }, () -> List.of(Text.translatable("immersive_melodies.close").asOrderedText())));

        if (selected != null) {
            // Delete
            addDrawableChild(new TexturedButtonWidget(width / 2 + 30, 200, 16, 16, BACKGROUND_TEXTURE, 256 - 16, 16, 256, 256, Text.of(null), button -> {

            }, () -> List.of(Text.translatable("immersive_melodies.delete").asOrderedText())));

            // Pause
            addDrawableChild(new TexturedButtonWidget(width / 2 - 20 - 8, 200, 16, 16, BACKGROUND_TEXTURE, 256 - 32, 32, 256, 256, Text.of(null), button -> {
                NetworkHandler.sendToServer(new ItemActionMessage(ItemActionMessage.State.PAUSE, selected));
            }, () -> List.of(Text.translatable("immersive_melodies.pause").asOrderedText())));

            // Play
            addDrawableChild(new TexturedButtonWidget(width / 2 - 8, 200, 16, 16, BACKGROUND_TEXTURE, 256 - 16, 32, 256, 256, Text.of(null), button -> {
                NetworkHandler.sendToServer(new ItemActionMessage(ItemActionMessage.State.PLAY, selected));
            }, () -> List.of(Text.translatable("immersive_melodies.play").asOrderedText())));
        }

        // Help
        addDrawableChild(new TexturedButtonWidget(width / 2 + 50, 200, 16, 16, BACKGROUND_TEXTURE, 256 - 48, 32, 256, 256, Text.of(null), button -> {
            openHelp();
        }, () -> List.of(Text.translatable("immersive_melodies.help").asOrderedText())));
    }

    public TextRenderer getTextRenderer() {
        return this.textRenderer;
    }
}
