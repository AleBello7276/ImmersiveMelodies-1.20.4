package ab.melodiesPort.mixin;

import ab.melodiesPort.item.InstrumentItem;
import ab.melodiesPort.util.EntityEquiper;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ServerWorld.class, priority = 900)
public class ServerWorldMixin {
    @Inject(method = "addEntity(Lnet/minecraft/entity/Entity;)Z",
            at = @At("HEAD")
    )
    private void onAddEntity(Entity entity, CallbackInfoReturnable<Boolean> info) {
        EntityEquiper.equip(entity);
    }

    @Inject(method = "tickEntity", at = @At("HEAD"))
    public void immersiveMelodies$injectTickEntity(Entity entity, CallbackInfo ci) {
        entity.getHandItems().forEach(itemStack -> {
            if (itemStack.getItem() instanceof InstrumentItem item) {
                item.inventoryServerTick(itemStack, (ServerWorld) (Object) this, entity);
            }
        });
    }
}
