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
package org.spongepowered.common.world.storage;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataMap;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.MemoryDataMap;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.world.WorldManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

public final class SpongePlayerDataHandler {

    private static final String SPONGE_DATA = "sponge";
    private boolean hasInitialized = false;
    private Path playerDir;

    private Map<UUID, SpongePlayerData> playerDataMap;

    public static void init() {
        final SpongePlayerDataHandler handlerInstance = Holder.INSTANCE;
        if (!Sponge.isServerAvailable()) {
            return;
        }
        handlerInstance.playerDataMap = new ConcurrentHashMap<>();
        final Path filePath = WorldManager.getCurrentSavesDirectory().get().resolve("data").resolve
                (SPONGE_DATA);

        try {
            handlerInstance.playerDir = filePath;
            Files.createDirectories(handlerInstance.playerDir);

            final List<Path> playerFiles = new ArrayList<>();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(filePath, "*.{dat}")) {
                for (Path entry : stream) {
                    playerFiles.add(entry);
                }
            } catch (DirectoryIteratorException e) {
                SpongeImpl.getLogger().error("Something happened when trying to gather all player files", e);
            }
            for (Path playerFile : playerFiles) {
                if (Files.isReadable(playerFile)) {
                    NBTTagCompound compound;

                    try (final InputStream stream = Files.newInputStream(playerFile)) {
                        compound = CompressedStreamTools.readCompressed(stream);
                    }

                    // TODO Hard exception? Logger entry?
                    if (compound == null) {
                        throw new RuntimeException("Failed to decompress player data within [" + playerFile + "]!");
                    }

                    DataMap container = NbtTranslator.getInstance().translateFrom(compound);
                    SpongePlayerData data = container.getObject(DataQuery.of(), SpongePlayerData.class).get();
                    handlerInstance.playerDataMap.put(data.uuid, data);
                }
            }
            playerFiles.clear();

        } catch (FileAlreadyExistsException e) {
            SpongeImpl.getLogger().error("Someone went and created a file for the desired path: {}", filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        handlerInstance.hasInitialized = true;
    }

    public static void savePlayer(UUID id) {
        checkState(Holder.INSTANCE.hasInitialized, "PlayerDataHandler hasn't initialized yet!");
        SpongePlayerDataHandler instance = Holder.INSTANCE;
        @Nullable SpongePlayerData data = instance.playerDataMap.get(checkNotNull(id, "Player id cannot be null!"));
        if (data != null) {
            saveFile(id.toString(), createCompoundFor(data));
        } else {
            SpongeImpl.getLogger().error("Couldn't find a player data for the uuid: " + id.toString());
        }
    }

    private static NBTTagCompound createCompoundFor(SpongePlayerData data) {
        DataMap map = new MemoryDataMap();
        data.toContainer(map);
        return NbtTranslator.getInstance().translateData(map);
    }

    private static void saveFile(String id, NBTTagCompound compound) {
        checkState(Holder.INSTANCE.hasInitialized, "PlayerDataHandler hasn't initialized yet!");
        SpongePlayerDataHandler instance = Holder.INSTANCE;
        try {
            final Path newDatPath = instance.playerDir.resolve(id + ".dat.tmp");
            if (Files.notExists(newDatPath)) {
                Files.createFile(newDatPath);
            }

            final Path finalDatPath = instance.playerDir.resolve(id + ".dat");

            try (OutputStream stream = Files.newOutputStream(newDatPath)) {
                CompressedStreamTools.writeCompressed(compound, stream);

                try {
                    Files.deleteIfExists(finalDatPath);
                } catch (IOException ioe) {
                    SpongeImpl.getLogger().error("Failed to delete the previous file [" + finalDatPath + "]!", ioe);
                    return;
                }

                try {
                    Files.move(newDatPath, finalDatPath,
                            StandardCopyOption.REPLACE_EXISTING); // Replace is our last ditch effort to put this into place
                } catch (IOException ioe) {
                    SpongeImpl.getLogger().error("Could not rename file [{}] to [{}]!", newDatPath, finalDatPath);
                    return;
                }

                try {
                    Files.deleteIfExists(newDatPath);
                } catch (IOException ioe) {
                    SpongeImpl.getLogger().error("Could not delete file [{}]!", newDatPath);
                }
            }
        } catch (Exception e) {
            SpongeImpl.getLogger().error("Failed to save player data for [{}]!", id, e);
        }
    }

    public static void setPlayerInfo(UUID playerId, Instant join, Instant last) {
        checkState(Holder.INSTANCE.hasInitialized, "PlayerDataHandler hasn't initialized yet!");
        checkNotNull(join, "Joined date cannot be null!");
        checkNotNull(last, "Last joined date cannot be null!");
        SpongePlayerDataHandler instance = Holder.INSTANCE;

        SpongePlayerData data = instance.playerDataMap.get(checkNotNull(playerId, "Player UUID cannot be null!"));
        if (data == null) {
            data = new SpongePlayerData();
            data.uuid = playerId;
        }
        data.firstJoined = join.toEpochMilli();
        data.lastJoined = last.toEpochMilli();
        instance.playerDataMap.put(playerId, data);
    }

    public static Optional<Instant> getFirstJoined(UUID player) {
        checkState(Holder.INSTANCE.hasInitialized, "PlayerDataHandler hasn't initialized yet!");
        final SpongePlayerData data = Holder.INSTANCE.playerDataMap.get(player);
        return Optional.ofNullable(data == null ? null : Instant.ofEpochMilli(data.firstJoined));
    }

    public static Optional<Instant> getLastPlayed(UUID player) {
        checkState(Holder.INSTANCE.hasInitialized, "PlayerDataHandler hasn't initialized yet!");
        final SpongePlayerData data = Holder.INSTANCE.playerDataMap.get(player);
        return Optional.ofNullable(data == null ? null : Instant.ofEpochMilli(data.lastJoined));
    }

    SpongePlayerDataHandler() { }

    private static final class Holder {
        static final SpongePlayerDataHandler INSTANCE = new SpongePlayerDataHandler();
    }
}
