package zone.rong.fluidizedtanks.data;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.slf4j.Logger;
import zone.rong.fluidizedtanks.FluidizedTanks;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class TankDefinitionManager extends SimpleJsonResourceReloadListener {

    public static TankDefinitionManager instance;

    private static final Gson GSON = new Gson();
    private static final Logger LOGGER = LogUtils.getLogger();

    private Map<ResourceLocation, TankDefinition> definitions;

    public static void listenAddReload(AddReloadListenerEvent event) {
        event.addListener(instance = new TankDefinitionManager());
    }

    public static void listenOnDatapackSync(OnDatapackSyncEvent event) {
        event.getPlayerList().getPlayers().forEach(TankDefinitionManager::sendToPlayer);
    }

    public static void listenOnPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        sendToPlayer((ServerPlayer) event.getPlayer());
    }

    private static void sendToPlayer(ServerPlayer player) {
        SimpleChannel channel = FluidizedTanks.NETWORK_CHANNEL;
        // This ensures we don't fire unnecessary packets such as in an SSP environment
        if (channel.isRemotePresent(player.connection.getConnection()) && !player.connection.getConnection().isMemoryConnection()) {
            channel.send(PacketDistributor.PLAYER.with(() -> player), new S2CUpdateTankDefinitionsPacket(instance.definitions));
        }
    }

    public TankDefinitionManager() {
        super(GSON, "fluidizedtanks");
    }

    public Map<ResourceLocation, TankDefinition> getDefinitions() {
        return Collections.unmodifiableMap(definitions);
    }

    public void refresh(Map<ResourceLocation, TankDefinition> newDefinitions) {
        this.definitions = ImmutableMap.copyOf(newDefinitions);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager manager, ProfilerFiller profiler) {
        this.definitions = null;
        ImmutableMap.Builder<ResourceLocation, TankDefinition> definitionsBuilder = ImmutableMap.builder();
        for (Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
            ResourceLocation name = entry.getKey();
            try {
                // TODO: Gson not >2.10 TankDefinition definition = GSON.fromJson(entry.getValue(), TankDefinition.class);
                JsonElement value = entry.getValue();
                if (value instanceof JsonObject json) {
                    JsonElement capacityMember = json.get("capacity");
                    if (capacityMember.isJsonPrimitive()) {
                        int capacity = capacityMember.getAsInt();
                        JsonElement colourMember = json.get("colour");
                        colourMember = colourMember == null ? json.get("color") : colourMember;
                        if (colourMember instanceof JsonPrimitive colourPrimitive) {
                            int colour = colourPrimitive.isString() ? (int) (long) Long.decode(colourPrimitive.getAsString()) : colourPrimitive.getAsInt();
                            definitionsBuilder.put(name, new TankDefinition(name, capacity, colour));
                        } else {
                            LOGGER.info("Skipping {} as the colour wasn't specified.", name);
                        }
                    } else {
                        LOGGER.info("Skipping {} as the capacity wasn't specified.", name);
                    }
                } else {
                    LOGGER.info("Skipping {} as the JSON is invalid.", name);
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        this.definitions = definitionsBuilder.build();
    }

}
