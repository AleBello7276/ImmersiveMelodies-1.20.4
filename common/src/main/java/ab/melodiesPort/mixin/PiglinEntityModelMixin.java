package ab.melodiesPort.mixin;

import ab.melodiesPort.client.animation.BipedEntityModelAnimator;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.PiglinEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PiglinEntityModel.class)
public abstract class PiglinEntityModelMixin<T extends MobEntity>extends PlayerEntityModel<T> {
    public PiglinEntityModelMixin(ModelPart root, boolean thinArms) {
        super(root, thinArms);
    }

    @Inject(method = "setAngles(Lnet/minecraft/entity/mob/MobEntity;FFFFF)V", at = @At("HEAD"), cancellable = true)
    public void immersiveMelodies$injectSetAngles(T entity, float f, float g, float h, float i, float j, CallbackInfo ci) {
        if (BipedEntityModelAnimator.getInstrument(entity) != null) {
            super.setAngles(entity, f, g, h, i, j);
            ci.cancel();
        }
    }
}