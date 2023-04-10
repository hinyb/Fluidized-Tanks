package zone.rong.fluidizedtanks;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
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
import zone.rong.fluidizedtanks.block.TankBlock;
import zone.rong.fluidizedtanks.block.TankBlockEntity;
import zone.rong.fluidizedtanks.client.TankBlockEntityRenderer;
import zone.rong.fluidizedtanks.data.TankDefinitionManager;

@Mod("fluidizedtanks")
public class FluidizedTanks {

    public static TankBlock TANK;
    public static BlockEntityType<TankBlockEntity> ENTITY_TYPE;

    public FluidizedTanks() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addGenericListener(Block.class, this::registerBlock);
        bus.addGenericListener(BlockEntityType.class, this::registerBlockEntity);
        bus.addGenericListener(Item.class, this::registerItem);

        bus.addListener(this::setupClient);
        bus.addListener(this::registerBlockColour);
        bus.addListener(this::registerItemColour);

        MinecraftForge.EVENT_BUS.addListener(TankDefinitionManager::listenAddReload);
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
        BlockItem tankItem = new BlockItem(TANK, new Item.Properties().tab(CreativeModeTab.TAB_REDSTONE).stacksTo(1));
        tankItem.setRegistryName(TANK.getRegistryName());
        event.getRegistry().register(tankItem);
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