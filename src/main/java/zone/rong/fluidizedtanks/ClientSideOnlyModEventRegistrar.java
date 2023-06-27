package zone/rong/fluidzedtanks;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class ClientSideOnlyModEventRegistrar {
    private final IEventBus eventBus;
    private static TankBlock TANK;
    private static BlockEntityType<TankBlockEntity> ENTITY_TYPE;
    /**
     * @param eventBus an instance of the mod event bus
     */
    public ClientSideOnlyModEventRegistrar(IEventBus eventBus,TankBlock TANK,BlockEntityType<TankBlockEntity> ENTITY_TYPE) {
        this.eventBus = eventBus;
        this.TANK = TANK;
        this.ENTITY_TYPE = ENTITY_TYPE;
    }

    /**
     * Register client only events. This method must only be called when it is certain that the mod is
     * is executing code on the client side and not the dedicated server.
     */
    public void registerClientOnlyEvents() {
      ItemBlockRenderTypes.setRenderLayer(TANK, RenderType.cutout());
      BlockEntityRenderers.register(ENTITY_TYPE, ctx -> new TankBlockEntityRenderer());
      event.getBlockColors().register(TANK, TANK);
      event.getItemColors().register(TANK, TANK);
    }
}
