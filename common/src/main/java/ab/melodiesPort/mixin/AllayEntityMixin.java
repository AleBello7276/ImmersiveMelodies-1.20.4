package ab.melodiesPort.mixin;

import ab.melodiesPort.client.MelodyProgressManager;
import net.minecraft.entity.passive.AllayEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AllayEntity.class)
public class AllayEntityMixin {
    @Inject(method = "isDancing()Z", at = @At("HEAD"), cancellable = true)
    void immersiveMelodiesIsDancing(CallbackInfoReturnable<Boolean> cir) {
        if (MelodyProgressManager.INSTANCE.isClose(((AllayEntity) (Object) this).getPos(), 5.0f)) {
            cir.setReturnValue(true);
        }
    }
}