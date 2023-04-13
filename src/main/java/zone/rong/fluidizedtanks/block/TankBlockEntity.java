package zone.rong.fluidizedtanks.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;
import zone.rong.fluidizedtanks.FluidizedTanks;
import zone.rong.fluidizedtanks.data.TankDefinition;

import java.util.Optional;

public class TankBlockEntity extends BlockEntity {

    // Every tick the tank will try to seek for other tanks above and below to transfer fluids to
    // Top if fluid rises (lighter than air or same as air)
    // Bottom if fluid falls (heavier than air)
    public static void tick(Level level, BlockPos pos, BlockState state, TankBlockEntity tile) {
        if (!tile.tank.isEmpty()) {
            FluidStack currentFluid = tile.tank.getFluid();
            TankBlockEntity otherTile = null;
            FluidAttributes fluidAttributes = currentFluid.getFluid().getAttributes();
            if (fluidAttributes.isLighterThanAir()) {
                if (level.getBlockEntity(pos.above()) instanceof TankBlockEntity upTankTile) {
                    otherTile = upTankTile;
                }
            } else {
                if (level.getBlockEntity(pos.below()) instanceof TankBlockEntity downTankTile) {
                    otherTile = downTankTile;
                }
            }
            if (otherTile == null) {
                return;
            }
            FluidUtil.tryFluidTransfer(otherTile.tank, tile.tank, 100, true); // TODO: change transfer speed based on viscosity
        }
    }

    // Every time the tank's content changes, the change has to be reflected on the client
    private final FluidTank tank = new FluidTank(FluidAttributes.BUCKET_VOLUME) {
        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            TankBlockEntity.this.syncToClient();
        }
    };
    private LazyOptional<IFluidHandler> holder = LazyOptional.of(() -> this.tank);

    private TankDefinition tankDefinition;

    public TankBlockEntity(BlockPos pos, BlockState state) {
        super(FluidizedTanks.ENTITY_TYPE, pos, state);
    }

    public void loadDefinition(ItemStack stack) {
        this.tankDefinition = TankDefinition.get(stack);
    }

    public Optional<TankDefinition> getTankDefinition() {
        return Optional.of(tankDefinition);
    }

    public FluidStack getFluid() {
        return this.tank.getFluid();
    }

    public int getAmount() {
        return this.tank.getFluidAmount();
    }

    public int getCapacity() {
        return this.tank.getCapacity();
    }

    public float getFill() {
        return (float) this.getAmount() / this.getCapacity();
    }

    public int getComparatorValue() {
        return (int) (15 * this.getFill());
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        if (this.tank != null) {
            this.tank.writeToNBT(tag);
        }
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        if (this.tank != null) {
            this.tank.readFromNBT(tag);
        }
    }

    // Persistency
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.tankDefinition != null) {
            tag.put("TankDefinition", this.tankDefinition.save());
        }
        CompoundTag tankTag = new CompoundTag();
        this.tank.writeToNBT(tankTag);
        tag.put("Tank", tankTag);
    }

    // Persistency
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("TankDefinition", Tag.TAG_COMPOUND)) {
            this.tankDefinition = TankDefinition.get(tag.getCompound("TankDefinition"));
        }
        if (tag.contains("Tank", Tag.TAG_COMPOUND)) {
            this.tank.readFromNBT(tag.getCompound("Tank"));
        }
    }

    // New Forge nonsense
    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        holder.invalidate();
    }

    // New Forge nonsense
    @Override
    public void reviveCaps() {
        super.reviveCaps();
        holder = LazyOptional.of(() -> this.tank);
    }

    // This will allow us to fire the custom ClientboundBlockEntityDataPacket packet with `getUpdateTag`
    private void syncToClient() {
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

}
