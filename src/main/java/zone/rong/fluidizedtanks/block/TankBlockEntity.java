package zone.rong.fluidizedtanks.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zone.rong.fluidizedtanks.FluidizedTanks;
import zone.rong.fluidizedtanks.TankDefinition;

import java.util.Optional;

public class TankBlockEntity extends BlockEntity {

    public static void tick(Level level, BlockPos pos, BlockState state, TankBlockEntity tile) {
        if (!tile.tank.isEmpty()) {
            FluidStack currentFluid = tile.tank.getFluid();
            TankBlockEntity otherTile = null;
            FluidAttributes fluidAttributes = currentFluid.getFluid().getAttributes();
            if (fluidAttributes.isLighterThanAir()) {
                BlockEntity upTile = level.getBlockEntity(pos.above());
                if (upTile instanceof TankBlockEntity upTankTile) {
                    otherTile = upTankTile;
                }
            } else {
                BlockEntity downTile = level.getBlockEntity(pos.below());
                if (downTile instanceof TankBlockEntity downTankTile) {
                    otherTile = downTankTile;
                }
            }
            if (otherTile == null) {
                return;
            }
            FluidUtil.tryFluidTransfer(otherTile.tank, tile.tank, 100, true); // TODO: change transfer speed based on viscosity
        }
    }

    private final NotifiableFluidTank tank;
    private final LazyOptional<IFluidHandler> capability;

    private TankDefinition tankDefinition;

    public TankBlockEntity(BlockPos pos, BlockState state) {
        super(FluidizedTanks.ENTITY_TYPE, pos, state);
        this.tank = new NotifiableFluidTank(this);
        this.capability = LazyOptional.of(() -> this.tank);
    }

    public Optional<TankDefinition> getDefinition() {
        return Optional.ofNullable(this.tankDefinition);
    }

    public void loadDefinition(ItemStack stack) {
        if (stack.getTag() != null) {
            this.load(stack.getTag());
        }
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

    @Override
    public void setChanged() {
        if (this.level != null) {
            setChanged(this.level, this.worldPosition, this.getBlockState());
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        this.tank.writeToNBT(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        this.tank.readFromNBT(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.tankDefinition != null) {
            CompoundTag tankDefinitionTag = this.tankDefinition.save();
            tag.put("TankDefinition", tankDefinitionTag);
            if (this.tank != null && !this.tank.getFluid().isEmpty()) {
                CompoundTag storedFluidTag = new CompoundTag();
                this.tank.writeToNBT(storedFluidTag);
                tag.put("StoredFluid", storedFluidTag);
            }
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (this.tankDefinition == null && tag.contains("TankDefinition", Tag.TAG_COMPOUND)) {
            CompoundTag tankDefinitionTag = tag.getCompound("TankDefinition");
            this.tankDefinition = TankDefinition.get(tankDefinitionTag);
            if (tag.contains("StoredFluid", Tag.TAG_COMPOUND)) {
                this.tank.readFromNBT(tag.getCompound("StoredFluid"));
            }
        }
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return this.capability.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.capability.invalidate();
    }

}
