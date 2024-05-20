package ab.melodiesPort.mixin;

import ab.melodiesPort.item.InstrumentItem;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientWorld.class, priority = 900)
public class ClientWorldMixin {
    @Inject(method = "tickEntity", at = @At("HEAD"))
    public void immersiveMelodies$injectTickEntity(Entity entity, CallbackInfo ci) {
        entity.getHandItems().forEach(itemStack -> {
            if (itemStack.getItem() instanceof InstrumentItem item) {
                item.inventoryClientTick(itemStack, (ClientWorld) (Object) this, entity);
            }
        });
    }
}