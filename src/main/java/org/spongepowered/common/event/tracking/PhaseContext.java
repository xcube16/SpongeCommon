/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.event.tracking;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.Multimap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

/**
 * Similar to {@link Cause} except it can be built continuously
 * and retains no real side effects. Strictly speaking this object
 * exists to avoid confusion between what is suggested to be a
 * {@link Cause} for an {@link Event} versus the context of which
 * a {@link IPhaseState} is being completed with.
 */
public class PhaseContext {

    private boolean isCompleted = false;

    @Nullable private CapturedBlocksSupplier blocksSupplier;
    @Nullable private BlockItemDropsSupplier blockItemDropsSupplier;
    @Nullable private BlockItemEntityDropsSupplier blockItemEntityDropsSupplier;
    @Nullable private CapturedItemsSupplier capturedItemsSupplier;
    @Nullable private CapturedEntitiesSupplier capturedEntitiesSupplier;
    @Nullable private CapturedItemStackSupplier capturedItemStackSupplier;
    @Nullable private EntityItemDropsSupplier entityItemDropsSupplier;
    @Nullable private EntityItemEntityDropsSupplier entityItemEntityDropsSupplier;
    @Nullable private CaptureBlockSnapshotForTile captureBlockSnapshotForTile;
    @Nullable protected User owner;
    @Nullable protected User notifier;
    @Nullable protected Explosion capturedExplosion;
    protected boolean processImmediately;

    private Object source;

    public static PhaseContext start() {
        return new PhaseContext();
    }

    public PhaseContext source(Object owner) {
        checkState(!this.isCompleted, "Cannot add a new object to the context if it's already marked as completed!");
        this.source = owner;
        return this;
    }

    public PhaseContext owner(User owner) {
        checkState(!this.isCompleted, "Cannot add a new object to the context if it's already marked as completed!");
        if (this.owner != null) {
            throw new IllegalStateException("Owner for this phase context is already set!");
        }
        this.owner = checkNotNull(owner, "Owner cannot be null!");
        return this;
    }

    public PhaseContext notifier(User notifier) {
        checkState(!this.isCompleted, "Cannot add a new object to the context if it's already marked as completed!");
        if (this.notifier != null) {
            throw new IllegalStateException("Notifier for this phase context is already set!");
        }
        this.notifier = checkNotNull(notifier, "Notifier cannot be null!");
        return this;
    }

    private void checkBlockSuppliers() {
        checkState(this.blocksSupplier == null, "BlocksSuppler is already set!");
        checkState(this.blockItemEntityDropsSupplier == null, "BlockItemEntityDropsSupplier is already set!");
        checkState(this.blockItemDropsSupplier == null, "BlockItemDropsSupplier is already set!");
    }

    public PhaseContext addBlockCaptures() {
        checkState(!this.isCompleted, "Cannot add a new object to the context if it's already marked as completed!");
        this.checkBlockSuppliers();

        CapturedBlocksSupplier blocksSupplier = new CapturedBlocksSupplier();
        this.blocksSupplier = blocksSupplier;
        BlockItemEntityDropsSupplier blockItemEntityDropsSupplier = new BlockItemEntityDropsSupplier();
        this.blockItemEntityDropsSupplier = blockItemEntityDropsSupplier;
        BlockItemDropsSupplier blockItemDropsSupplier = new BlockItemDropsSupplier();
        this.blockItemDropsSupplier = blockItemDropsSupplier;
        return this;
    }

    public PhaseContext addCaptures() {
        checkState(!this.isCompleted, "Cannot add a new object to the context if it's already marked as completed!");
        this.checkBlockSuppliers();
        checkState(this.capturedItemsSupplier == null, "CapturedItemsSupplier is already set!");
        checkState(this.capturedEntitiesSupplier == null, "CapturedEntitiesSupplier is already set!");
        checkState(this.capturedItemStackSupplier == null, "CapturedItemStackSupplier is already set!");

        CapturedBlocksSupplier blocksSupplier = new CapturedBlocksSupplier();
        this.blocksSupplier = blocksSupplier;
        BlockItemEntityDropsSupplier blockItemEntityDropsSupplier = new BlockItemEntityDropsSupplier();
        this.blockItemEntityDropsSupplier = blockItemEntityDropsSupplier;
        BlockItemDropsSupplier blockItemDropsSupplier = new BlockItemDropsSupplier();
        this.blockItemDropsSupplier = blockItemDropsSupplier;
        CapturedItemsSupplier capturedItemsSupplier = new CapturedItemsSupplier();
        this.capturedItemsSupplier = capturedItemsSupplier;
        CapturedEntitiesSupplier capturedEntitiesSupplier = new CapturedEntitiesSupplier();
        this.capturedEntitiesSupplier = capturedEntitiesSupplier;
        CapturedItemStackSupplier capturedItemStackSupplier = new CapturedItemStackSupplier();
        this.capturedItemStackSupplier = capturedItemStackSupplier;
        return this;
    }

