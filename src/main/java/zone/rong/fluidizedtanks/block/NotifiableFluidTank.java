package zone.rong.fluidizedtanks.block;

import net.minecraftforge.fluids.capability.templates.FluidTank;

public class NotifiableFluidTank extends FluidTank {

    private final TankBlockEntity entity;

    public NotifiableFluidTank(TankBlockEntity entity) {
        super(0);
        this.entity = entity;
    }

    @Override
    protected void onContentsChanged() {
        super.onContentsChanged();
        this.entity.setChanged();
    }

}
