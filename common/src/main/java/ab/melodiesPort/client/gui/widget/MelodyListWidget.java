package ab.melodiesPort.client.gui.widget;

import ab.melodiesPort.client.gui.ImmersiveMelodiesScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.Objects;

public class MelodyListWidget extends AlwaysSelectedEntryListWidget<MelodyListWidget.MelodyEntry> {
    private final ImmersiveMelodiesScreen currentScreen;
    private final boolean showSelection;
    int top;
    int bottom;

    int aLeft;
    int aRight;
    public MelodyListWidget(MinecraftClient client, ImmersiveMelodiesScreen currentScreen, int left, int width, int listY, int listH, boolean showSelection) {
        super(client, currentScreen.width, 162, listY, 10);


        this.currentScreen = currentScreen;

        this.aLeft = left;
        this.aRight = this.aLeft + width;

        this.showSelection = showSelection;

        top = (currentScreen.height - 230) / 2 + 22;
        bottom = (currentScreen.height - 230) / 2 + 184;


        setRenderBackground(false);
        //setRenderHorizontalShadows(false); //TO FIX
        setRenderHeader(false, 0);
    }





    @Override
    public void clearEntries() {
        super.clearEntries();
    }

    public void addEntry(Identifier identifier, Text name, Runnable onPress) {
        super.addEntry(new MelodyEntry(identifier, name, onPress));
    }

    @Override
    public void replaceEntries(Collection<MelodyEntry> newEntries) {
        super.replaceEntries(newEntries);
    }

    @Override
    protected int getScrollbarPositionX() {
        return super.getScrollbarPositionX() - 50;
    }

    @Override
    public int getRowWidth() {
        return 160;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= currentScreen.width / 2.0 - 120 && mouseX <= currentScreen.width / 2.0 + 120 && mouseY >= this.top && mouseY <= this.bottom;
    }

    @Override
    protected void enableScissor(DrawContext context) {
        context.enableScissor(currentScreen.width / 2 - 100, this.top, currentScreen.width / 2 + 70, this.bottom);
    }

    @Override
    protected void drawSelectionHighlight(DrawContext context, int y, int entryWidth, int entryHeight, int borderColor, int fillColor) {
        int x0 = currentScreen.width / 2 - 75;
        int x1 = currentScreen.width / 2 + 80;
        context.fill(x0 - 1, y - 1, x1, y + entryHeight + 3, 0x40000000);
    }

    public class MelodyEntry extends Entry<MelodyEntry> {
        final Identifier identifier;
        final Text name;
        final Runnable onPress;

        public MelodyEntry(Identifier identifier, Text melody, Runnable onPress) {
            this.identifier = identifier;
            this.name = melody;
            this.onPress = onPress;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawText(currentScreen.getTextRenderer(), name, aLeft + (onPress == null ? -2 : 2), y + 1, 0x404040, false);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0 && onPress != null) {
                onPress.run();
                return true;
            }
            return false;
        }

        @Override
        public Text getNarration() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MelodyEntry that)) return false;
            return Objects.equals(identifier, that.identifier);
        }

        @Override
        public int hashCode() {
            return Objects.hash(identifier);
        }
    }
}