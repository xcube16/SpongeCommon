package org.spongepowered.common.event.tracking;

import com.google.common.collect.Multimap;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnType;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.tracking.phase.packet.PacketFunction;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class UnwindingFunctions {

    public static void processBlocks(CauseTracker causeTracker, IPhaseState state, PhaseContext context) {
        context.getCapturedBlockSupplier()
                .ifPresentAndNotEmpty(blocks ->
                        TrackingUtil.processBlockCaptures(blocks, causeTracker, state, context));
    }

    public static void spawnFromList(EntityPlayerMP player, List<? extends net.minecraft.entity.Entity> entities, SpawnType type, SpawnEntityEventFunction<SpawnEntityEvent> function, Cause.Builder builder) {
        IMixinWorldServer mixinWorldServer = (IMixinWorldServer) player.worldObj;
        final List<Entity> items = entities.stream().map(EntityUtil::fromNative).collect(Collectors.toList());
        final Cause cause = builder.named(NamedCause.SOURCE, EntitySpawnCause.builder()
                .entity((Player) player)
                .type(type)
                .build()
        ).build();
        SpawnEntityEvent event = function.create(cause, items, (World) mixinWorldServer);
        SpongeImpl.postEvent(event);
        if (!event.isCancelled()) {
            PacketFunction.processEntities(player, mixinWorldServer.getCauseTracker(), event.getEntities());
        }
    }

    public static SpawnEntityEvent spawnItemDrops(CauseTracker causeTracker, CapturedMultiMapSupplier<UUID, EntityItem> supplier, EntityPlayerMP player, SpawnType type, SpawnEntityEventFunction<SpawnEntityEvent> function, Cause.Builder builder) {
        if (!supplier.isEmpty()) {
            Multimap<UUID, EntityItem> map = supplier.get();
            for (Map.Entry<UUID, Collection<EntityItem>> entry : map.asMap().entrySet()) {
                final UUID key = entry.getKey();
                final Optional<Entity> attackedEntities = causeTracker.getWorld().getEntity(key);
                if (!attackedEntities.isPresent()) {
                    continue;
                }
                final List<Entity> items = entry.getValue().stream().map(EntityUtil::fromNative).collect(Collectors.toList());
                final Cause cause = builder.named(NamedCause.SOURCE, EntitySpawnCause.builder()
                        .entity(EntityUtil.fromNative(player))
                        .type(type)
                        .build()
                )
                        .build();
                final SpawnEntityEvent
                        event =
                        function.create(cause, items, causeTracker.getWorld());
                SpongeImpl.postEvent(event);
                if (!event.isCancelled()) {
                    PacketFunction.processSpawnedEntities(player, causeTracker, event);
                }
                return event;
            }
        }
        return null;
    }

    public static void spawnEntities(EntityPlayerMP player, PhaseContext context, SpawnType type, SpawnEntityEventFunction<SpawnEntityEvent> function, Cause.Builder builder) {
        spawnFromList(player, (List<net.minecraft.entity.Entity>) (Object) context.getCapturedEntitySupplier().get(), type, function, builder);
    }

    @SuppressWarnings("unchecked")
    public static void spawnEntities(EntityPlayerMP player, PhaseContext context, SpawnType type, SpawnEntityEventFunction<SpawnEntityEvent> function) {
        spawnEntities(player, context, type, function, Cause.builder());
    }

    public static void spawnEntities(EntityPlayerMP player, PhaseContext context, SpawnType type) {
        spawnEntities(player, context, type, SpongeEventFactory::createSpawnEntityEvent);
    }

    public static void spawnEntityItems(EntityPlayerMP player, PhaseContext context, SpawnType type, SpawnEntityEventFunction<SpawnEntityEvent> function, Cause.Builder builder) {
        spawnFromList(player, (List<net.minecraft.entity.Entity>) (Object) context.getCapturedItemsSupplier().get(), type, function, builder);
    }

    public static void spawnEntityItems(EntityPlayerMP player, PhaseContext context, SpawnType type, SpawnEntityEventFunction<SpawnEntityEvent> function) {
        spawnEntityItems(player, context, type, function, Cause.builder());
    }

    public static void spawnEntityItems(EntityPlayerMP player, PhaseContext context, SpawnType type) {
        spawnEntityItems(player, context, type, SpongeEventFactory::createSpawnEntityEvent);
    }

    public static void spawnItemStacks(EntityPlayerMP player, PhaseContext context, SpawnType type) {
        context.getCapturedItemStackSupplier().ifPresentAndNotEmpty(drops -> {
            final List<EntityItem>
                    items =
                    drops.stream().map(drop -> drop.create(player.getServerWorld())).collect(Collectors.toList());
            UnwindingFunctions.spawnFromList(player, items, InternalSpawnTypes.CUSTOM, SpongeEventFactory::createDropItemEventCustom, Cause.builder().named(NamedCause.notifier(player)));
        });
    }

}
