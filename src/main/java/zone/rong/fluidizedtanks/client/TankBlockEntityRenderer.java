package zone.rong.fluidizedtanks.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import zone.rong.fluidizedtanks.block.TankBlockEntity;

public class TankBlockEntityRenderer implements BlockEntityRenderer<TankBlockEntity> {

    private static final float TANK_W = 0.001F; // Avoiding z-fighting
    private static final int FULLBRIGHT = 0xF000F0;
    private static final CubeBuilder BUILDER = new CubeBuilder();

    private static TextureAtlasSprite lastSprite;
    private static float lastMinY = Float.NEGATIVE_INFINITY, lastMaxY = Float.NEGATIVE_INFINITY;

    @Override
    public void render(TankBlockEntity entity, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay) {
        FluidStack fluid = entity.getFluid();
        if (!fluid.isEmpty()) {
            FluidAttributes attributes = fluid.getFluid().getAttributes();
            TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(attributes.getStillTexture(fluid));
            int colour;
            if (entity.getLevel() != null) {
                colour = attributes.getColor(entity.getLevel(), entity.getBlockPos());
            } else {
                colour = attributes.getColor(fluid);
            }
            float r = ((colour >> 16) & 0xFF) / (float) 0xFF;
            float g = ((colour >> 8) & 0xFF) / (float) 0xFF;
            float b = ((colour) & 0xFF) / 256F;
            // float a = ((colour >> 24) & 0xFF) / (float) 0xFF;
            float fillY = Mth.lerp(Mth.clamp(entity.getFill(), 0, 1), TANK_W, 1 - TANK_W);
            float topHeight = fillY;
            float bottomHeight = TANK_W;
            if (attributes.isLighterThanAir()) {
                topHeight = 1 - TANK_W;
                bottomHeight = 1 - fillY;
            }
            float minY = bottomHeight;
            float maxY = topHeight;
            if (lastMinY != minY || lastMaxY != maxY || lastSprite != sprite) {
                BUILDER.clear();
                lastMinY = minY;
                lastMaxY = maxY;
                lastSprite = sprite;
                BUILDER.setTexture(sprite);
                BUILDER.addCube(TANK_W * 16, minY * 16, TANK_W * 16, (1 - TANK_W) * 16, maxY  * 16, (1 - TANK_W) * 16);
            }
            VertexConsumer consumer = buffer.getBuffer(RenderType.translucentMovingBlock());
            for (BakedQuad quad : BUILDER.getOutput()) {
                consumer.putBulkData(matrixStack.last(), quad, r, g, b, FULLBRIGHT, OverlayTexture.NO_OVERLAY);
            }
        }
    }

}
