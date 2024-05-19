package ab.melodiesPort.mixin;

import ab.melodiesPort.client.gui.ImmersiveMelodiesScreen;
import com.google.common.collect.Lists;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.List;

@Mixin(Screen.class)
public class ScreenMixin {



    /**
     * @author
     */
    @Inject(method = "Lnet/minecraft/client/gui/screen/Screen;render(Lnet/minecraft/client/gui/DrawContext;IIF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;renderBackground(Lnet/minecraft/client/gui/DrawContext;IIF)V",
                    shift = At.Shift.AFTER))
    public void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {


        if(ImmersiveMelodiesScreen.shouldRenderGUI){
            context.drawTexture(ImmersiveMelodiesScreen.BACKGROUND_TEXTURE, ImmersiveMelodiesScreen._x, ImmersiveMelodiesScreen._y, 0, 0, 192, 215);
        }


    }




}