    public PhaseContext addEntityCaptures() {
        checkState(!this.isCompleted, "Cannot add a new object to the context if it's already marked as completed!");
        checkState(this.capturedItemsSupplier == null, "CapturedItemsSupplier is already set!");
        checkState(this.capturedEntitiesSupplier == null, "CapturedEntitiesSupplier is already set!");
        checkState(this.capturedItemStackSupplier == null, "CapturedItemStackSupplier is already set!");

        CapturedItemsSupplier capturedItemsSupplier = new CapturedItemsSupplier();
        this.capturedItemsSupplier = capturedItemsSupplier;
        CapturedEntitiesSupplier capturedEntitiesSupplier = new CapturedEntitiesSupplier();
        this.capturedEntitiesSupplier = capturedEntitiesSupplier;
        CapturedItemStackSupplier capturedItemStackSupplier = new CapturedItemStackSupplier();
        this.capturedItemStackSupplier = capturedItemStackSupplier;
        return this;
    }

    public PhaseContext addEntityDropCaptures() {
        checkState(!this.isCompleted, "Cannot add a new object to the context if it's already marked as completed!");
        checkState(this.entityItemDropsSupplier == null, "EntityItemDropsSupplier is already set!");
        checkState(this.entityItemEntityDropsSupplier == null, "EntityItemEntityDropsSupplier is already set!");

        EntityItemDropsSupplier entityItemDropsSupplier = new EntityItemDropsSupplier();
        this.entityItemDropsSupplier = entityItemDropsSupplier;
        EntityItemEntityDropsSupplier entityItemEntityDropsSupplier = new EntityItemEntityDropsSupplier();
        this.entityItemEntityDropsSupplier = entityItemEntityDropsSupplier;
        return this;
    }

    public PhaseContext complete() {
        this.isCompleted = true;
        return this;
    }

    public boolean isComplete() {
        return this.isCompleted;
    }

    public boolean shouldProcessImmediately() {
        return this.processImmediately;
    }

