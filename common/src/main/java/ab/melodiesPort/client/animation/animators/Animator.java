package ab.melodiesPort.client.animation.animators;

import ab.melodiesPort.client.MelodyProgress;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;

public interface Animator {
    void setAngles(ModelPart left, ModelPart right, BipedEntityModel<?> model, LivingEntity entity, MelodyProgress progress, float time);
}
