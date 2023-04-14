package zone.rong.fluidizedtanks.block.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import zone.rong.fluidizedtanks.data.TankDefinition;
import zone.rong.fluidizedtanks.data.TankDefinitionManager;

import java.util.List;

public class TankBlockItem extends BlockItem {

    public TankBlockItem(Block block) {
        super(block, new Item.Properties().tab(CreativeModeTab.TAB_REDSTONE));
    }

    @Override
    public void verifyTagAfterLoad(CompoundTag tag) {
        // . . . . .
        if (tag.contains("TankDefinitionId", Tag.TAG_STRING)) {
            String id = tag.getString("TankDefinitionId");
            // if (StackWalker.getInstance().walk(stream -> stream.skip(3).findFirst()).filter(frame -> frame.getClassName().equals(CraftingHelper.class.getName())).isPresent()) {
                tag.remove("TankDefinitionId");
                TankDefinition definition = TankDefinitionManager.instance.getDefinitions().get(new ResourceLocation(id));
                tag.put("TankDefinition", definition.save());
            // }
        }
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        TankDefinition definition = TankDefinition.get(stack);
        if (definition != null) {
            return definition.getDescriptionId();
        }
        return super.getDescriptionId(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> texts, TooltipFlag flag) {
        if (stack.getTag() != null && stack.getTag().contains("Tank", Tag.TAG_COMPOUND)) {
            CompoundTag tankTag = stack.getTag().getCompound("Tank");
            FluidStack fluid = FluidStack.loadFluidStackFromNBT(tankTag);
            if (!fluid.isEmpty()) {
                MutableComponent name = fluid.getDisplayName().copy();
                name = name.withStyle(name.getStyle().withColor(fluid.getFluid().getAttributes().getColor(fluid))); // TODO: lava default?
                texts.add(new TranslatableComponent("tooltip.fluidizedtanks.fluid_amount", name, fluid.getAmount()));
            }
        }
    }

}