    public void setProcessImmediately(boolean state) {
        this.processImmediately = state;
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getSource(Class<T> sourceClass) {
        if (this.source == null) {
            return Optional.empty();
        }
        if (sourceClass.isInstance(this.source)) {
            return Optional.of((T) this.source);
        }
        return Optional.empty();
    }

    public Optional<User> getOwner() {
        return Optional.ofNullable(this.owner);
    }

    public Optional<User> getNotifier() {
        return Optional.ofNullable(this.notifier);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Entity> getCapturedEntities() throws IllegalStateException {
        return this.capturedEntitiesSupplier.get();
    }

    @SuppressWarnings("unchecked")
    public CapturedSupplier<Entity> getCapturedEntitySupplier() throws IllegalStateException {
        if (this.capturedEntitiesSupplier == null) {
            throw TrackingUtil.throwWithContext("Intended to capture entity spawns!", this).get();
        }
        return this.capturedEntitiesSupplier;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<EntityItem> getCapturedItems() throws IllegalStateException {
        if (this.capturedItemsSupplier == null) {
            throw TrackingUtil.throwWithContext("Intended to capture dropped item entities!", this).get();
        }
        return this.capturedItemsSupplier.get();
    }

    @SuppressWarnings("unchecked")
    public CapturedSupplier<EntityItem> getCapturedItemsSupplier() throws IllegalStateException {
        if (this.capturedItemsSupplier == null) {
            throw TrackingUtil.throwWithContext("Intended to capture dropped item entities!", this).get();
        }
        return this.capturedItemsSupplier;
    }

    @SuppressWarnings("unchecked")
    public List<BlockSnapshot> getCapturedBlocks() throws IllegalStateException {
        return this.blocksSupplier.get();
    }

    @SuppressWarnings("unchecked")
    public CapturedSupplier<BlockSnapshot> getCapturedBlockSupplier() throws IllegalStateException {
        if (this.blocksSupplier == null) {
            throw TrackingUtil.throwWithContext("Expected to be capturing blocks, but we're not capturing them!", this).get();
        }
        return this.blocksSupplier;
    }

    public Multimap<BlockPos, ItemDropData> getCapturedBlockDrops() throws IllegalStateException {
        if (this.blockItemDropsSupplier == null) {
            throw TrackingUtil.throwWithContext("Expected to be capturing block drops!", this).get();
        }
        return this.blockItemDropsSupplier.get();
    }

    @SuppressWarnings("unchecked")
    public CapturedMultiMapSupplier<BlockPos, ItemDropData> getBlockDropSupplier() throws IllegalStateException {
        if (this.blockItemDropsSupplier == null) {
            throw TrackingUtil.throwWithContext("Expected to be capturing block drops!", this).get();
        }
        return this.blockItemDropsSupplier;
    }

    @SuppressWarnings("unchecked")
    public CapturedMultiMapSupplier<BlockPos, EntityItem> getBlockItemDropSupplier() throws IllegalStateException {
        if (this.blockItemEntityDropsSupplier == null) {
            throw TrackingUtil.throwWithContext("Intended to track block item drops!", this).get();
        }
        return this.blockItemEntityDropsSupplier;
    }

    @SuppressWarnings("unchecked")
    public CapturedMultiMapSupplier<UUID, ItemDropData> getCapturedEntityDropSupplier() throws IllegalStateException {
        if (this.entityItemDropsSupplier == null) {
            throw TrackingUtil.throwWithContext("Intended to capture entity drops!", this).get();
        }
        return this.entityItemDropsSupplier;
    }

    @SuppressWarnings("unchecked")
    public CapturedMultiMapSupplier<UUID, EntityItem> getCapturedEntityItemDropSupplier() throws IllegalStateException {
        if (this.entityItemEntityDropsSupplier == null) {
            throw TrackingUtil.throwWithContext("Intended to capture entity drops!", this).get();
        }
        return this.entityItemEntityDropsSupplier;
    }

    @SuppressWarnings("unchecked")
    public CapturedSupplier<ItemDropData> getCapturedItemStackSupplier() throws IllegalStateException {
        if (this.capturedItemStackSupplier == null) {
            throw TrackingUtil.throwWithContext("Expected to be capturing ItemStack drops from entities!", this).get();
        }
        return this.capturedItemStackSupplier;
    }

    public PhaseContext addTileSnapshotCapture(TileEntity tileEntity, WorldServer worldServer) {
        this.captureBlockSnapshotForTile = new CaptureBlockSnapshotForTile(tileEntity, worldServer);
        return this;
    }

    public CaptureBlockSnapshotForTile getTileSnapshot() throws IllegalStateException {
        return this.captureBlockSnapshotForTile;
    }

    public PhaseContext explosion(@Nullable Explosion explosion) {
        checkState(!this.isCompleted, "Cannot add a new object to the context if it's already marked as completed!");
        this.capturedExplosion = explosion;
        return this;
    }

    public Explosion getCaptureExplosion() {
        return this.capturedExplosion;
    }

    PhaseContext() {
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.isCompleted);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final PhaseContext other = (PhaseContext) obj;
        return Objects.equals(this.isCompleted, other.isCompleted);
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("isCompleted", this.isCompleted)
                .toString();
    }

    static class BlockItemDropsSupplier extends CapturedMultiMapSupplier<BlockPos, ItemDropData> {

        BlockItemDropsSupplier() {
        }

    }

    static class EntityItemDropsSupplier extends CapturedMultiMapSupplier<UUID, ItemDropData> {

        EntityItemDropsSupplier() {
        }
    }

    static final class CapturedItemsSupplier extends CapturedSupplier<EntityItem> {

        CapturedItemsSupplier() {
        }
    }

    static final class CapturedItemStackSupplier extends CapturedSupplier<ItemDropData> {

        CapturedItemStackSupplier() {
        }
    }

    static final class CapturedBlocksSupplier extends CapturedSupplier<BlockSnapshot> {

        CapturedBlocksSupplier() {
        }
    }

    static final class CapturedEntitiesSupplier extends CapturedSupplier<Entity> {

        CapturedEntitiesSupplier() {
        }
    }

    static final class EntityItemEntityDropsSupplier extends CapturedMultiMapSupplier<UUID, EntityItem> {

        EntityItemEntityDropsSupplier() {
        }
    }

    static final class BlockItemEntityDropsSupplier extends CapturedMultiMapSupplier<BlockPos, EntityItem> {
        BlockItemEntityDropsSupplier() {
        }
    }

    public static final class CapturePlayer {

        @Nullable private Player player;

        CapturePlayer() {

        }

        CapturePlayer(@Nullable Player player) {
            this.player = player;
        }

        public Optional<Player> getPlayer() {
            return Optional.ofNullable(this.player);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            CapturePlayer that = (CapturePlayer) o;
            return com.google.common.base.Objects.equal(this.player, that.player);
        }

        @Override
        public int hashCode() {
            return com.google.common.base.Objects.hashCode(this.player);
        }

        @Override
        public String toString() {
            return com.google.common.base.Objects.toStringHelper(this)
                    .add("player", this.player)
                    .toString();
        }

        public void addPlayer(EntityPlayerMP playerMP) {
            this.player = ((Player) playerMP);
        }
    }

    public static final class CaptureExplosion {

        @Nullable private Explosion explosion;

        CaptureExplosion() {

        }

        CaptureExplosion(@Nullable Explosion explosion) {
            this.explosion = explosion;
        }

        public Optional<Explosion> getExplosion() {
            return Optional.ofNullable(this.explosion);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            CaptureExplosion that = (CaptureExplosion) o;
            return com.google.common.base.Objects.equal(this.explosion, that.explosion);
        }

        @Override
        public int hashCode() {
            return com.google.common.base.Objects.hashCode(this.explosion);
        }

        @Override
        public String toString() {
            return com.google.common.base.Objects.toStringHelper(this)
                    .add("explosion", this.explosion)
                    .toString();
        }

        public void addExplosion(Explosion explosion) {
            this.explosion = explosion;
        }
    }

    public static final class CaptureFlag {

        @Nullable private BlockChangeFlag flag;

        public CaptureFlag() {

        }

        public CaptureFlag(@Nullable BlockChangeFlag flag) {

        }

        public Optional<BlockChangeFlag> getFlag() {
            return Optional.ofNullable(this.flag);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            CaptureFlag that = (CaptureFlag) o;
            return this.flag == that.flag;
        }

        @Override
        public int hashCode() {
            return com.google.common.base.Objects.hashCode(this.flag);
        }

        public void addFlag(BlockChangeFlag flag) {
            this.flag = flag;
        }
    }

    public static final class CaptureBlockSnapshotForTile {

        @Nullable private BlockSnapshot snapshot;
        private IBlockState tileState;
        private BlockPos tilePosition;
        private TileEntity owningEntity;
        private WorldServer worldServer;

        public CaptureBlockSnapshotForTile(TileEntity tileEntity, WorldServer worldServer) {
            this.owningEntity = tileEntity;
            this.worldServer = worldServer;
            this.tilePosition = tileEntity.getPos();
            this.tileState = worldServer.getBlockState(tileEntity.getPos());
        }

        public BlockSnapshot getSnapshot() {
            if (this.snapshot == null) {
                this.snapshot = ((IMixinWorldServer) this.worldServer).createSpongeBlockSnapshot(this.tileState, this.tileState, this.tilePosition, 0);
            }
            return this.snapshot;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            CaptureBlockSnapshotForTile that = (CaptureBlockSnapshotForTile) o;
            return com.google.common.base.Objects.equal(this.snapshot, that.snapshot) &&
                   com.google.common.base.Objects.equal(this.tileState, that.tileState) &&
                   com.google.common.base.Objects.equal(this.tilePosition, that.tilePosition) &&
                   com.google.common.base.Objects.equal(this.owningEntity, that.owningEntity) &&
                   com.google.common.base.Objects.equal(this.worldServer, that.worldServer);
        }

        @Override
        public int hashCode() {
            return com.google.common.base.Objects.hashCode(this.snapshot, this.tileState, this.tilePosition, this.owningEntity, this.worldServer);
        }

        @Override
        public String toString() {
            return com.google.common.base.Objects.toStringHelper(this)
                    .add("tileState", this.tileState)
                    .add("tilePosition", this.tilePosition)
                    .add("owningEntity", this.owningEntity)
                    .add("worldServer", this.worldServer)
                    .toString();
        }
    }
}
