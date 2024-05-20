package ab.melodiesPort.client.animation;

import ab.melodiesPort.client.MelodyProgress;
import ab.melodiesPort.client.MelodyProgressManager;
import ab.melodiesPort.item.InstrumentItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

public class BipedEntityModelAnimator {
    public static <T extends LivingEntity> Item getInstrument(T entity) {
        for (ItemStack handItem : entity.getHandItems()) {
            if (handItem.getItem() instanceof InstrumentItem) {
                return handItem.getItem();
            }
        }
        return null;
    }

    private static <T extends LivingEntity> boolean isInMainHand(T entity) {
        return entity.getMainHandStack().getItem() instanceof InstrumentItem;
    }

    public static <T extends LivingEntity> ModelPart getLeftArm(BipedEntityModel<T> model, T entity) {
        return isInMainHand(entity) ? model.leftArm : model.rightArm;
    }

    public static <T extends LivingEntity> ModelPart getRightArm(BipedEntityModel<T> model, T entity) {
        return isInMainHand(entity) ? model.rightArm : model.leftArm;
    }

    public static <T extends LivingEntity> void setAngles(BipedEntityModel<T> model, T entity) {
        Item item = getInstrument(entity);
        if (item != null) {
            ModelPart left = getLeftArm(model, entity);
            ModelPart right = getRightArm(model, entity);

            float time = (MinecraftClient.getInstance().isPaused() ? 0.0f : MinecraftClient.getInstance().getTickDelta()) + entity.age;

            MelodyProgress progress = MelodyProgressManager.INSTANCE.getProgress(entity);
            progress.visualTick(time);

            ItemAnimators.get(Registries.ITEM.getId(item)).setAngles(left, right, model, entity, progress, time);

            if (!isInMainHand(entity)) {
                left.roll = -left.roll;
                right.roll = -right.roll;
                left.yaw = -left.yaw;
                right.yaw = -right.yaw;
            }
        }
    }
}