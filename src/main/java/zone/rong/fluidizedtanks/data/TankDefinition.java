package zone.rong.fluidizedtanks.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import zone.rong.fluidizedtanks.block.TankBlockEntity;

public record TankDefinition(ResourceLocation id, int capacity, int colour) {

    public static @Nullable TankDefinition get(ItemStack stack) {
        if (stack.getTag() == null) {
            return null;
        }
        CompoundTag tankDefinitionTag = stack.getTag().getCompound("TankDefinition");
        return tankDefinitionTag.isEmpty() ? null : get(tankDefinitionTag);
    }

    public static TankDefinition get(CompoundTag tag) {
        if (tag.contains("Id", Tag.TAG_STRING)) {
            ResourceLocation id = new ResourceLocation(tag.getString("Id"));
            if (tag.contains("Capacity", Tag.TAG_ANY_NUMERIC)) {
                int capacity = tag.getInt("Capacity");
                if (tag.contains("Colour", Tag.TAG_INT)) {
                    int colour = tag.getInt("Colour");
                    return new TankDefinition(id, capacity, colour);
                }
            }
        }
        return null;
    }

    public static void load(TankBlockEntity tank, ItemStack stack) {
        tank.getTankDefinition().ifPresent(definition -> definition.load(stack));
    }

    public void load(ItemStack stack) {
        CompoundTag tag = new CompoundTag();
        tag.putString("Id", this.id.toString());
        tag.putInt("Capacity", this.capacity);
        tag.putInt("Colour", this.colour);
        stack.addTagElement("TankDefinition", tag);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Id", this.id.toString());
        tag.putInt("Capacity", this.capacity);
        tag.putInt("Colour", this.colour);
        return tag;
    }

}
