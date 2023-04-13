package zone.rong.fluidizedtanks.block.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import zone.rong.fluidizedtanks.data.TankDefinition;

public class TankBlockItem extends BlockItem {

    public TankBlockItem(Block block) {
        super(block, new Item.Properties().tab(CreativeModeTab.TAB_REDSTONE));
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        TankDefinition definition = TankDefinition.get(stack);
        if (definition != null) {
            return definition.getDescriptionId();
        }
        return super.getDescriptionId(stack);
    }

}
