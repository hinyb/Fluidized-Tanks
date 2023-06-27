package zone.rong.fluidizedtanks;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import zone.rong.fluidizedtanks.block.TankBlock;
import zone.rong.fluidizedtanks.block.TankBlockEntity;
import zone.rong.fluidizedtanks.block.item.TankBlockItem;
import zone.rong.fluidizedtanks.client.TankBlockEntityRenderer;
import zone.rong.fluidizedtanks.data.S2CUpdateTankDefinitionsPacket;
import zone.rong.fluidizedtanks.data.TankDefinitionManager;

@Mod("fluidizedtanks")
public class FluidizedTanks {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel NETWORK_CHANNEL =
            NetworkRegistry.newSimpleChannel(new ResourceLocation("fluidizedtanks", "main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    public static TankBlock TANK;
    public static BlockEntityType<TankBlockEntity> ENTITY_TYPE;

    public FluidizedTanks() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addGenericListener(Block.class, this::registerBlock);
        bus.addGenericListener(BlockEntityType.class, this::registerBlockEntity);
        bus.addGenericListener(Item.class, this::registerItem);

        DistExecutor.safeRunWhenOn(Dist.CLIENT, registerClientOnlyEvents());

        // MinecraftForge.EVENT_BUS.addListener(TankDefinitionManager::listenAddReload);
        MinecraftForge.EVENT_BUS.addListener(TankDefinitionManager::listenOnDatapackSync);
        MinecraftForge.EVENT_BUS.addListener(TankDefinitionManager::listenOnPlayerLoggedIn);

        int networkId = 0;
        S2CUpdateTankDefinitionsPacket.register(NETWORK_CHANNEL, networkId++);
    }

    private void registerBlock(final RegistryEvent.Register<Block> event) {
        TANK = new TankBlock();
        TANK.setRegistryName("fluidizedtanks", "tank");
        event.getRegistry().register(TANK);
    }

    private void registerBlockEntity(final RegistryEvent.Register<BlockEntityType<?>> event) {
        ENTITY_TYPE = BlockEntityType.Builder.of(TankBlockEntity::new, TANK).build(null);
        ENTITY_TYPE.setRegistryName("fluidizedtanks", "tank");
        event.getRegistry().register(ENTITY_TYPE);
    }

    private void registerItem(final RegistryEvent.Register<Item> event) {
        TankBlockItem tankItem = new TankBlockItem(TANK);
        tankItem.setRegistryName(TANK.getRegistryName());
        event.getRegistry().register(tankItem);
    }

    private void registerClientOnlyEvents(){
        bus.addListener(this::setupClient);
        bus.addListener(this::registerBlockColour);
        bus.addListener(this::registerItemColour);
    }
    private void setupClient(final FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(TANK, RenderType.cutout());
        BlockEntityRenderers.register(ENTITY_TYPE, ctx -> new TankBlockEntityRenderer());
    }

    private void registerBlockColour(final ColorHandlerEvent.Block event) {
        event.getBlockColors().register(TANK, TANK);
    }
    
    private void registerItemColour(final ColorHandlerEvent.Item event) {
        event.getItemColors().register(TANK, TANK);
    }

}
