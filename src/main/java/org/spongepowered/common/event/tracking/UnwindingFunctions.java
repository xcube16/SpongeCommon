package org.spongepowered.common.event.tracking;

import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnType;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;

import java.util.List;
import java.util.stream.Collectors;

public class UnwindingFunctions {

    public static void processBlocks(CauseTracker causeTracker, IPhaseState state, PhaseContext context) {
        context.getCapturedBlockSupplier()
                .ifPresentAndNotEmpty(blocks ->
                        TrackingUtil.processBlockCaptures(blocks, causeTracker, state, context));
    }

    private static void spawnFromSupplier(EntityPlayerMP player, CapturedSupplier<? extends net.minecraft.entity.Entity> supplier, SpawnType type, SpawnEntityEventFunction<SpawnEntityEvent> function) {
        IMixinWorldServer mixinWorldServer = (IMixinWorldServer) player.worldObj;
        supplier.ifPresentAndNotEmpty(entities -> {
            final List<Entity> items = entities.stream().map(EntityUtil::fromNative).collect(Collectors.toList());
            final Cause cause = Cause.source(EntitySpawnCause.builder()
                    .entity((Player) player)
                    .type(type)
                    .build()
            ).build();
            SpawnEntityEvent event = function.create(cause, items, (World) mixinWorldServer);
            SpongeImpl.postEvent(event);
            if (!event.isCancelled()) {
                for (Entity spawnedEntity : event.getEntities()) {
                    spawnedEntity.setCreator(player.getUniqueID());
                    mixinWorldServer.forceSpawnEntity(spawnedEntity);
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static void spawnEntities(EntityPlayerMP player, PhaseContext context, SpawnType type, SpawnEntityEventFunction<SpawnEntityEvent> function) {
        spawnFromSupplier(player, (CapturedSupplier<net.minecraft.entity.Entity>) (Object) context.getCapturedEntitySupplier(), type, function);
    }

    public static void spawnEntities(EntityPlayerMP player, PhaseContext context, SpawnType type) {
        spawnEntities(player, context, type, SpongeEventFactory::createSpawnEntityEvent);
    }

    public static void spawnEntityItems(EntityPlayerMP player, PhaseContext context, SpawnType type, SpawnEntityEventFunction<SpawnEntityEvent> function) {
        spawnFromSupplier(player, context.getCapturedItemsSupplier(), type, function);
    }

    public static void spawnEntityItems(EntityPlayerMP player, PhaseContext context, SpawnType type) {
        spawnEntityItems(player, context, type, SpongeEventFactory::createSpawnEntityEvent);
    }

}
