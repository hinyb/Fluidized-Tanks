package zone.rong.fluidizedtanks.data;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class S2CUpdateTankDefinitionsPacket {

    public static void register(SimpleChannel channel, int id) {
        channel.registerMessage(id, S2CUpdateTankDefinitionsPacket.class, S2CUpdateTankDefinitionsPacket::encoder, S2CUpdateTankDefinitionsPacket::decoder,
                S2CUpdateTankDefinitionsPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    private static S2CUpdateTankDefinitionsPacket decoder(FriendlyByteBuf buffer) {
        int length = buffer.readVarInt();
        Map<ResourceLocation, TankDefinition> definitions = new HashMap<>(length);
        for (int i = 0; i < length; i++) {
            definitions.put(buffer.readResourceLocation(), TankDefinition.get(buffer.readNbt()));
        }
        return new S2CUpdateTankDefinitionsPacket(definitions);
    }

    private final Map<ResourceLocation, TankDefinition> definitions = new HashMap<>();

    public S2CUpdateTankDefinitionsPacket(Map<ResourceLocation, TankDefinition> definitions) {
        this.definitions.putAll(definitions);
    }

    private void encoder(FriendlyByteBuf buffer) {
        buffer.writeVarInt(definitions.size()); // Length
        for (Map.Entry<ResourceLocation, TankDefinition> entry : definitions.entrySet()) { // Values
            buffer.writeResourceLocation(entry.getKey());
            buffer.writeNbt(entry.getValue().save());
        }
    }

    private void handle(Supplier<Context> ctx) {
        ctx.get().enqueueWork(() -> {
            TankDefinitionManager.instance = new TankDefinitionManager();
            TankDefinitionManager.instance.refresh(definitions);
        });
        ctx.get().setPacketHandled(true);
    }

}
