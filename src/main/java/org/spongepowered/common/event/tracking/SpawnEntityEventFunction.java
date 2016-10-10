package org.spongepowered.common.event.tracking;

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.world.World;

import java.util.List;

@FunctionalInterface
public interface SpawnEntityEventFunction<E extends SpawnEntityEvent> {

    E create(Cause cause, List<Entity> entities, World targetWorld);

}
