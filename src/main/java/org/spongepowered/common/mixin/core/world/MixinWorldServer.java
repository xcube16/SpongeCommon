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
package org.spongepowered.common.mixin.core.world;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import co.aikar.timings.TimingHistory;
import co.aikar.timings.WorldTimingsHandler;
import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.passive.EntitySkeletonHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.ITickable;
import net.minecraft.util.ReportedException;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.DimensionType;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.Explosion;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.ChunkProviderEnd;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldInfo;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.ScheduledBlockUpdate;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.DataMap;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.sound.SoundCategory;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.cause.entity.spawn.WeatherSpawnCause;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.world.ChangeWorldWeatherEvent;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.PositionOutOfBoundsException;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.GeneratorType;
import org.spongepowered.api.world.GeneratorTypes;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.PortalAgent;
import org.spongepowered.api.world.PortalAgentType;
import org.spongepowered.api.world.PortalAgentTypes;
import org.spongepowered.api.world.gen.BiomeGenerator;
import org.spongepowered.api.world.gen.WorldGenerator;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.api.world.storage.WorldStorage;
import org.spongepowered.api.world.weather.Weather;
import org.spongepowered.api.world.weather.Weathers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.type.WorldConfig;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.effect.particle.SpongeParticleEffect;
import org.spongepowered.common.effect.particle.SpongeParticleHelper;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.entity.EntityPhase;
import org.spongepowered.common.event.tracking.phase.general.GeneralPhase;
import org.spongepowered.common.event.tracking.phase.generation.GenerationPhase;
import org.spongepowered.common.event.tracking.phase.plugin.PluginPhase;
import org.spongepowered.common.event.tracking.phase.tick.TickPhase;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.IMixinNextTickListEntry;
import org.spongepowered.common.interfaces.block.IMixinBlock;
import org.spongepowered.common.interfaces.block.IMixinBlockEventData;
import org.spongepowered.common.interfaces.block.tile.IMixinTileEntity;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.server.management.IMixinPlayerChunkMap;
import org.spongepowered.common.interfaces.util.math.IMixinBlockPos;
import org.spongepowered.common.interfaces.world.IMixinExplosion;
import org.spongepowered.common.interfaces.world.IMixinServerWorldEventHandler;
import org.spongepowered.common.interfaces.world.IMixinWorldInfo;
import org.spongepowered.common.interfaces.world.IMixinWorldProvider;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.interfaces.world.gen.IMixinChunkProviderServer;
import org.spongepowered.common.interfaces.world.gen.IPopulatorProvider;
import org.spongepowered.common.mixin.plugin.entityactivation.interfaces.IModData_Activation;
import org.spongepowered.common.mixin.plugin.entitycollisions.interfaces.IModData_Collisions;
import org.spongepowered.common.registry.provider.DirectionFacingProvider;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.WorldManager;
import org.spongepowered.common.world.border.PlayerBorderListener;
import org.spongepowered.common.world.gen.SpongeChunkGenerator;
import org.spongepowered.common.world.gen.SpongeGenerationPopulator;
import org.spongepowered.common.world.gen.SpongeWorldGenerator;
import org.spongepowered.common.world.gen.WorldGenConstants;
import org.spongepowered.common.world.type.SpongeWorldType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import javax.annotation.Nullable;

@Mixin(WorldServer.class)
@Implements(@Interface(iface = IMixinWorldServer.class, prefix = "worldServer$", unique = true))
public abstract class MixinWorldServer extends MixinWorld implements IMixinWorldServer {

    private static final String PROFILER_SS = "Lnet/minecraft/profiler/Profiler;startSection(Ljava/lang/String;)V";
    private static final String PROFILER_ESS = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V";

    private static final Vector3i BLOCK_MIN = new Vector3i(-30000000, 0, -30000000);
    private static final Vector3i BLOCK_MAX = new Vector3i(30000000, 256, 30000000).sub(1, 1, 1);

    private static final EnumSet<EnumFacing> NOTIFY_DIRECTIONS = EnumSet.of(EnumFacing.WEST, EnumFacing.EAST, EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH);

    private final Map<net.minecraft.entity.Entity, Vector3d> rotationUpdates = new HashMap<>();
    private SpongeChunkGenerator spongegen;
    private SpongeConfig<?> activeConfig;
    protected long weatherStartTime;
    protected Weather prevWeather;
    protected WorldTimingsHandler timings = new WorldTimingsHandler((WorldServer) (Object) this);
    private int chunkGCTickCount = 0;
    private int chunkGCLoadThreshold = 0;
    private int chunkGCTickInterval = 600;
    private long chunkUnloadDelay = 30000;
    private boolean weatherThunderEnabled = true;
    private boolean weatherIceAndSnowEnabled = true;
    private int dimensionId;

    @Shadow @Final private MinecraftServer mcServer;
    @Shadow @Final private Set<NextTickListEntry> pendingTickListEntriesHashSet;
    @Shadow @Final private TreeSet<NextTickListEntry> pendingTickListEntriesTreeSet;
    @Shadow @Final private PlayerChunkMap playerChunkMap;
    @Shadow @Final @Mutable private Teleporter worldTeleporter;
    @Shadow @Final private WorldServer.ServerBlockEventList[] blockEventQueue;
    @Shadow private int blockEventCacheIndex;
    @Shadow private int updateEntityTick;

