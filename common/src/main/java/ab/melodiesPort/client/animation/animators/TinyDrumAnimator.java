package ab.melodiesPort.client.animation.animators;

import ab.melodiesPort.client.MelodyProgress;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;

public class TinyDrumAnimator implements Animator {
    @Override
    public void setAngles(ModelPart left, ModelPart right, BipedEntityModel<?> model, LivingEntity entity, MelodyProgress progress, float time) {
        float hit = Math.min(progress.delta(), 300) / 300.0f * progress.getCurrentVolume() * 0.4f;
        left.pitch = -1.2f - hit;
        left.yaw =  0.2f - hit;
        left.roll = (progress.getCurrentPitch() - 0.5f) * 0.5f;

        right.pitch = -0.6f;
        right.yaw = -0.8f + progress.getCurrentPitch() * 0.15f;
        right.roll = 0.2f + progress.getCurrentPitch() * 0.15f;
    }
}