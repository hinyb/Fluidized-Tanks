package zone.rong.fluidizedtanks.mixin;

import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import zone.rong.fluidizedtanks.data.TankDefinitionManager;

import java.util.ArrayList;
import java.util.List;

@Mixin(ReloadableServerResources.class)
public class ReloadableServerResourcesMixin {

    @Inject(method = "listeners", at = @At("RETURN"), cancellable = true)
    private void injectHigherPriorityListener(CallbackInfoReturnable<List<PreparableReloadListener>> cir) {
        TankDefinitionManager.instance = new TankDefinitionManager();
        List<PreparableReloadListener> listeners = new ArrayList<>();
        listeners.add(TankDefinitionManager.instance);
        listeners.addAll(cir.getReturnValue());
        cir.setReturnValue(listeners);
    }

}