    @Shadow public abstract boolean fireBlockEvent(BlockEventData event);
    @Shadow protected abstract void createBonusChest();
    @Shadow @Nullable public abstract net.minecraft.entity.Entity getEntityFromUuid(UUID uuid);
    @Shadow public abstract PlayerChunkMap getPlayerChunkMap();
    @Shadow public abstract ChunkProviderServer getChunkProvider();
    @Shadow protected abstract void playerCheckLight();
    @Shadow protected abstract BlockPos adjustPosToNearbyEntity(BlockPos pos);
    @Shadow private boolean canAddEntity(net.minecraft.entity.Entity entityIn) {
        return false; // Shadowed
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldProvider;setWorld(Lnet/minecraft/world/World;)V"))
    public void onSetWorld(WorldProvider worldProvider, World worldIn) {
        // Guarantees no mod has changed our worldInfo.
        // Mods such as FuturePack replace worldInfo with a custom one for separate world time.
        // This change is not needed as all worlds in Sponge use separate save handlers.
        WorldInfo originalWorldInfo = worldIn.getWorldInfo();
        worldProvider.setWorld(worldIn);
        this.worldInfo = originalWorldInfo;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstruct(MinecraftServer server, ISaveHandler saveHandlerIn, WorldInfo info, int dimensionId, Profiler profilerIn, CallbackInfo callbackInfo) {
        this.worldInfo = info;
        this.dimensionId = dimensionId;
        this.prevWeather = getWeather();
        this.weatherStartTime = this.worldInfo.getWorldTotalTime();
        ((World) (Object) this).getWorldBorder().addListener(new PlayerBorderListener(this.getMinecraftServer(), dimensionId));
        PortalAgentType portalAgentType = ((WorldProperties) this.worldInfo).getPortalAgentType();
        if (!portalAgentType.equals(PortalAgentTypes.DEFAULT)) {
            try {
                this.worldTeleporter = (Teleporter) portalAgentType.getPortalAgentClass().getConstructor(new Class<?>[] {WorldServer.class})
                        .newInstance(new Object[] {this});
            } catch (Exception e) {
                SpongeImpl.getLogger().log(Level.ERROR, "Could not create PortalAgent of type " + portalAgentType.getId()
                        + " for world " + this.getName() + ": " + e.getMessage() + ". Falling back to default...");
            }
        }

        // Turn on capturing
        updateWorldGenerator();
        // Need to set the active config before we call it.
        this.chunkGCLoadThreshold = SpongeHooks.getActiveConfig((WorldServer) (Object) this).getConfig().getWorld().getChunkLoadThreadhold();
        this.chunkGCTickInterval = this.getActiveConfig().getConfig().getWorld().getTickInterval();
        this.weatherIceAndSnowEnabled = this.getActiveConfig().getConfig().getWorld().getWeatherIceAndSnow();
        this.weatherThunderEnabled = this.getActiveConfig().getConfig().getWorld().getWeatherThunder();
        this.updateEntityTick = 0;
    }

    @Redirect(method = "init", at = @At(value = "NEW", target = "net/minecraft/world/storage/MapStorage"))
    public MapStorage onCreateMapStorage(ISaveHandler saveHandler) {
        WorldServer overWorld = WorldManager.getWorldByDimensionId(0).orElse(null);
        // if overworld has loaded, use its mapstorage
        if (this.dimensionId != 0 && overWorld != null) {
            return overWorld.getMapStorage();
        }

        // if we are loading overworld, create a new mapstorage
        return new MapStorage(saveHandler);
    }

    @Inject(method = "createBonusChest", at = @At(value = "HEAD"))
    public void onCreateBonusChest(CallbackInfo ci) {
        if (CauseTracker.ENABLED) {
            CauseTracker.getInstance().switchToPhase(GenerationPhase.State.TERRAIN_GENERATION, PhaseContext.start()
                    .add(NamedCause.source(this))
                    .add(NamedCause.of(InternalNamedCauses.WorldGeneration.WORLD, this))
                    .addCaptures()
                    .complete());
        }
    }


    @Inject(method = "createBonusChest", at = @At(value = "RETURN"))
    public void onCreateBonusChestEnd(CallbackInfo ci) {
        if (CauseTracker.ENABLED) {
            CauseTracker.getInstance().completePhase(GenerationPhase.State.TERRAIN_GENERATION);
        }
    }

    @Inject(method = "createSpawnPosition(Lnet/minecraft/world/WorldSettings;)V", at = @At("HEAD"), cancellable = true)
    public void onCreateSpawnPosition(WorldSettings settings, CallbackInfo ci) {
        GeneratorType generatorType = (GeneratorType) settings.getTerrainType();

        // Allow bonus chest generation for non-Overworld worlds
        if (!this.provider.canRespawnHere() && this.getProperties().doesGenerateBonusChest()) {
            this.createBonusChest();
        }

        if ((generatorType != null && generatorType.equals(GeneratorTypes.THE_END)) || ((((WorldServer) (Object) this)).getChunkProvider().chunkGenerator instanceof ChunkProviderEnd)) {
            this.worldInfo.setSpawn(new BlockPos(100, 50, 0));
            ci.cancel();
        }
    }

    @Redirect(method = "createSpawnPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldSettings;isBonusChestEnabled()Z"))
    public boolean onIsBonusChestEnabled(WorldSettings settings) {
        return this.getProperties().doesGenerateBonusChest();
    }

    @Override
    public boolean isProcessingExplosion() {
        return this.processingExplosion;
    }

    @Override
    public boolean isMinecraftChunkLoaded(int x, int z, boolean allowEmpty) {
        return this.isChunkLoaded(x, z, allowEmpty);
    }

    @Override
    public SpongeConfig<WorldConfig> getWorldConfig() {
        return ((IMixinWorldInfo) this.worldInfo).getOrCreateWorldConfig();
    }


    @Override
    public SpongeConfig<?> getActiveConfig() {
        return this.activeConfig;
    }

    @Override
    public void setActiveConfig(SpongeConfig<?> config) {
        this.activeConfig = config;
        // update cached settings
        this.chunkGCLoadThreshold = this.activeConfig.getConfig().getWorld().getChunkLoadThreadhold();
        this.chunkGCTickInterval = this.activeConfig.getConfig().getWorld().getTickInterval();
        this.weatherIceAndSnowEnabled = this.activeConfig.getConfig().getWorld().getWeatherIceAndSnow();
        this.weatherThunderEnabled = this.activeConfig.getConfig().getWorld().getWeatherThunder();
        this.chunkUnloadDelay = this.activeConfig.getConfig().getWorld().getChunkUnloadDelay() * 1000;
        if (this.getChunkProvider() != null) {
            final IMixinChunkProviderServer mixinChunkProvider = (IMixinChunkProviderServer) this.getChunkProvider();
            final int maxChunkUnloads = this.activeConfig.getConfig().getWorld().getMaxChunkUnloads();
            mixinChunkProvider.setMaxChunkUnloads(maxChunkUnloads < 1 ? 1 : maxChunkUnloads);
            mixinChunkProvider.setDenyChunkRequests(this.activeConfig.getConfig().getWorld().getDenyChunkRequests());
            for (net.minecraft.entity.Entity entity : this.loadedEntityList) {
                if (entity instanceof IModData_Activation) {
                    ((IModData_Activation) entity).requiresActivationCacheRefresh(true);
                }
                if (entity instanceof IModData_Collisions) {
                    ((IModData_Collisions) entity).requiresCollisionsCacheRefresh(true);
                }
            }
        }
    }

    @Override
    public boolean isLoaded() {
        return WorldManager.getWorldByDimensionId(getDimensionId()).isPresent();
    }

    @Override
    public Path getDirectory() {
        final File worldDirectory = this.saveHandler.getWorldDirectory();
        if (worldDirectory == null) {
            new PrettyPrinter(60).add("A Server World has a null save directory!").centre().hr()
                .add("%s : %s", "World Name", this.getName())
                .add("%s : %s", "Dimension", this.getProperties().getDimensionType())
                .add("Please report this to sponge developers so they may potentially fix this")
                .trace(System.err, SpongeImpl.getLogger(), Level.ERROR);
            return null;
        }
        return worldDirectory.toPath();
    }

    @Override
    public void updateWorldGenerator() {

        // Get the default generator for the world type
        DataMap generatorSettings = this.getProperties().getGeneratorSettings();

        SpongeWorldGenerator newGenerator = createWorldGenerator(generatorSettings);
        // If the base generator is an IChunkProvider which implements
        // IPopulatorProvider we request that it add its populators not covered
        // by the base generation populator
        if (newGenerator.getBaseGenerationPopulator() instanceof IChunkGenerator) {
            // We check here to ensure that the IPopulatorProvider is one of our mixed in ones and not
            // from a mod chunk provider extending a provider that we mixed into
            if (WorldGenConstants.isValid((IChunkGenerator) newGenerator.getBaseGenerationPopulator(), IPopulatorProvider.class)) {
                ((IPopulatorProvider) newGenerator.getBaseGenerationPopulator()).addPopulators(newGenerator);
            }
        } else if (newGenerator.getBaseGenerationPopulator() instanceof IPopulatorProvider) {
            // If its not a chunk provider but is a populator provider then we call it as well
            ((IPopulatorProvider) newGenerator.getBaseGenerationPopulator()).addPopulators(newGenerator);
        }

        for (WorldGeneratorModifier modifier : this.getProperties().getGeneratorModifiers()) {
            modifier.modifyWorldGenerator(this.getProperties(), generatorSettings, newGenerator);
        }

        this.spongegen = createChunkGenerator(newGenerator);
        this.spongegen.setGenerationPopulators(newGenerator.getGenerationPopulators());
        this.spongegen.setPopulators(newGenerator.getPopulators());
        this.spongegen.setBiomeOverrides(newGenerator.getBiomeSettings());

        ChunkProviderServer chunkProviderServer = this.getChunkProvider();
        chunkProviderServer.chunkGenerator = this.spongegen;
    }

    @Override
    public SpongeChunkGenerator createChunkGenerator(SpongeWorldGenerator newGenerator) {
        return new SpongeChunkGenerator((net.minecraft.world.World) (Object) this, newGenerator.getBaseGenerationPopulator(),
                newGenerator.getBiomeGenerator());
    }

    @Override
    public SpongeWorldGenerator createWorldGenerator(DataMap settings) {
        // Minecraft uses a string for world generator settings
        // This string can be a JSON string, or be a string of a custom format

        // Try to convert to custom format
        Optional<String> optCustomSettings = settings.getString(DataQueries.WORLD_CUSTOM_SETTINGS);
        if (optCustomSettings.isPresent()) {
            return this.createWorldGenerator(optCustomSettings.get());
        }

        String jsonSettings = "";
        try {
            jsonSettings = DataFormats.JSON.write(settings);
        } catch (Exception e) {
            SpongeImpl.getLogger().warn("Failed to convert settings from [{}] for GeneratorType [{}] used by World [{}].", settings,
                    ((net.minecraft.world.World) (Object) this).getWorldType(), this, e);
        }

        return this.createWorldGenerator(jsonSettings);
    }

    @Override
    public SpongeWorldGenerator createWorldGenerator(String settings) {
        final WorldServer worldServer = (WorldServer) (Object) this;
        final WorldType worldType = worldServer.getWorldType();
        final IChunkGenerator chunkGenerator;
        final BiomeProvider biomeProvider;
        if (worldType instanceof SpongeWorldType) {
            chunkGenerator = ((SpongeWorldType) worldType).getChunkGenerator(worldServer, settings);
            biomeProvider = ((SpongeWorldType) worldType).getBiomeProvider(worldServer);
        } else {
            final IChunkGenerator currentGenerator = this.getChunkProvider().chunkGenerator;
            if (currentGenerator != null) {
                chunkGenerator = currentGenerator;
            } else {
                final WorldProvider worldProvider = worldServer.provider;
                ((IMixinWorldProvider) worldProvider).setGeneratorSettings(settings);
                chunkGenerator = worldProvider.createChunkGenerator();
            }
            biomeProvider = worldServer.provider.biomeProvider;
        }
        return new SpongeWorldGenerator(worldServer, (BiomeGenerator) biomeProvider, SpongeGenerationPopulator.of(worldServer, chunkGenerator));
    }

    @Override
    public WorldGenerator getWorldGenerator() {
        return this.spongegen;
    }

    @Override
    public WorldServer asMinecraftWorld() {
        return (WorldServer) (Object) this;
    }

    @Override
    public org.spongepowered.api.world.World asSpongeWorld() {
        return this;
    }

    /**
     * @author blood - July 1st, 2016
     * @author gabizou - July 1st, 2016 - Update to 1.10 and cause tracking
     *
     * @reason Added chunk and block tick optimizations, timings, cause tracking, and pre-construction events.
     */
    @Override
    @Overwrite
    protected void updateBlocks() {
        this.playerCheckLight();

        if (this.worldInfo.getTerrainType() == WorldType.DEBUG_WORLD)
        {
            Iterator<net.minecraft.world.chunk.Chunk> iterator1 = this.playerChunkMap.getChunkIterator();

            while (iterator1.hasNext())
            {
                iterator1.next().onTick(false);
            }
            return; // Sponge: Add return
        }
        // else // Sponge - Remove unnecessary else
        // { //

        int i = this.shadow$getGameRules().getInt("randomTickSpeed");
        boolean flag = this.isRaining();
        boolean flag1 = this.isThundering();
        // this.profiler.startSection("pollingChunks"); // Sponge - Don't use the profiler

        final CauseTracker causeTracker = CauseTracker.getInstance(); // Sponge - get the cause tracker

        // Sponge: Use SpongeImplHooks for Forge
        for (Iterator<net.minecraft.world.chunk.Chunk> iterator =
             SpongeImplHooks.getChunkIterator((WorldServer) (Object) this); iterator.hasNext(); ) // this.profiler.endSection()) // Sponge - don't use the profiler
        {
            // this.profiler.startSection("getChunk"); // Sponge - Don't use the profiler
            net.minecraft.world.chunk.Chunk chunk = iterator.next();
            int j = chunk.xPosition * 16;
            int k = chunk.zPosition * 16;
            // this.profiler.endStartSection("checkNextLight"); // Sponge - Don't use the profiler
            this.timings.updateBlocksCheckNextLight.startTiming(); // Sponge - Timings
            chunk.enqueueRelightChecks();
            this.timings.updateBlocksCheckNextLight.stopTiming(); // Sponge - Timings
            // this.profiler.endStartSection("tickChunk"); // Sponge - Don't use the profiler
            this.timings.updateBlocksChunkTick.startTiming(); // Sponge - Timings
            chunk.onTick(false);
            this.timings.updateBlocksChunkTick.stopTiming(); // Sponge - Timings
            // Sponge start - if surrounding neighbors are not loaded, skip
            if (!((IMixinChunk) chunk).areNeighborsLoaded()) {
                continue;
            }
            // Sponge end
            // this.profiler.endStartSection("thunder"); // Sponge - Don't use the profiler
            // Sponge start
            this.timings.updateBlocksThunder.startTiming();

            //if (this.provider.canDoLightning(chunk) && flag && flag1 && this.rand.nextInt(100000) == 0) // Sponge - Add SpongeImplHooks for forge
            if (this.weatherThunderEnabled && SpongeImplHooks.canDoLightning(this.provider, chunk) && flag && flag1 && this.rand.nextInt(100000) == 0)
            {
                if (CauseTracker.ENABLED) {
                    causeTracker.switchToPhase(TickPhase.Tick.WEATHER, PhaseContext.start()
                            .addCaptures()
                            .add(NamedCause.source(this))
                            .complete());
                }
                // Sponge end
                this.updateLCG = this.updateLCG * 3 + 1013904223;
                int l = this.updateLCG >> 2;
                BlockPos blockpos = this.adjustPosToNearbyEntity(new BlockPos(j + (l & 15), 0, k + (l >> 8 & 15)));

                if (this.isRainingAt(blockpos))
                {
                    DifficultyInstance difficultyinstance = this.getDifficultyForLocation(blockpos);

                    // Sponge - create a transform to be used for events
                    final Transform<org.spongepowered.api.world.World> transform = new Transform<>(this, VecHelper.toVector3d(blockpos).toDouble());

                    if (this.rand.nextDouble() < (double)difficultyinstance.getAdditionalDifficulty() * 0.05D)
                    {
                        // Sponge Start - Throw construction events
                        SpawnCause horseCause = WeatherSpawnCause.builder().weather(this.getWeather()).type(SpawnTypes.WEATHER).build();
                        ConstructEntityEvent.Pre constructEntityEvent = SpongeEventFactory.createConstructEntityEventPre(Cause.source(horseCause).build(), EntityTypes.HORSE, transform);
                        SpongeImpl.postEvent(constructEntityEvent);
                        if (!constructEntityEvent.isCancelled()) {
                            // Sponge End
                            EntitySkeletonHorse entityhorse = new EntitySkeletonHorse((WorldServer) (Object) this);
                            entityhorse.setTrap(true);
                            entityhorse.setGrowingAge(0);
                            entityhorse.setPosition((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ());
                            this.spawnEntity(entityhorse);
                            // Sponge Start - Throw a construct event for the lightning
                        }

                        SpawnCause lightiningCause = WeatherSpawnCause.builder().weather(this.getWeather()).type(SpawnTypes.WEATHER).build();
                        ConstructEntityEvent.Pre lightning = SpongeEventFactory.createConstructEntityEventPre(Cause.source(lightiningCause).build(), EntityTypes.LIGHTNING, transform);
                        SpongeImpl.postEvent(lightning);
                        if (!lightning.isCancelled()) {
                            // Sponge End
                            this.addWeatherEffect(new EntityLightningBolt((WorldServer) (Object) this, (double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), true));
                        } // Sponge - Brackets.
                    }
                    else
                    {
                        // Sponge start - Throw construction event for lightningbolts

                        SpawnCause cause = WeatherSpawnCause.builder().weather(this.getWeather()).type(SpawnTypes.WEATHER).build();
                        ConstructEntityEvent.Pre event = SpongeEventFactory.createConstructEntityEventPre(Cause.of(NamedCause.source(cause)),
                                EntityTypes.LIGHTNING, transform);
                        SpongeImpl.postEvent(event);
                        if (!event.isCancelled()) {
                            // Sponge End
                            this.addWeatherEffect(new EntityLightningBolt((WorldServer) (Object) this, (double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), false));
                        } // Sponge - Brackets.
                    }
                }
                // Sponge Start - Cause tracker unwind
                if (CauseTracker.ENABLED) {
                    causeTracker.completePhase(TickPhase.Tick.WEATHER);
                }
                // Sponge End

            }

            this.timings.updateBlocksThunder.stopTiming(); // Sponge - Stop thunder timing
            this.timings.updateBlocksIceAndSnow.startTiming(); // Sponge - Start thunder timing
            // this.profiler.endStartSection("iceandsnow"); // Sponge - don't use the profiler

            // if (this.rand.nextInt(16) == 0) // Sponge - Rewrite to use our boolean, and forge hook
            if (this.weatherIceAndSnowEnabled && SpongeImplHooks.canDoRainSnowIce(this.provider, chunk) && this.rand.nextInt(16) == 0)
            {
                // Sponge Start - Enter weather phase for snow and ice and flooding.
                causeTracker.switchToPhase(TickPhase.Tick.WEATHER, PhaseContext.start()
                        .addCaptures()
                        .add(NamedCause.source(this))
                        .complete());
                // Sponge End
                this.updateLCG = this.updateLCG * 3 + 1013904223;
                int j2 = this.updateLCG >> 2;
                BlockPos blockpos1 = this.getPrecipitationHeight(new BlockPos(j + (j2 & 15), 0, k + (j2 >> 8 & 15)));
                BlockPos blockpos2 = blockpos1.down();

                if (this.canBlockFreezeNoWater(blockpos2))
                {
                    this.setBlockState(blockpos2, Blocks.ICE.getDefaultState());
                }

                if (flag && this.canSnowAt(blockpos1, true))
                {
                    this.setBlockState(blockpos1, Blocks.SNOW_LAYER.getDefaultState());
                }

                if (flag && this.getBiome(blockpos2).canRain())
                {
                    this.getBlockState(blockpos2).getBlock().fillWithRain((WorldServer) (Object) this, blockpos2);
                }
                causeTracker.completePhase(TickPhase.Tick.WEATHER); // Sponge - complete weather phase
            }

            this.timings.updateBlocksIceAndSnow.stopTiming(); // Sponge - Stop ice and snow timing
            this.timings.updateBlocksRandomTick.startTiming(); // Sponge - Start random block tick timing
            // this.profiler.endStartSection("tickBlocks"); // Sponge - Don't use the profiler

            if (i > 0)
            {
                for (ExtendedBlockStorage extendedblockstorage : chunk.getBlockStorageArray())
                {
                    if (extendedblockstorage != net.minecraft.world.chunk.Chunk.NULL_BLOCK_STORAGE && extendedblockstorage.getNeedsRandomTick())
                    {
                        for (int i1 = 0; i1 < i; ++i1)
                        {
                            this.updateLCG = this.updateLCG * 3 + 1013904223;
                            int j1 = this.updateLCG >> 2;
                            int k1 = j1 & 15;
                            int l1 = j1 >> 8 & 15;
                            int i2 = j1 >> 16 & 15;
                            IBlockState iblockstate = extendedblockstorage.get(k1, i2, l1);
                            Block block = iblockstate.getBlock();
                            // this.profiler.startSection("randomTick"); // Sponge - Don't use the profiler

                            if (block.getTickRandomly())
                            {
                                // Sponge start - capture random tick
                                // Remove the random tick for cause tracking
                                // block.randomTick(this, new BlockPos(k1 + j, i2 + extendedblockstorage.getYLocation(), l1 + k), iblockstate, this.rand);

                                BlockPos pos = new BlockPos(k1 + j, i2 + extendedblockstorage.getYLocation(), l1 + k);
                                IMixinBlock spongeBlock = (IMixinBlock) block;
                                spongeBlock.getTimingsHandler().startTiming();
                                final PhaseData currentTuple = causeTracker.getCurrentPhaseData();
                                final IPhaseState phaseState = currentTuple.state;
                                if (!CauseTracker.ENABLED || phaseState.getPhase().alreadyCapturingBlockTicks(phaseState, currentTuple.context)) {
                                    block.randomTick((WorldServer) (Object) this, pos, iblockstate, this.rand);
                                } else {
                                    TrackingUtil.randomTickBlock(causeTracker, this, block, pos, iblockstate, this.rand);
                                }
                                spongeBlock.getTimingsHandler().stopTiming();
                                // Sponge end
                            }

                            // this.profiler.endSection(); // Sponge - Don't use the profiler
                        }
                    }
                }
            }
        }

        this.timings.updateBlocksRandomTick.stopTiming(); // Sponge - Stop random block timing
        // this.profiler.endSection(); // Sponge - Don't use the profiler
        // } // Sponge- Remove unecessary else
    }

    @Redirect(method = "updateBlockTick", at = @At(value = "INVOKE", target= "Lnet/minecraft/world/WorldServer;isAreaLoaded(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)Z"))
    public boolean onBlockTickIsAreaLoaded(WorldServer worldIn, BlockPos fromPos, BlockPos toPos) {
        int posX = fromPos.getX() + 8;
        int posZ = fromPos.getZ() + 8;
        // Forge passes the same block position for forced chunks
        if (fromPos.equals(toPos)) {
            posX = fromPos.getX();
            posZ = fromPos.getZ();
        }
        final net.minecraft.world.chunk.Chunk chunk = ((IMixinChunkProviderServer) this.getChunkProvider()).getLoadedChunkWithoutMarkingActive(posX >> 4, posZ >> 4);
        if (chunk == null || !((IMixinChunk) chunk).areNeighborsLoaded()) {
            return false;
        }

        return true;
    }

    /**
     * @author blood - August 30th, 2016
     *
     * @reason Always allow entity cleanup to occur. This prevents issues such as a plugin
     *         generating chunks with no players causing entities not getting cleaned up.
     */
    @Override
    @Overwrite
    public void updateEntities() {
        // Sponge start
        /*
        if (this.playerEntities.isEmpty()) {
            if (this.updateEntityTick++ >= 300) {
                return;
            }
        } else {
            this.resetUpdateEntityTick();
        }*/
        // Sponge end

        if (CauseTracker.ENABLED) {
            TrackingUtil.tickWorldProvider(this);
        } else {
            this.provider.onWorldUpdateEntities();
        }
        super.updateEntities();
    }

    @Redirect(method = "updateBlockTick", at = @At(value = "INVOKE", target="Lnet/minecraft/block/Block;updateTick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V"))
    public void onUpdateBlockTick(Block block, net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, Random rand) {
        this.onUpdateTick(block, worldIn, pos, state, rand);
    }

    // This ticks pending updates to blocks, Requires mixin for NextTickListEntry so we use the correct tracking
    @Redirect(method = "tickUpdates", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;updateTick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V"))
    public void onUpdateTick(Block block, net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, Random rand) {
        final CauseTracker causeTracker = CauseTracker.getInstance();
        final PhaseData phaseData = causeTracker.getCurrentPhaseData();
        final IPhaseState phaseState = phaseData.state;
        if (phaseState.getPhase().alreadyCapturingBlockTicks(phaseState, phaseData.context) || phaseState.getPhase().ignoresBlockUpdateTick(phaseData)) {
            block.updateTick(worldIn, pos, state, rand);
            return;
        }

        IMixinBlock spongeBlock = (IMixinBlock) block;
        spongeBlock.getTimingsHandler().startTiming();
        TrackingUtil.updateTickBlock(this, block, pos, state, rand);
        spongeBlock.getTimingsHandler().stopTiming();
    }

    @Redirect(method = "tickUpdates", at = @At(value = "INVOKE", target = "Lnet/minecraft/crash/CrashReportCategory;addBlockInfo(Lnet/minecraft/crash/CrashReportCategory;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)V"))
    private void onBlockInfo(CrashReportCategory category, BlockPos pos, IBlockState state) {
        try {
            CrashReportCategory.addBlockInfo(category, pos, state);
        } catch (NoClassDefFoundError e) {
            SpongeImpl.getLogger().error("An error occurred while adding crash report info!", e);
            SpongeImpl.getLogger().error("Original caught error:", category.crashReport.cause);
            throw new ReportedException(category.crashReport);
        }

    }

    @Redirect(method = "addBlockEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer$ServerBlockEventList;add(Ljava/lang/Object;)Z", remap = false))
    public boolean onAddBlockEvent(WorldServer.ServerBlockEventList list, Object obj, BlockPos pos, Block blockIn, int eventId, int eventParam) {
        final BlockEventData blockEventData = (BlockEventData) obj;
        IMixinBlockEventData blockEvent = (IMixinBlockEventData) blockEventData;
        // We fire a Pre event to make sure our captures do not get stuck in a loop.
        // This is very common with pistons as they add block events while blocks are being notified.
        if (blockIn instanceof BlockPistonBase) {
            // We only fire pre events for pistons
            if (SpongeCommonEventFactory.handlePistonEvent(this, list, obj, pos, blockIn, eventId, eventParam)) {
                return false;
            }

            blockEvent.setCaptureBlocks(false);
        } else if (SpongeCommonEventFactory.callChangeBlockEventPre(this, pos, NamedCause.of(NamedCause.BLOCK_EVENT, this)).isCancelled()) {
            return false;
        }

        if (CauseTracker.ENABLED) {
            final CauseTracker causeTracker = CauseTracker.getInstance();
            final PhaseData currentPhase = causeTracker.getCurrentPhaseData();
            final IPhaseState phaseState = currentPhase.state;
            if (phaseState.getPhase().ignoresBlockEvent(phaseState)) {
                return list.add((BlockEventData) obj);
            }
            final PhaseContext context = currentPhase.context;

            final LocatableBlock locatable = LocatableBlock.builder()
                    .location(new Location<>(this, pos.getX(), pos.getY(), pos.getZ()))
                    .state(this.getBlock(pos.getX(), pos.getY(), pos.getZ()))
                    .build();

            blockEvent.setTickBlock(locatable);
            phaseState.getPhase().addNotifierToBlockEvent(phaseState, context, this, pos, blockEvent);
        }
        return list.add((BlockEventData) obj);
    }

    // special handling for Pistons since they use their own event system
    @Redirect(method = "sendQueuedBlockEvents", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/WorldServer;fireBlockEvent(Lnet/minecraft/block/BlockEventData;)Z"))
    public boolean onFireBlockEvent(net.minecraft.world.WorldServer worldIn, BlockEventData event) {
        if (!CauseTracker.ENABLED) {
            fireBlockEvent(event);
        }
        final CauseTracker causeTracker = CauseTracker.getInstance();
        final IPhaseState phaseState = causeTracker.getCurrentState();
        if (phaseState.getPhase().ignoresBlockEvent(phaseState)) {
            return fireBlockEvent(event);
        }
        return TrackingUtil.fireMinecraftBlockEvent(causeTracker, worldIn, event);
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/IChunkProvider;tick()Z"))
    public boolean onTicktick(IChunkProvider chunkProvider) {
        // chunk unloads are moved at end of server tick to avoid clashing with chunk GC
        if (this.chunkGCTickInterval > 0) {
            return false;
        }

        return chunkProvider.tick();
    }

    // Chunk GC
    @Override
    public void doChunkGC() {
        this.chunkGCTickCount++;

        ChunkProviderServer chunkProviderServer = this.getChunkProvider();
        int chunkLoadCount = this.getChunkProvider().getLoadedChunkCount();
        if (chunkLoadCount >= this.chunkGCLoadThreshold && this.chunkGCLoadThreshold > 0) {
            chunkLoadCount = 0;
        } else if (this.chunkGCTickCount >= this.chunkGCTickInterval && this.chunkGCTickInterval > 0) {
            this.chunkGCTickCount = 0;
        } else {
            return;
        }

        for (net.minecraft.world.chunk.Chunk chunk : chunkProviderServer.getLoadedChunks()) {
            IMixinChunk spongeChunk = (IMixinChunk) chunk;
            if (chunk.unloadQueued || spongeChunk.isPersistedChunk() || !this.provider.canDropChunk(chunk.xPosition, chunk.zPosition)) {
                continue;
            }

            // If a player is currently using the chunk, skip it
            if (((IMixinPlayerChunkMap) this.getPlayerChunkMap()).isChunkInUse(chunk.xPosition, chunk.zPosition)) {
                continue;
            }

            // If we reach this point the chunk leaked so queue for unload
            chunkProviderServer.queueUnload(chunk);
            SpongeHooks.logChunkGCQueueUnload(chunkProviderServer.world, chunk);
        }
    }

    @Override
    public boolean save() throws IOException {
        if (!getChunkProvider().canSave()) {
            return false;
        }

        // TODO: Expose flush parameter in SpongeAPI?
        try {
            WorldManager.saveWorld((WorldServer) (Object) this, true);
        } catch (MinecraftException e) {
            throw Throwables.propagate(e);
        }
        return true;
    }

    @Inject(method = "saveLevel", at = @At("HEAD"))
    public void onSaveLevel(CallbackInfo ci) {
        // Always call the provider's onWorldSave method as we do not use WorldServerMulti
        for (WorldServer worldServer : this.mcServer.worlds) {
            worldServer.provider.onWorldSave();
        }
    }

    @Redirect(method = "saveAllChunks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/ChunkProviderServer;canSave()Z"))
    public boolean canChunkProviderSave(ChunkProviderServer chunkProviderServer) {
        return chunkProviderServer.canSave() &&
                !Sponge.getEventManager().post(
                        SpongeEventFactory.createSaveWorldEventPre(Cause.of(NamedCause.source(SpongeImpl.getServer())), this));
    }

    @Inject(method = "saveAllChunks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/ChunkProviderServer;getLoadedChunks()Ljava/util/Collection;"), cancellable = true)
    public void onSaveAllChunks(boolean saveAllChunks, IProgressUpdate progressCallback, CallbackInfo ci) {
        Sponge.getEventManager().post(SpongeEventFactory.createSaveWorldEventPost(Cause.of(NamedCause.source(SpongeImpl.getServer())), this));
        // The chunk GC handles all queuing for chunk unloads so we cancel here to avoid it during a save.
        if (this.chunkGCTickInterval > 0) {
            ci.cancel();
        }
    }

    @Redirect(method = "sendQueuedBlockEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/DimensionType;getId()I"), expect = 0, require = 0)
    private int onGetDimensionIdForBlockEvents(DimensionType dimensionType) {
        return this.getDimensionId();
    }

    @Override
    public Collection<ScheduledBlockUpdate> getScheduledUpdates(int x, int y, int z) {
        BlockPos position = new BlockPos(x, y, z);
        ImmutableList.Builder<ScheduledBlockUpdate> builder = ImmutableList.builder();
        for (NextTickListEntry sbu : this.pendingTickListEntriesTreeSet) {
            if (sbu.position.equals(position)) {
                builder.add((ScheduledBlockUpdate) sbu);
            }
        }
        return builder.build();
    }

    @Nullable
    private NextTickListEntry tmpScheduledObj;

    /*@Redirect(method = "updateBlockTick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/NextTickListEntry;setPriority(I)V"))
    private void onUpdateScheduledBlock(NextTickListEntry sbu, int priority) {
        this.onCreateScheduledBlockUpdate(sbu, priority);
    }*/

    @Redirect(method = "updateBlockTick", // really scheduleUpdate
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/NextTickListEntry;setPriority(I)V"))
    private void onCreateScheduledBlockUpdate(NextTickListEntry sbu, int priority) {
        final CauseTracker causeTracker = CauseTracker.getInstance();
        final IPhaseState phaseState = causeTracker.getCurrentState();

        if (phaseState.getPhase().ignoresScheduledUpdates(phaseState)) {
            this.tmpScheduledObj = sbu;
            return;
        }

        sbu.setPriority(priority);
        ((IMixinNextTickListEntry) sbu).setWorld((WorldServer) (Object) this);
        if (!((WorldServer) (Object) this).isBlockLoaded(sbu.position)) {
            this.tmpScheduledObj = sbu;
            return;
        }

        this.tmpScheduledObj = sbu;
    }

    @Override
    public ScheduledBlockUpdate addScheduledUpdate(int x, int y, int z, int priority, int ticks) {
        BlockPos pos = new BlockPos(x, y, z);
        this.updateBlockTick(pos, getBlockState(pos).getBlock(), ticks, priority);
        ScheduledBlockUpdate sbu = (ScheduledBlockUpdate) this.tmpScheduledObj;
        this.tmpScheduledObj = null;
        return sbu;
    }

    @Override
    public void removeScheduledUpdate(int x, int y, int z, ScheduledBlockUpdate update) {
        // Note: Ignores position argument
        this.pendingTickListEntriesHashSet.remove(update);
        this.pendingTickListEntriesTreeSet.remove(update);
    }

    @Redirect(method = "updateAllPlayersSleepingFlag()V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/player/EntityPlayer;isSpectator()Z"))
    public boolean isSpectatorOrIgnored(EntityPlayer entityPlayer) {
        // spectators are excluded from the sleep tally in vanilla
        // this redirect expands that check to include sleep-ignored players as well
        boolean ignore = entityPlayer instanceof Player && ((Player)entityPlayer).isSleepingIgnored();
        return ignore || entityPlayer.isSpectator();
    }

    @Redirect(method = "areAllPlayersAsleep()Z", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/player/EntityPlayer;isPlayerFullyAsleep()Z"))
    public boolean isPlayerFullyAsleep(EntityPlayer entityPlayer) {
        // if isPlayerFullyAsleep() returns false areAllPlayerAsleep() breaks its loop and returns false
        // this redirect forces it to return true if the player is sleep-ignored even if they're not sleeping
        boolean ignore = entityPlayer instanceof Player && ((Player)entityPlayer).isSleepingIgnored();
        return ignore || entityPlayer.isPlayerFullyAsleep();
    }

    @Redirect(method = "areAllPlayersAsleep()Z", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/player/EntityPlayer;isSpectator()Z"))
    public boolean isSpectatorAndNotIgnored(EntityPlayer entityPlayer) {
        // if a player is marked as a spectator areAllPlayersAsleep() breaks its loop and returns false
        // this redirect forces it to return false if a player is sleep-ignored even if they're a spectator
        boolean ignore = entityPlayer instanceof Player && ((Player)entityPlayer).isSleepingIgnored();
        return !ignore && entityPlayer.isSpectator();
    }

    @Override
    public Optional<Entity> getEntity(UUID uuid) {
        return Optional.ofNullable((Entity) this.getEntityFromUuid(uuid));
    }

    @Override
    public boolean setBlock(int x, int y, int z, BlockState blockState, BlockChangeFlag flag, Cause cause) {
        checkBlockBounds(x, y, z);
        final CauseTracker causeTracker = CauseTracker.getInstance();
        final PhaseData peek = causeTracker.getCurrentPhaseData();
        boolean isWorldGen = CauseTracker.ENABLED && peek.state.getPhase().isWorldGeneration(peek.state);
        boolean handlesOwnCompletion = CauseTracker.ENABLED && peek.state.getPhase().handlesOwnPhaseCompletion(peek.state);
        if (!isWorldGen) {
            checkArgument(cause != null, "Cause cannot be null!");
            checkArgument(cause.root() instanceof PluginContainer, "PluginContainer must be at the ROOT of a cause!");
            checkArgument(flag != null, "BlockChangeFlag cannot be null!");
        }
        if (!isWorldGen && !handlesOwnCompletion) {
            final PhaseContext context = PhaseContext.start()
                    .add(NamedCause.of(InternalNamedCauses.General.PLUGIN_CAUSE, cause))
                    .addCaptures()
                    .add(NamedCause.of(InternalNamedCauses.General.BLOCK_CHANGE, flag))
                    .add(NamedCause.source(cause.root()));
            for (Map.Entry<String, Object> entry : cause.getNamedCauses().entrySet()) {
                context.add(NamedCause.of(entry.getKey(), entry.getValue()));
            }
            context.complete();
            causeTracker.switchToPhase(PluginPhase.State.BLOCK_WORKER, context);
        }
        if (handlesOwnCompletion) {
            peek.context.firstNamed(InternalNamedCauses.General.BLOCK_CHANGE, PhaseContext.CaptureFlag.class)
                    .ifPresent(captureFlag -> captureFlag.addFlag(flag));
        }
        final boolean state = setBlockState(new BlockPos(x, y, z), (IBlockState) blockState, flag);
        if (!isWorldGen && !handlesOwnCompletion) {
            causeTracker.completePhase(PluginPhase.State.BLOCK_WORKER);
        }
        return state;
    }

    private void checkBlockBounds(int x, int y, int z) {
        if (!containsBlock(x, y, z)) {
            throw new PositionOutOfBoundsException(new Vector3i(x, y, z), BLOCK_MIN, BLOCK_MAX);
        }
    }

    @Override
    public BlockSnapshot createSnapshot(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        IBlockState currentState = this.getBlockState(pos);
        return this.createSpongeBlockSnapshot(currentState, currentState.getActualState((WorldServer) (Object) this, pos), pos, 2);
    }

    @Override
    public boolean spawnEntities(Iterable<? extends Entity> entities, Cause cause) {
        checkArgument(cause != null, "Cause cannot be null!");
        checkArgument(cause.root() instanceof SpawnCause, "SpawnCause must be at the ROOT of a cause!");
        checkArgument(cause.containsType(PluginContainer.class), "PluginContainer must be within the cause!");
        List<Entity> entitiesToSpawn = new ArrayList<>();
        entities.forEach(entitiesToSpawn::add);
        final SpawnEntityEvent.Custom event = SpongeEventFactory.createSpawnEntityEventCustom(cause, entitiesToSpawn);
        SpongeImpl.postEvent(event);
        if (!event.isCancelled()) {
            for (Entity entity : event.getEntities()) {
                this.forceSpawnEntity(entity);
            }
        }
        return event.isCancelled();
    }

    /**
     * @author gabizou - April 24th, 2016
     * @reason Needs to redirect the dimension id for the packet being sent to players
     * so that the dimension is correctly adjusted
     *
     * @param id The world provider's dimension id
     * @return True if the spawn was successful and the effect is played.
     */
    // We expect 0 because forge patches it correctly
    @Redirect(method = "addWeatherEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/DimensionType;getId()I"), expect = 0, require = 0)
    public int getDimensionIdForWeatherEffect(DimensionType id) {
        return this.getDimensionId();
    }

    /**
     * @author gabizou - February 7th, 2016
     * @author gabizou - September 3rd, 2016 - Moved from MixinWorld since WorldServer overrides the method.
     *
     * This will short circuit all other patches such that we control the
     * entities being loaded by chunkloading and can throw our bulk entity
     * event. This will bypass Forge's hook for individual entity events,
     * but the SpongeModEventManager will still successfully throw the
     * appropriate event and cancel the entities otherwise contained.
     *
     * @param entities The entities being loaded
     * @param callbackInfo The callback info
     */
    @Final
    @Inject(method = "loadEntities", at = @At("HEAD"), cancellable = true)
    private void spongeLoadEntities(Collection<net.minecraft.entity.Entity> entities, CallbackInfo callbackInfo) {
        if (entities.isEmpty()) {
            // just return, no entities to load!
            callbackInfo.cancel();
            return;
        }
        List<Entity> entityList = new ArrayList<>();
        for (net.minecraft.entity.Entity entity : entities) {
            // Make sure no entities load in invalid positions
            if (((IMixinBlockPos) entity.getPosition()).isInvalidYPosition()) {
                entity.setDead();
                continue;
            }
            if (this.canAddEntity(entity)) {
                entityList.add((Entity) entity);
            }
        }
        SpawnCause cause = SpawnCause.builder().type(InternalSpawnTypes.CHUNK_LOAD).build();
        List<NamedCause> causes = new ArrayList<>();
        causes.add(NamedCause.source(cause));
        causes.add(NamedCause.of("World", this));
        SpawnEntityEvent.ChunkLoad chunkLoad = SpongeEventFactory.createSpawnEntityEventChunkLoad(Cause.of(causes), Lists.newArrayList(entityList));
        SpongeImpl.postEvent(chunkLoad);
        if (!chunkLoad.isCancelled() && chunkLoad.getEntities().size() > 0) {
            for (Entity successful : chunkLoad.getEntities()) {
                this.loadedEntityList.add((net.minecraft.entity.Entity) successful);
                this.onEntityAdded((net.minecraft.entity.Entity) successful);
            }
        }
        // Remove entities from chunk/world that were filtered in event
        // This prevents invisible entities from loading into the world and blocking the position.
        for (Entity entity : entityList) {
            if (!chunkLoad.getEntities().contains(entity)) {
                ((net.minecraft.world.World) (Object) this).removeEntityDangerously((net.minecraft.entity.Entity) entity);
            }
        }
        callbackInfo.cancel();
    }

    @Override
    public void triggerExplosion(org.spongepowered.api.world.explosion.Explosion explosion, Cause cause) {
        checkNotNull(explosion, "explosion");
        Location<org.spongepowered.api.world.World> origin = explosion.getLocation();
        checkNotNull(origin, "location");
        checkNotNull(cause, "Cause cannot be null!");
        checkArgument(cause.containsType(PluginContainer.class), "Cause must contain a PluginContainer!");
        final CauseTracker causeTracker = CauseTracker.getInstance();
        if (CauseTracker.ENABLED) {
            final PhaseContext phaseContext = PhaseContext.start()
                    .add(NamedCause.source(cause))
                    .explosion()
                    .addEntityCaptures()
                    .addEntityDropCaptures()
                    .addBlockCaptures();
            phaseContext.getCaptureExplosion().addExplosion(explosion);
            phaseContext.complete();
            causeTracker.switchToPhase(PluginPhase.State.CUSTOM_EXPLOSION, phaseContext);
        }
        final Explosion mcExplosion;
        try {
            // Since we already have the API created implementation Explosion, let's use it.
            mcExplosion = (Explosion) explosion;
        } catch (Exception e) {
            new PrettyPrinter(60).add("Explosion not compatible with this implementation").centre().hr()
                    .add("An explosion that was expected to be used for this implementation does not")
                    .add("originate from this implementation.")
                    .add(e)
                    .trace();
            return;
        }
        final double x = mcExplosion.explosionX;
        final double y = mcExplosion.explosionY;
        final double z = mcExplosion.explosionZ;
        final boolean isSmoking = mcExplosion.isSmoking;
        final float strength = explosion.getRadius();

        // Set up the pre event
        final ExplosionEvent.Pre event = SpongeEventFactory.createExplosionEventPre(cause, explosion, this);
        if (SpongeImpl.postEvent(event)) {
            this.processingExplosion = false;
            if (CauseTracker.ENABLED) {
                causeTracker.completePhase(PluginPhase.State.CUSTOM_EXPLOSION);
            }
            return;
        }
        // Sponge End

        mcExplosion.doExplosionA();
        mcExplosion.doExplosionB(false);

        if (!isSmoking) {
            mcExplosion.clearAffectedBlockPositions();
        }

        for (EntityPlayer entityplayer : this.playerEntities) {
            if (entityplayer.getDistanceSq(x, y, z) < 4096.0D) {
                ((EntityPlayerMP) entityplayer).connection.sendPacket(new SPacketExplosion(x, y, z, strength, mcExplosion.getAffectedBlockPositions(),
                        mcExplosion.getPlayerKnockbackMap().get(entityplayer)));
            }
        }

        // Sponge Start - end processing
        this.processingExplosion = false;
        if (CauseTracker.ENABLED) {
            causeTracker.completePhase(PluginPhase.State.CUSTOM_EXPLOSION);
        }
        // Sponge End
    }

    @Override
    public void triggerInternalExplosion(org.spongepowered.api.world.explosion.Explosion explosion) {
        checkNotNull(explosion, "explosion");
        Location<org.spongepowered.api.world.World> origin = explosion.getLocation();
        checkNotNull(origin, "location");
        newExplosion(EntityUtil.toNullableNative(explosion.getSourceExplosive().orElse(null)), origin.getX(),
                origin.getY(), origin.getZ(), explosion.getRadius(), explosion.canCauseFire(),
                explosion.shouldBreakBlocks()
        );
    }

    // ------------------------- Start Cause Tracking overrides of Minecraft World methods ----------

    /**
     * @author gabizou March 11, 2016
     *
     * The train of thought for how spawning is handled:
     * 1) This method is called in implementation
     * 2) handleVanillaSpawnEntity is called to associate various contextual SpawnCauses
     * 3) {@link CauseTracker#spawnEntity(Entity)} is called to check if the entity is to
     *    be "collected" or "captured" in the current {@link PhaseContext} of the current phase
     * 4) If the entity is forced or is captured, {@code true} is returned, otherwise, the entity is
     *    passed along normal spawning handling.
     */
    @Override
    public boolean spawnEntity(net.minecraft.entity.Entity entity) {
        return canAddEntity(entity) && CauseTracker.getInstance().spawnEntity(EntityUtil.fromNative(entity));
    }


    /**
     * @author gabizou, March 12th, 2016
     *
     * Move this into WorldServer as we should not be modifying the client world.
     *
     * Purpose: Rewritten to support capturing blocks
     */
    @Override
    public boolean setBlockState(BlockPos pos, IBlockState newState, int flags) {
        if (!this.isValid(pos)) {
            return false;
        } else if (this.worldInfo.getTerrainType() == WorldType.DEBUG_WORLD) { // isRemote is always false since this is WorldServer
            return false;
        } else {
            // Sponge - reroute to the CauseTracker
            return CauseTracker.getInstance().setBlockState(this, pos, newState, flags);
        }
    }

    @Override
    public boolean setBlockState(BlockPos pos, IBlockState state, BlockChangeFlag flag) {
        if (!this.isValid(pos)) {
            return false;
        } else if (this.worldInfo.getTerrainType() == WorldType.DEBUG_WORLD) { // isRemote is always false since this is WorldServer
            return false;
        } else {
            // Sponge - reroute to the CauseTracker
            return CauseTracker.getInstance().setBlockStateWithFlag(this, pos, state, flag);
        }
    }

    /**
     * @author gabizou - March 12th, 2016
     *
     * Technically an overwrite to properly track on *server* worlds.
     */
    @Override
    public void immediateBlockTick(BlockPos pos, IBlockState state, Random random) {
        this.scheduledUpdatesAreImmediate = true;
        // Sponge start - Cause tracking
        final PhaseData peek = CauseTracker.getInstance().getCurrentPhaseData();
        if (!CauseTracker.ENABLED || peek.state.getPhase().ignoresBlockUpdateTick(peek)) {
            state.getBlock().updateTick((WorldServer) (Object) this, pos, state, random);
            // THIS NEEDS TO BE SET BACK TO FALSE OR ELSE ALL HELL BREAKS LOOSE!
            // No seriously, if this is not set back to false, all future updates are processed immediately
            // and various things get caught under the Unwinding Phase.
            this.scheduledUpdatesAreImmediate = false;
            return;
        }
        TrackingUtil.updateTickBlock(this, state.getBlock(), pos, state, random);
        // Sponge end
        this.scheduledUpdatesAreImmediate = false;
    }

    /**
     * @author gabizou - March 12th, 2016
     *
     * Technically an overwrite to properly track on *server* worlds.
     */
    @Override
    public void neighborChanged(BlockPos pos, Block blockIn, BlockPos otherPos) { // notifyBlockOfStateChange
        CauseTracker.getInstance().notifyBlockOfStateChange(this, pos, blockIn, otherPos);
    }

    /**
     * @author gabizou - March 12th, 2016
     *
     * Technically an overwrite to properly track on *server* worlds.
     */
    @Override
    public void notifyNeighborsOfStateExcept(BlockPos pos, Block blockType, EnumFacing skipSide) {
        if (!isValid(pos)) {
            return;
        }

        EnumSet<EnumFacing> directions = EnumSet.copyOf(NOTIFY_DIRECTIONS);
        directions.remove(skipSide);
        final NotifyNeighborBlockEvent event = SpongeCommonEventFactory.callNotifyNeighborEvent(this, pos, directions);
        if (event == null || !event.isCancelled()) {
            final CauseTracker causeTracker = CauseTracker.getInstance();
            for (EnumFacing facing : EnumFacing.values()) {
                if (event != null) {
                    final Direction direction = DirectionFacingProvider.getInstance().getKey(facing).get();
                    if (!event.getNeighbors().keySet().contains(direction)) {
                        continue;
                    }
                }

                causeTracker.notifyBlockOfStateChange(this, pos.offset(facing), blockType, pos);
            }
        }
    }

    /**
     * @author gabizou - March 12th, 2016
     *
     * Technically an overwrite to properly track on *server* worlds.
     */
    @Override
    public void notifyNeighborsOfStateChange(BlockPos pos, Block blockType, boolean updateObserverBlocks) {
        if (!isValid(pos)) {
            return;
        }

        final NotifyNeighborBlockEvent event = SpongeCommonEventFactory.callNotifyNeighborEvent(this, pos, NOTIFY_DIRECTIONS);
        if (event == null || !event.isCancelled()) {
            final CauseTracker causeTracker = CauseTracker.getInstance();
            for (EnumFacing facing : EnumFacing.values()) {
                if (event != null) {
                    final Direction direction = DirectionFacingProvider.getInstance().getKey(facing).get();
                    if (!event.getNeighbors().keySet().contains(direction)) {
                        continue;
                    }
                }

                causeTracker.notifyBlockOfStateChange(this, pos.offset(facing), blockType, pos);
            }
        }

        // Copied over to ensure observers retain functionality.
        if (updateObserverBlocks) {
            this.updateObservingBlocksAt(pos, blockType);
        }
    }

    @SuppressWarnings("Duplicates")
    @Override
    protected void onUpdateWeatherEffect(net.minecraft.entity.Entity entityIn) {
        final CauseTracker causeTracker = CauseTracker.getInstance();
        final IPhaseState state = causeTracker.getCurrentState();
        if (!CauseTracker.ENABLED || state.getPhase().alreadyCapturingEntityTicks(state)) {
            entityIn.onUpdate();
            return;
        }
        TrackingUtil.tickEntity(entityIn);
        updateRotation(entityIn);
    }

    @Override
    protected void onUpdateTileEntities(ITickable tile) {
        this.updateTileEntity(tile);
    }

    // separated from onUpdateEntities for TileEntityActivation mixin
    private void updateTileEntity(ITickable tile) {
        final CauseTracker causeTracker = CauseTracker.getInstance();
        final IPhaseState state = causeTracker.getCurrentState();

        if (!CauseTracker.ENABLED || state.getPhase().alreadyCapturingTileTicks(state)) {
            tile.update();
            return;
        }

        TrackingUtil.tickTileEntity(this, tile);
    }

    @Override
    protected void onCallEntityUpdate(net.minecraft.entity.Entity entity) {
        final CauseTracker causeTracker = CauseTracker.getInstance();
        final IPhaseState state = causeTracker.getCurrentState();
        if (!CauseTracker.ENABLED || state.getPhase().alreadyCapturingEntityTicks(state)) {
            entity.onUpdate();
            return;
        }

        TrackingUtil.tickEntity(entity);
        updateRotation(entity);
    }

    @Override
    protected void onCallEntityRidingUpdate(net.minecraft.entity.Entity entity) {
        final CauseTracker causeTracker = CauseTracker.getInstance();
        final IPhaseState state = causeTracker.getCurrentState();
        if (!CauseTracker.ENABLED || state.getPhase().alreadyCapturingEntityTicks(state)) {
            entity.updateRidden();
            return;
        }

        TrackingUtil.tickRidingEntity(entity);
        updateRotation(entity);
    }

    @Redirect(method = "wakeAllPlayers", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;wakeUpPlayer(ZZZ)V"))
    private void spongeWakeUpPlayer(EntityPlayer player, boolean immediately, boolean updateWorldFlag, boolean setSpawn) {
        if (CauseTracker.ENABLED) {
            CauseTracker.getInstance().switchToPhase(EntityPhase.State.PLAYER_WAKE_UP, PhaseContext.start()
                    .add(NamedCause.source(player))
                    .addCaptures()
                    .complete()
            );
        }
        player.wakeUpPlayer(immediately, updateWorldFlag, setSpawn);
        if (CauseTracker.ENABLED) {
            CauseTracker.getInstance().completePhase(EntityPhase.State.PLAYER_WAKE_UP);
        }
    }

    // ------------------------ End of Cause Tracking ------------------------------------

    // IMixinWorld method
    @Override
    public void spongeNotifyNeighborsPostBlockChange(BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {
        if ((flags & 1) != 0) {
            this.notifyNeighborsRespectDebug(pos, newState.getBlock(), true);

            if (newState.hasComparatorInputOverride()) {
                this.updateComparatorOutputLevel(pos, newState.getBlock());
            }
        }
    }

    // IMixinWorld method
    @Override
    public void addEntityRotationUpdate(net.minecraft.entity.Entity entity, Vector3d rotation) {
        this.rotationUpdates.put(entity, rotation);
    }

    // IMixinWorld method
    @Override
    public void updateRotation(net.minecraft.entity.Entity entityIn) {
        Vector3d rotationUpdate = this.rotationUpdates.get(entityIn);
        if (rotationUpdate != null) {
            entityIn.rotationPitch = (float) rotationUpdate.getX();
            entityIn.rotationYaw = (float) rotationUpdate.getY();
        }
        this.rotationUpdates.remove(entityIn);
    }

    @Override
    public void onSpongeEntityAdded(net.minecraft.entity.Entity entity) {
        this.onEntityAdded(entity);
        ((IMixinEntity) entity).onJoinWorld();
    }

    @Override
    public void onSpongeEntityRemoved(net.minecraft.entity.Entity entity) {
        this.onEntityRemoved(entity);
    }

    @Override
    public boolean spawnEntity(Entity entity, Cause cause) {
        final CauseTracker causeTracker = CauseTracker.getInstance();
        final IPhaseState state = causeTracker.getCurrentState();
        if (CauseTracker.ENABLED && !state.getPhase().alreadyCapturingEntitySpawns(state)) {
            causeTracker.switchToPhase(PluginPhase.State.CUSTOM_SPAWN, PhaseContext.start()
                .add(NamedCause.source(cause))
                .addCaptures()
                .complete());
            causeTracker.spawnEntityWithCause(entity, cause);
            causeTracker.completePhase(PluginPhase.State.CUSTOM_SPAWN);
            return true;
        }
        return causeTracker.spawnEntityWithCause(entity, cause);
    }

    @Override
    public boolean forceSpawnEntity(Entity entity) {
        final net.minecraft.entity.Entity minecraftEntity = (net.minecraft.entity.Entity) entity;
        final int x = minecraftEntity.getPosition().getX();
        final int z = minecraftEntity.getPosition().getZ();
        return forceSpawnEntity(minecraftEntity, x >> 4, z >> 4);
    }

    private boolean forceSpawnEntity(net.minecraft.entity.Entity entity, int chunkX, int chunkZ) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer entityplayer = (EntityPlayer) entity;
            this.playerEntities.add(entityplayer);
            this.updateAllPlayersSleepingFlag();
        }

        if (entity instanceof EntityLightningBolt) {
            this.addWeatherEffect(entity);
            return true;
        }

        this.getChunkFromChunkCoords(chunkX, chunkZ).addEntity(entity);
        this.loadedEntityList.add(entity);
        this.onSpongeEntityAdded(entity);
        return true;
    }


    @Override
    public SpongeBlockSnapshot createSpongeBlockSnapshot(IBlockState state, IBlockState extended, BlockPos pos, int updateFlag) {
        this.builder.reset();
        this.builder.blockState((BlockState) state)
                .extendedState((BlockState) extended)
                .worldId(this.getUniqueId())
                .position(VecHelper.toVector3i(pos));
        Optional<UUID> creator = getCreator(pos.getX(), pos.getY(), pos.getZ());
        Optional<UUID> notifier = getNotifier(pos.getX(), pos.getY(), pos.getZ());
        if (creator.isPresent()) {
            this.builder.creator(creator.get());
        }
        if (notifier.isPresent()) {
            this.builder.notifier(notifier.get());
        }
        if (state.getBlock() instanceof ITileEntityProvider) {
            net.minecraft.tileentity.TileEntity te = getTileEntity(pos);
            if (te != null) {
                TileEntity tile = (TileEntity) te;
                for (DataManipulator<?, ?> manipulator : tile.getContainers()) {
                    this.builder.add(manipulator);
                }
                NBTTagCompound nbt = new NBTTagCompound();
                // Some mods like OpenComputers assert if attempting to save robot while moving
                try {
                    te.writeToNBT(nbt);
                    this.builder.unsafeNbt(nbt);
                }
                catch(Throwable t) {
                    // ignore
                }
            }
        }
        return new SpongeBlockSnapshot(this.builder, BlockChangeFlag.ALL.setUpdateNeighbors((updateFlag & 1) != 0), updateFlag);
    }

    /**
     * @author gabizou - September 10th, 2016
     * @reason Due to the amount of changes, and to ensure that Forge's events are being properly
     * thrown, we must overwrite to have our hooks in place where we need them to be and when.
     *
     * @param entityIn The entity that caused the explosion
     * @param x The x position
     * @param y The y position
     * @param z The z position
     * @param strength The strength of the explosion, determines what blocks can be destroyed
     * @param isFlaming Whether fire will be caused from the explosion
     * @param isSmoking Whether blocks will break
     * @return The explosion
     */
    @Overwrite
    @Override
    public Explosion newExplosion(@Nullable net.minecraft.entity.Entity entityIn, double x, double y, double z, float strength, boolean isFlaming,
            boolean isSmoking) {
        // Sponge Start - Cause tracking
        this.processingExplosion = true;
        if (CauseTracker.ENABLED) {
            PhaseContext phaseContext = PhaseContext.start()
                    .explosion()
                    .addEntityCaptures()
                    .addEntityDropCaptures()
                    .addBlockCaptures();
            if (entityIn != null) {
                phaseContext.add(NamedCause.source(entityIn));
            } else {
                phaseContext.add(NamedCause.source(this));
            }
            final PhaseData currentPhaseData = CauseTracker.getInstance().getCurrentPhaseData();
            currentPhaseData.state.getPhase().appendContextPreExplosion(phaseContext, currentPhaseData);
            phaseContext.complete();
            CauseTracker.getInstance().switchToPhase(GeneralPhase.State.EXPLOSION, phaseContext);
        }
        // Sponge End

        Explosion explosion = new Explosion((WorldServer) (Object) this, entityIn, x, y, z, strength, isFlaming, isSmoking);

        // Sponge Start - More cause tracking
        if (CauseTracker.ENABLED) {
            try {
                CauseTracker.getInstance().getCurrentContext().getCaptureExplosion().addExplosion(((org.spongepowered.api.world.explosion.Explosion) explosion));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Set up the pre event
        final ExplosionEvent.Pre event = SpongeEventFactory.createExplosionEventPre(((IMixinExplosion) explosion).createCause(),
                (org.spongepowered.api.world.explosion.Explosion) explosion, this);
        if (SpongeImpl.postEvent(event)) {
            this.processingExplosion = false;
            if (CauseTracker.ENABLED) {
                CauseTracker.getInstance().completePhase(GeneralPhase.State.EXPLOSION);
            }
            return explosion;
        }
        // Sponge End

        explosion.doExplosionA();
        explosion.doExplosionB(false);

        if (!isSmoking) {
            explosion.clearAffectedBlockPositions();
        }

        for (EntityPlayer entityplayer : this.playerEntities) {
            if (entityplayer.getDistanceSq(x, y, z) < 4096.0D) {
                ((EntityPlayerMP) entityplayer).connection.sendPacket(new SPacketExplosion(x, y, z, strength, explosion.getAffectedBlockPositions(),
                        explosion.getPlayerKnockbackMap().get(entityplayer)));
            }
        }

        // Sponge Start - end processing
        this.processingExplosion = false;
        if (CauseTracker.ENABLED) {
            CauseTracker.getInstance().completePhase(GeneralPhase.State.EXPLOSION);
        }
        // Sponge End
        return explosion;
    }

    /**
     * @author gabizou - August 4th, 2016
     * @reason Overrides the same method from MixinWorld_Lighting that redirects
     * {@link #isAreaLoaded(BlockPos, int, boolean)} to simplify the check to
     * whether the chunk's neighbors are loaded. Since the passed radius is always
     * 17, the check is simply checking for whether neighboring chunks are loaded
     * properly.
     *
     * @param thisWorld This world
     * @param pos The block position to check light for
     * @param radius The radius, always 17
     * @param allowEmtpy Whether to allow empty chunks, always false
     * @param lightType The light type
     * @param samePosition The block position to check light for, again.
     * @return True if the chunk is loaded and neighbors are loaded
     */
    @Override
    protected boolean spongeIsAreaLoadedForCheckingLight(World thisWorld, BlockPos pos, int radius, boolean allowEmtpy, EnumSkyBlock lightType,
            BlockPos samePosition) {
        final Chunk chunk = ((IMixinChunkProviderServer) this.chunkProvider).getLoadedChunkWithoutMarkingActive(pos.getX() >> 4, pos.getZ() >> 4);
        return !(chunk == null || !((IMixinChunk) chunk).areNeighborsLoaded());
    }

    /**
     * @author gabizou - April 8th, 2016
     *
     * Instead of providing chunks which has potential chunk loads,
     * we simply get the chunk directly from the chunk provider, if it is loaded
     * and return the light value accordingly.
     *
     * @param pos The block position
     * @return The light at the desired block position
     */
    @Override
    public int getLight(BlockPos pos) {
        if (pos.getY() < 0) {
            return 0;
        } else {
            if (pos.getY() >= 256) {
                pos = new BlockPos(pos.getX(), 255, pos.getZ());
            }
            // Sponge Start - Use our hook to get the chunk only if it is loaded
            // return this.getChunkFromBlockCoords(pos).getLightSubtracted(pos, 0);
            final Chunk chunk = ((IMixinChunkProviderServer) this.chunkProvider).getLoadedChunkWithoutMarkingActive(pos.getX() >> 4, pos.getZ() >> 4);
            return chunk == null ? 0 : chunk.getLightSubtracted(pos, 0);
            // Sponge End
        }
    }

    /**
     * @author gabizou - April 8th, 2016
     *
     * @reason Rewrites the chunk accessor to get only a chunk if it is loaded.
     * This avoids loading chunks from file or generating new chunks
     * if the chunk didn't exist, when the only function of this method is
     * to get the light for the given block position.
     *
     * @param pos The block position
     * @param checkNeighbors Whether to check neighboring block lighting
     * @return The light value at the block position, if the chunk is loaded
     */
    @Override
    public int getLight(BlockPos pos, boolean checkNeighbors) {
        if (((IMixinBlockPos) pos).isValidXZPosition()) { // Sponge - Replace with inlined method
            if (checkNeighbors && this.getBlockState(pos).useNeighborBrightness()) {
                int i1 = this.getLight(pos.up(), false);
                int i = this.getLight(pos.east(), false);
                int j = this.getLight(pos.west(), false);
                int k = this.getLight(pos.south(), false);
                int l = this.getLight(pos.north(), false);

                if (i > i1) {
                    i1 = i;
                }

                if (j > i1) {
                    i1 = j;
                }

                if (k > i1) {
                    i1 = k;
                }

                if (l > i1) {
                    i1 = l;
                }

                return i1;
            } else if (pos.getY() < 0) {
                return 0;
            } else {
                if (pos.getY() >= 256) {
                    pos = new BlockPos(pos.getX(), 255, pos.getZ());
                }

                // Sponge - Gets only loaded chunks, unloaded chunks will not get loaded to check lighting
                // Chunk chunk = this.getChunkFromBlockCoords(pos);
                // return chunk.getLightSubtracted(pos, this.skylightSubtracted);
                final Chunk chunk = ((IMixinChunkProviderServer) this.chunkProvider).getLoadedChunkWithoutMarkingActive(pos.getX() >> 4, pos.getZ() >> 4);
                return chunk == null ? 0 : chunk.getLightSubtracted(pos, this.getSkylightSubtracted());
                // Sponge End
            }
        } else {
            return 15;
        }
    }

    /**
     * @author gabizou - August 4th, 2016
     * @reason Rewrites the check to be inlined to {@link IMixinBlockPos}.
     *
     * @param type The type of sky lighting
     * @param pos The position
     * @return The light for the defined sky type and block position
     */
    @Override
    public int getLightFor(EnumSkyBlock type, BlockPos pos) {
        if (pos.getY() < 0) {
            pos = new BlockPos(pos.getX(), 0, pos.getZ());
        }

        // Sponge Start - Replace with inlined method to check
        // if (!this.isValid(pos)) // vanilla
        if (!((IMixinBlockPos) pos).isValidPosition()) {
            // Sponge End
            return type.defaultLightValue;
        } else {
            Chunk chunk = ((IMixinChunkProviderServer) ((WorldServer) (Object) this).getChunkProvider()).getLoadedChunkWithoutMarkingActive(pos.getX() >> 4, pos.getZ() >> 4);
            if (chunk == null) {
                return type.defaultLightValue;
            }
            return chunk.getLightFor(type, pos);
        }
    }

    @Override
    public boolean isLightLevel(Chunk chunk, BlockPos pos, int level) {
        if (((IMixinBlockPos) pos).isValidPosition()) {
            if (this.getBlockState(pos).useNeighborBrightness()) {
                if (this.getLight(pos.up(), false) >= level) {
                    return true;
                }
                if (this.getLight(pos.east(), false) >= level) {
                    return true;
                }
                if (this.getLight(pos.west(), false) >= level) {
                    return true;
                }
                if (this.getLight(pos.south(), false) >= level) {
                    return true;
                }
                if (this.getLight(pos.north(), false) >= level) {
                    return true;
                }
                return false;
            } else {
                if (pos.getY() >= 256) {
                    pos = new BlockPos(pos.getX(), 255, pos.getZ());
                }

                return chunk.getLightSubtracted(pos, this.getSkylightSubtracted()) >= level;
            }
        } else {
            return true;
        }
    }

    /**
     * @author amaranth - April 25th, 2016
     * @reason Avoid 25 chunk map lookups per entity per tick by using neighbor pointers
     *
     * @param xStart X block start coordinate
     * @param yStart Y block start coordinate
     * @param zStart Z block start coordinate
     * @param xEnd X block end coordinate
     * @param yEnd Y block end coordinate
     * @param zEnd Z block end coordinate
     * @param allowEmpty Whether empty chunks should be accepted
     * @return If the chunks for the area are loaded
     */
    @Override
    public boolean isAreaLoaded(int xStart, int yStart, int zStart, int xEnd, int yEnd, int zEnd, boolean allowEmpty) {
        if (yEnd < 0 || yStart > 255) {
            return false;
        }

        xStart = xStart >> 4;
        zStart = zStart >> 4;
        xEnd = xEnd >> 4;
        zEnd = zEnd >> 4;

        net.minecraft.world.chunk.Chunk base = ((IMixinChunkProviderServer) this.getChunkProvider()).getLoadedChunkWithoutMarkingActive(xStart, zStart);
        if (base == null) {
            return false;
        }

        IMixinChunk currentColumn = (IMixinChunk) base;
        for (int i = xStart; i <= xEnd; i++) {
            if (currentColumn == null) {
                return false;
            }

            IMixinChunk currentRow = (IMixinChunk) currentColumn.getNeighborChunk(1);
            for (int j = zStart; j <= zEnd; j++) {
                if (currentRow == null) {
                    return false;
                }

                if (!allowEmpty && ((net.minecraft.world.chunk.Chunk) currentRow).isEmpty()) {
                    return false;
                }

                currentRow = (IMixinChunk) currentRow.getNeighborChunk(1);
            }

            currentColumn = (IMixinChunk) currentColumn.getNeighborChunk(2);
        }

        return true;
    }

    @Override
    public WorldStorage getWorldStorage() {
        return (WorldStorage) ((WorldServer) (Object) this).getChunkProvider();
    }

    @Override
    public PortalAgent getPortalAgent() {
        return (PortalAgent) this.worldTeleporter;
    }

    @Redirect(method = "canAddEntity", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;[Ljava/lang/Object;)V", ordinal = 1, remap = false))
    public void onCanAddEntityLogWarn(Logger logger, String message, Object... params) {
        // don't log anything to avoid useless spam
    }
    /**************************** TIMINGS ***************************************/
    /*
    The remaining of these overridden methods are all injectors into World#updateEntities() to where
    the exact fine tuning of where the methods are invoked, the call stack is precisely emulated as
    if this were an overwrite. The injections themselves are sensitive in some regards, but mostly
    will remain just fine.
     */


    @Override
    protected void startEntityGlobalTimings() {
        this.timings.entityTick.startTiming();
        co.aikar.timings.TimingHistory.entityTicks += this.loadedEntityList.size();
    }

    @Override
    protected void stopTimingForWeatherEntityTickCrash(net.minecraft.entity.Entity updatingEntity) {
        EntityUtil.toMixin(updatingEntity).getTimingsHandler().stopTiming();
    }

    @Override
    protected void stopEntityTickTimingStartEntityRemovalTiming() {
        this.timings.entityTick.stopTiming();
        this.timings.entityRemoval.startTiming();
    }

    @Override
    protected void stopEntityRemovalTiming() {
        this.timings.entityRemoval.stopTiming();
    }

    @Override
    protected void startEntityTickTiming() {
        this.timings.entityTick.startTiming();
    }

    @Override
    protected void stopTimingTickEntityCrash(net.minecraft.entity.Entity updatingEntity) {
        EntityUtil.toMixin(updatingEntity).getTimingsHandler().stopTiming();
    }

    @Override
    protected void stopEntityTickSectionBeforeRemove() {
       this.timings.entityTick.stopTiming();
    }

    @Override
    protected void startEntityRemovalTick() {
        this.timings.entityRemoval.startTiming();
    }

    @Override
    protected void startTileTickTimer() {
        this.timings.tileEntityTick.startTiming();
    }

    @Override
    protected void stopTimingTickTileEntityCrash(net.minecraft.tileentity.TileEntity updatingTileEntity) {
        ((IMixinTileEntity) updatingTileEntity).getTimingsHandler().stopTiming();
    }

    @Override
    protected void stopTileEntityAndStartRemoval() {
        this.timings.tileEntityTick.stopTiming();
        this.timings.tileEntityRemoval.startTiming();
    }

    @Override
    protected void stopTileEntityRemovelInWhile() {
        this.timings.tileEntityRemoval.stopTiming();
    }

    @Override
    protected void startPendingTileEntityTimings() {
        this.timings.tileEntityPending.startTiming();
    }

    @Override
    protected void endPendingTileEntities() {
        this.timings.tileEntityPending.stopTiming();
        TimingHistory.tileEntityTicks += this.loadedTileEntityList.size();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE_STRING", target = PROFILER_ESS, args = "ldc=tickPending") )
    private void onBeginTickBlockUpdate(CallbackInfo ci) {
        this.timings.scheduledBlocks.startTiming();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE_STRING", target = PROFILER_ESS, args = "ldc=tickBlocks") )
    private void onAfterTickBlockUpdate(CallbackInfo ci) {
        this.timings.scheduledBlocks.stopTiming();
        this.timings.updateBlocks.startTiming();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE_STRING", target = PROFILER_ESS, args = "ldc=chunkMap") )
    private void onBeginUpdateBlocks(CallbackInfo ci) {
        this.timings.updateBlocks.stopTiming();
        this.timings.doChunkMap.startTiming();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE_STRING", target = PROFILER_ESS, args = "ldc=village") )
    private void onBeginUpdateVillage(CallbackInfo ci) {
        this.timings.doChunkMap.stopTiming();
        this.timings.doVillages.startTiming();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE_STRING", target = PROFILER_ESS, args = "ldc=portalForcer"))
    private void onBeginUpdatePortal(CallbackInfo ci) {
        this.timings.doVillages.stopTiming();
        this.timings.doPortalForcer.startTiming();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endSection()V"))
    private void onEndUpdatePortal(CallbackInfo ci) {
        this.timings.doPortalForcer.stopTiming();
    }
    // TIMINGS
    @Inject(method = "tickUpdates", at = @At(value = "INVOKE_STRING", target = PROFILER_SS, args = "ldc=cleaning"))
    private void onTickUpdatesCleanup(boolean flag, CallbackInfoReturnable<Boolean> cir) {
        this.timings.scheduledBlocksCleanup.startTiming();
    }

    @Inject(method = "tickUpdates", at = @At(value = "INVOKE_STRING", target = PROFILER_SS, args = "ldc=ticking"))
    private void onTickUpdatesTickingStart(boolean flag, CallbackInfoReturnable<Boolean> cir) {
        this.timings.scheduledBlocksCleanup.stopTiming();
        this.timings.scheduledBlocksTicking.startTiming();
    }

    @Inject(method = "tickUpdates", at = @At("RETURN"))
    private void onTickUpdatesTickingEnd(CallbackInfoReturnable<Boolean> cir) {
        this.timings.scheduledBlocksTicking.stopTiming();
    }

    @Override
    public WorldTimingsHandler getTimingsHandler() {
        return this.timings;
    }

    /**************************** EFFECT ****************************************/

    @Override
    public void playSound(SoundType sound, SoundCategory category, Vector3d position, double volume) {
        this.playSound(sound, category, position, volume, 1);
    }

    @Override
    public void playSound(SoundType sound,  SoundCategory category, Vector3d position, double volume, double pitch) {
        this.playSound(sound, category, position, volume, pitch, 0);
    }

    @Override
    public void playSound(SoundType sound,  SoundCategory category, Vector3d position, double volume, double pitch, double minVolume) {
        SoundEvent event;
        try {
            // Check if the event is registered (ie has an integer ID)
            event = SoundEvents.getRegisteredSoundEvent(sound.getId());
        } catch (IllegalStateException e) {
            // Otherwise send it as a custom sound
            this.playCustomSound(null, position.getX(), position.getY(), position.getZ(), sound.getId(),
                    (net.minecraft.util.SoundCategory) (Object) category, (float) Math.max(minVolume, volume), (float) pitch);
            return;
        }

        this.playSound(null, position.getX(), position.getY(), position.getZ(), event, (net.minecraft.util.SoundCategory) (Object) category,
                (float) Math.max(minVolume, volume), (float) pitch);
    }

    @Override
    public void playCustomSound(@Nullable EntityPlayer player, double x, double y, double z, String soundIn, net.minecraft.util.SoundCategory category,
            float volume, float pitch) {

        if (player instanceof IMixinEntity) {
            if (((IMixinEntity) player).isVanished()) {
                return;
            }
        }

        this.eventListeners.stream()
                .filter(listener -> listener instanceof IMixinServerWorldEventHandler)
                .map(listener -> (IMixinServerWorldEventHandler) listener)
                .forEach(listener -> {
                    // There's no method for playing a custom sound to all, so I made one -_-
                    listener.playCustomSoundToAllNearExcept(null, soundIn, category, x, y, z, volume, pitch);
                });
    }

    @Override
    public void spawnParticles(ParticleEffect particleEffect, Vector3d position) {
        this.spawnParticles(particleEffect, position, Integer.MAX_VALUE);
    }

    @Override
    public void spawnParticles(ParticleEffect particleEffect, Vector3d position, int radius) {
        checkNotNull(particleEffect, "The particle effect cannot be null!");
        checkNotNull(position, "The position cannot be null");
        checkArgument(radius > 0, "The radius has to be greater then zero!");

        List<Packet<?>> packets = SpongeParticleHelper.toPackets((SpongeParticleEffect) particleEffect, position);

        if (!packets.isEmpty()) {
            PlayerList playerList = this.mcServer.getPlayerList();

            double x = position.getX();
            double y = position.getY();
            double z = position.getZ();

            for (Packet<?> packet : packets) {
                playerList.sendToAllNearExcept(null, x, y, z, radius, this.getDimensionId(), packet);
            }
        }
    }


    @Override
    public Weather getWeather() {
        if (this.worldInfo.isThundering()) {
            return Weathers.THUNDER_STORM;
        } else if (this.worldInfo.isRaining()) {
            return Weathers.RAIN;
        } else {
            return Weathers.CLEAR;
        }
    }

    @Override
    public long getRemainingDuration() {
        Weather weather = getWeather();
        if (weather.equals(Weathers.CLEAR)) {
            if (this.worldInfo.getCleanWeatherTime() > 0) {
                return this.worldInfo.getCleanWeatherTime();
            } else {
                return Math.min(this.worldInfo.getThunderTime(), this.worldInfo.getRainTime());
            }
        } else if (weather.equals(Weathers.THUNDER_STORM)) {
            return this.worldInfo.getThunderTime();
        } else if (weather.equals(Weathers.RAIN)) {
            return this.worldInfo.getRainTime();
        }
        return 0;
    }

    @Override
    public long getRunningDuration() {
        return this.worldInfo.getWorldTotalTime() - this.weatherStartTime;
    }

    @Override
    public void setWeather(Weather weather) {
        if (weather.equals(Weathers.CLEAR)) {
            this.setWeather(weather, (300 + this.rand.nextInt(600)) * 20);
        } else {
            this.setWeather(weather, 0);
        }
    }

    @Override
    public void setWeather(Weather weather, long duration) {
        if (weather.equals(Weathers.CLEAR)) {
            this.worldInfo.setCleanWeatherTime((int) duration);
            this.worldInfo.setRainTime(0);
            this.worldInfo.setThunderTime(0);
            this.worldInfo.setRaining(false);
            this.worldInfo.setThundering(false);
        } else if (weather.equals(Weathers.RAIN)) {
            this.worldInfo.setCleanWeatherTime(0);
            this.worldInfo.setRainTime((int) duration);
            this.worldInfo.setThunderTime((int) duration);
            this.worldInfo.setRaining(true);
            this.worldInfo.setThundering(false);
        } else if (weather.equals(Weathers.THUNDER_STORM)) {
            this.worldInfo.setCleanWeatherTime(0);
            this.worldInfo.setRainTime((int) duration);
            this.worldInfo.setThunderTime((int) duration);
            this.worldInfo.setRaining(true);
            this.worldInfo.setThundering(true);
        }
    }

    @Inject(method = "updateWeather", at = @At(value = "RETURN"))
    public void onUpdateWeatherReturn(CallbackInfo ci) {
        Weather weather = getWeather();
        int duration = (int) getRemainingDuration();
        if (this.prevWeather != weather && duration > 0) {
            ChangeWorldWeatherEvent event = SpongeEventFactory.createChangeWorldWeatherEvent(Cause.of(NamedCause.source(this)), duration, duration,
                    weather, weather, this.prevWeather, this);
            SpongeImpl.postEvent(event);
            if (event.isCancelled()) {
                this.setWeather(this.prevWeather);
            } else {
                // TODO: Rewrite this correctly so it doesn't rain 24/7
                //this.setWeather(event.getWeather(), event.getDuration());
                this.prevWeather = event.getWeather();
                this.weatherStartTime = this.worldInfo.getWorldTotalTime();
            }
        }
    }

    @Override
    public long getWeatherStartTime() {
        return this.weatherStartTime;
    }

    @Override
    public void setWeatherStartTime(long weatherStartTime) {
        this.weatherStartTime = weatherStartTime;
    }

    @Override
    public int getChunkGCTickInterval() {
        return this.chunkGCTickInterval;
    }

    @Override
    public long getChunkUnloadDelay() {
        return this.chunkUnloadDelay;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("LevelName", this.worldInfo.getWorldName())
                .add("DimensionId", this.provider.getDimensionType().getId())
                .add("DimensionType", ((org.spongepowered.api.world.DimensionType) (Object) this.provider.getDimensionType()).getId())
                .toString();
    }
}
