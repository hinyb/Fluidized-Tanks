package zone.rong.fluidizedtanks.block;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidUtil;
import org.jetbrains.annotations.Nullable;
import zone.rong.fluidizedtanks.FluidizedTanks;
import zone.rong.fluidizedtanks.data.TankDefinition;
import zone.rong.fluidizedtanks.data.TankDefinitionManager;

public class TankBlock extends Block implements EntityBlock, BlockColor, ItemColor {

    public TankBlock() {
        super(Properties.of(Material.BUILDABLE_GLASS).strength(1F).sound(SoundType.GLASS).noOcclusion());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (FluidUtil.interactWithFluidHandler(player, hand, level, pos, hit.getDirection())) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TankBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_153212_, BlockState p_153213_, BlockEntityType<T> p_153214_) {
        return !p_153212_.isClientSide ? createTickerHelper(p_153214_, FluidizedTanks.ENTITY_TYPE, TankBlockEntity::tick) : null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        level.getBlockEntity(pos, FluidizedTanks.ENTITY_TYPE).ifPresent(tank -> tank.loadFromStack(stack));
    }

    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> list) {
        if (TankDefinitionManager.instance != null) {
            // TODO
            for (TankDefinition definition : TankDefinitionManager.instance.getDefinitions().values()) {
                ItemStack stack = new ItemStack(this);
                definition.load(stack);
                list.add(stack);
            }
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        ItemStack stack = super.getCloneItemStack(level, pos, state);
        level.getBlockEntity(pos, FluidizedTanks.ENTITY_TYPE).ifPresent(tank -> tank.saveToStack(stack));
        return stack;
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && !player.isCreative()) {
            ItemStack stack = getCloneItemStack(level, pos, state);
            ItemEntity itementity = new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, stack);
            itementity.setDefaultPickUpDelay();
            level.addFreshEntity(itementity);
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter getter, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos, FluidizedTanks.ENTITY_TYPE).map(TankBlockEntity::getComparatorValue).orElse(0);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public boolean skipRendering(BlockState selfState, BlockState neighbourState, Direction direction) {
        return neighbourState.is(this) || super.skipRendering(selfState, neighbourState, direction);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getColor(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex) {
        if (tintIndex == 0) {
            if (level != null && pos != null) {
                if (level.getBlockEntity(pos) instanceof TankBlockEntity tank) {
                    return tank.getTankDefinition().map(TankDefinition::colour).orElse(-1);
                }
            }
        }
        return -1;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getColor(ItemStack stack, int tintIndex) {
        if (tintIndex == 0) {
            TankDefinition tankDefinition = TankDefinition.get(stack);
            if (tankDefinition != null) {
                return tankDefinition.colour();
            }
        }
        return -1;
    }

    private static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> p_152133_, BlockEntityType<E> p_152134_, BlockEntityTicker<? super E> p_152135_) {
        return p_152134_ == p_152133_ ? (BlockEntityTicker<A>)p_152135_ : null;
    }

}
