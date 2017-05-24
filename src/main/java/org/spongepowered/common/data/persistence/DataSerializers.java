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
package org.spongepowered.common.data.persistence;

import com.flowpowered.math.imaginary.Complexd;
import com.flowpowered.math.imaginary.Complexf;
import com.flowpowered.math.imaginary.Quaterniond;
import com.flowpowered.math.imaginary.Quaternionf;
import com.flowpowered.math.vector.Vector2d;
import com.flowpowered.math.vector.Vector2f;
import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector2l;
import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3f;
import com.flowpowered.math.vector.Vector3i;
import com.flowpowered.math.vector.Vector3l;
import com.flowpowered.math.vector.Vector4d;
import com.flowpowered.math.vector.Vector4f;
import com.flowpowered.math.vector.Vector4i;
import com.flowpowered.math.vector.Vector4l;
import com.google.common.reflect.TypeToken;
import org.spongepowered.api.data.DataManager;
import org.spongepowered.api.data.DataMap;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.common.data.util.DataQueries;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.UUID;
import java.util.function.Supplier;

public final class DataSerializers {

    public static final DataTranslator<UUID> UUID_DATA_SERIALIZER;
    public static final DataTranslator<Vector2d> VECTOR_2_D_DATA_SERIALIZER;
    public static final DataTranslator<Vector2f> VECTOR_2_F_DATA_SERIALIZER;
    public static final DataTranslator<Vector2i> VECTOR_2_I_DATA_SERIALIZER;
    public static final DataTranslator<Vector2l> VECTOR_2_L_DATA_SERIALIZER;
    public static final DataTranslator<Vector3d> VECTOR_3_D_DATA_SERIALIZER;
    public static final DataTranslator<Vector3f> VECTOR_3_F_DATA_SERIALIZER;
    public static final DataTranslator<Vector3i> VECTOR_3_I_DATA_SERIALIZER;
    public static final DataTranslator<Vector3l> VECTOR_3_L_DATA_SERIALIZER;
    public static final DataTranslator<Vector4d> VECTOR_4_D_DATA_SERIALIZER;
    public static final DataTranslator<Vector4f> VECTOR_4_F_DATA_SERIALIZER;
    public static final DataTranslator<Vector4i> VECTOR_4_I_DATA_SERIALIZER;
    public static final DataTranslator<Vector4l> VECTOR_4_L_DATA_SERIALIZER;
    public static final DataTranslator<Complexd> COMPLEXD_DATA_SERIALIZER;
    public static final DataTranslator<Complexf> COMPLEXF_DATA_SERIALIZER;
    public static final DataTranslator<Quaterniond> QUATERNIOND_DATA_SERIALIZER;
    public static final DataTranslator<Quaternionf> QUATERNIONF_DATA_SERIALIZER;
    public static final DataTranslator<LocalTime> LOCAL_TIME_DATA_SERIALIZER;
    public static final DataTranslator<LocalDate> LOCAL_DATE_DATA_SERIALIZER;
    public static final DataTranslator<LocalDateTime> LOCAL_DATE_TIME_DATA_SERIALIZER;
    public static final DataTranslator<Instant> INSTANT_DATA_SERIALIZER;
    public static final DataTranslator<ZonedDateTime> ZONED_DATE_TIME_DATA_SERIALIZER;

    static {
        UUID_DATA_SERIALIZER = new DataTranslator<UUID>() {

            final TypeToken<UUID> token = TypeToken.of(UUID.class);

            @Override
            public TypeToken<UUID> getToken() {
                return this.token;
            }

            @Override
            public UUID translate(DataMap view) throws InvalidDataException {
                final long most = view.getLong(Queries.UUID_MOST).orElseThrow(invalidDataQuery(Queries.UUID_MOST));
                final long least = view.getLong(Queries.UUID_LEAST).orElseThrow(invalidDataQuery(Queries.UUID_LEAST));
                return new UUID(most, least);
            }

            @Override
            public DataMap translate(UUID obj, DataMap view) {
                return view
                    .set(Queries.UUID_LEAST, obj.getLeastSignificantBits())
                    .set(Queries.UUID_MOST, obj.getMostSignificantBits());
            }

            @Override
            public String getId() {
                return "sponge:uuid";
            }

            @Override
            public String getName() {
                return "UuidTranslator";
            }
        };
        VECTOR_2_D_DATA_SERIALIZER = new DataTranslator<Vector2d>() {

            final TypeToken<Vector2d> token = TypeToken.of(Vector2d.class);

            @Override
            public TypeToken<Vector2d> getToken() {
                return this.token;
            }

            @Override
            public Vector2d translate(DataMap view) throws InvalidDataException {
                final double x = view.getDouble(DataQueries.X_POS).orElseThrow(invalidDataQuery(DataQueries.X_POS));
                final double y = view.getDouble(DataQueries.Y_POS).orElseThrow(invalidDataQuery(DataQueries.Y_POS));
                return new Vector2d(x, y);
            }

            @Override
            public DataMap translate(Vector2d obj, DataMap view) {
                return view.set(DataQueries.X_POS, obj.getX())
                    .set(DataQueries.Y_POS, obj.getY());
            }

            @Override
            public String getId() {
                return "sponge:vector_2_d";
            }

            @Override
            public String getName() {
                return "Vector2dTranslator";
            }
        };
        VECTOR_2_F_DATA_SERIALIZER = new DataTranslator<Vector2f>() {

            final TypeToken<Vector2f> token = TypeToken.of(Vector2f.class);

            @Override
            public TypeToken<Vector2f> getToken() {
                return this.token;
            }

            @Override
            public Vector2f translate(DataMap view) throws InvalidDataException {
                final double x = view.getDouble(DataQueries.X_POS).orElseThrow(invalidDataQuery(DataQueries.X_POS));
                final double y = view.getDouble(DataQueries.Y_POS).orElseThrow(invalidDataQuery(DataQueries.Y_POS));
                return new Vector2f(x, y);
            }

            @Override
            public DataMap translate(Vector2f obj, DataMap view) {
                return view.set(DataQueries.X_POS, obj.getX())
                    .set(DataQueries.Y_POS, obj.getY());
            }

            @Override
            public String getId() {
                return "sponge:vector_2_f";
            }

            @Override
            public String getName() {
                return "Vector2fTranslator";
            }
        };
        VECTOR_2_I_DATA_SERIALIZER = new DataTranslator<Vector2i>() {

            final TypeToken<Vector2i> token = TypeToken.of(Vector2i.class);

            @Override
            public TypeToken<Vector2i> getToken() {
                return this.token;
            }

            @Override
            public Vector2i translate(DataMap view) throws InvalidDataException {
                final int x = view.getInt(DataQueries.X_POS).orElseThrow(invalidDataQuery(DataQueries.X_POS));
                final int y = view.getInt(DataQueries.Y_POS).orElseThrow(invalidDataQuery(DataQueries.Y_POS));
                return new Vector2i(x, y);
            }

            @Override
            public DataMap translate(Vector2i obj, DataMap view) {
                return view.set(DataQueries.X_POS, obj.getX())
                    .set(DataQueries.Y_POS, obj.getY());
            }

            @Override
            public String getId() {
                return "sponge:vector_2_i";
            }

            @Override
            public String getName() {
                return "Vector2iTranslator";
            }
        };
        VECTOR_2_L_DATA_SERIALIZER = new DataTranslator<Vector2l>() {

            final TypeToken<Vector2l> token = TypeToken.of(Vector2l.class);

            @Override
            public TypeToken<Vector2l> getToken() {
                return this.token;
            }

            @Override
            public Vector2l translate(DataMap view) throws InvalidDataException {
                final long x = view.getLong(DataQueries.X_POS).orElseThrow(invalidDataQuery(DataQueries.X_POS));
                final long y = view.getLong(DataQueries.Y_POS).orElseThrow(invalidDataQuery(DataQueries.Y_POS));
                return new Vector2l(x, y);
            }

            @Override
            public DataMap translate(Vector2l obj, DataMap view) {
                return view.set(DataQueries.X_POS, obj.getX())
                    .set(DataQueries.Y_POS, obj.getY());
            }

            @Override
            public String getId() {
                return "sponge:vector_2_l";
            }

            @Override
            public String getName() {
                return "Vector2lTranslator";
            }
        };
        VECTOR_3_D_DATA_SERIALIZER = new DataTranslator<Vector3d>() {

            final TypeToken<Vector3d> token = TypeToken.of(Vector3d.class);

            @Override
            public TypeToken<Vector3d> getToken() {
                return this.token;
            }

            @Override
            public Vector3d translate(DataMap view) throws InvalidDataException {
                final double x = view.getDouble(DataQueries.X_POS).orElseThrow(invalidDataQuery(DataQueries.X_POS));
                final double y = view.getDouble(DataQueries.Y_POS).orElseThrow(invalidDataQuery(DataQueries.Y_POS));
                final double z = view.getDouble(DataQueries.Z_POS).orElseThrow(invalidDataQuery(DataQueries.Z_POS));
                return new Vector3d(x, y, z);
            }

            @Override
            public DataMap translate(Vector3d obj, DataMap view) {
                return view.set(DataQueries.X_POS, obj.getX())
                    .set(DataQueries.Y_POS, obj.getY())
                    .set(DataQueries.Z_POS, obj.getZ());
            }

            @Override
            public String getId() {
                return "sponge:vector_3_d";
            }

            @Override
            public String getName() {
                return "Vector3dTranslator";
            }
        };
        VECTOR_3_F_DATA_SERIALIZER = new DataTranslator<Vector3f>() {

            final TypeToken<Vector3f> token = TypeToken.of(Vector3f.class);

            @Override
            public TypeToken<Vector3f> getToken() {
                return this.token;
            }

            @Override
            public Vector3f translate(DataMap view) throws InvalidDataException {
                final double x = view.getDouble(DataQueries.X_POS).orElseThrow(invalidDataQuery(DataQueries.X_POS));
                final double y = view.getDouble(DataQueries.Y_POS).orElseThrow(invalidDataQuery(DataQueries.Y_POS));
                final double z = view.getDouble(DataQueries.Z_POS).orElseThrow(invalidDataQuery(DataQueries.Z_POS));
                return new Vector3f(x, y, z);
            }

            @Override
            public DataMap translate(Vector3f obj, DataMap view) {
                return view.set(DataQueries.X_POS, obj.getX())
                    .set(DataQueries.Y_POS, obj.getY())
                    .set(DataQueries.Z_POS, obj.getZ());
            }

            @Override
            public String getId() {
                return "sponge:vector_3_f";
            }

            @Override
            public String getName() {
                return "Vector3fTranslator";
            }
        };
        VECTOR_3_I_DATA_SERIALIZER = new DataTranslator<Vector3i>() {

            final TypeToken<Vector3i> token = TypeToken.of(Vector3i.class);

            @Override
            public TypeToken<Vector3i> getToken() {
                return this.token;
            }

            @Override
            public Vector3i translate(DataMap view) throws InvalidDataException {
                final int x = view.getInt(DataQueries.X_POS).orElseThrow(invalidDataQuery(DataQueries.X_POS));
                final int y = view.getInt(DataQueries.Y_POS).orElseThrow(invalidDataQuery(DataQueries.Y_POS));
                final int z = view.getInt(DataQueries.Z_POS).orElseThrow(invalidDataQuery(DataQueries.Z_POS));
                return new Vector3i(x, y, z);
            }

            @Override
            public DataMap translate(Vector3i obj, DataMap view) {
                return view.set(DataQueries.X_POS, obj.getX())
                    .set(DataQueries.Y_POS, obj.getY())
                    .set(DataQueries.Z_POS, obj.getZ());
            }

            @Override
            public String getId() {
                return "sponge:vector_3_i";
            }

            @Override
            public String getName() {
                return "Vector3iTranslator";
            }
        };
        VECTOR_3_L_DATA_SERIALIZER = new DataTranslator<Vector3l>() {

            final TypeToken<Vector3l> token = TypeToken.of(Vector3l.class);

            @Override
            public TypeToken<Vector3l> getToken() {
                return this.token;
            }

            @Override
            public Vector3l translate(DataMap view) throws InvalidDataException {
                final long x = view.getLong(DataQueries.X_POS).orElseThrow(invalidDataQuery(DataQueries.X_POS));
                final long y = view.getLong(DataQueries.Y_POS).orElseThrow(invalidDataQuery(DataQueries.Y_POS));
                final long z = view.getLong(DataQueries.Z_POS).orElseThrow(invalidDataQuery(DataQueries.Z_POS));
                return new Vector3l(x, y, z);
            }

            @Override
            public DataMap translate(Vector3l obj, DataMap view) {
                return view.set(DataQueries.X_POS, obj.getX())
                    .set(DataQueries.Y_POS, obj.getY())
                    .set(DataQueries.Z_POS, obj.getZ());
            }

            @Override
            public String getId() {
                return "sponge:vector_3_l";
            }

            @Override
            public String getName() {
                return "Vector3lTranslator";
            }
        };
        VECTOR_4_F_DATA_SERIALIZER = new DataTranslator<Vector4f>() {

            final TypeToken<Vector4f> token = TypeToken.of(Vector4f.class);

            @Override
            public TypeToken<Vector4f> getToken() {
                return this.token;
            }

            @Override
            public Vector4f translate(DataMap view) throws InvalidDataException {
                final double x = view.getDouble(DataQueries.X_POS).orElseThrow(invalidDataQuery(DataQueries.X_POS));
                final double y = view.getDouble(DataQueries.Y_POS).orElseThrow(invalidDataQuery(DataQueries.Y_POS));
                final double z = view.getDouble(DataQueries.Z_POS).orElseThrow(invalidDataQuery(DataQueries.Z_POS));
                final double w = view.getDouble(DataQueries.W_POS).orElseThrow(invalidDataQuery(DataQueries.W_POS));
                return new Vector4f(x, y, z, w);
            }

            @Override
            public DataMap translate(Vector4f obj, DataMap view) {
                return view.set(DataQueries.X_POS, obj.getX())
                    .set(DataQueries.Y_POS, obj.getY())
                    .set(DataQueries.Z_POS, obj.getZ())
                    .set(DataQueries.W_POS, obj.getW());
            }

            @Override
            public String getId() {
                return "sponge:vector_4_f";
            }

            @Override
            public String getName() {
                return "Vector4fTranslator";
            }
        };
        VECTOR_4_I_DATA_SERIALIZER = new DataTranslator<Vector4i>() {

            final TypeToken<Vector4i> token = TypeToken.of(Vector4i.class);

            @Override
            public TypeToken<Vector4i> getToken() {
                return this.token;
            }

            @Override
            public Vector4i translate(DataMap view) throws InvalidDataException {
                final int x = view.getInt(DataQueries.X_POS).orElseThrow(invalidDataQuery(DataQueries.X_POS));
                final int y = view.getInt(DataQueries.Y_POS).orElseThrow(invalidDataQuery(DataQueries.Y_POS));
                final int z = view.getInt(DataQueries.Z_POS).orElseThrow(invalidDataQuery(DataQueries.Z_POS));
                final int w = view.getInt(DataQueries.W_POS).orElseThrow(invalidDataQuery(DataQueries.W_POS));
                return new Vector4i(x, y, z, w);
            }

            @Override
            public DataMap translate(Vector4i obj, DataMap view) {
                return view.set(DataQueries.X_POS, obj.getX())
                    .set(DataQueries.Y_POS, obj.getY())
                    .set(DataQueries.Z_POS, obj.getZ())
                    .set(DataQueries.W_POS, obj.getW());
            }

            @Override
            public String getId() {
                return "sponge:vector_4_i";
            }

            @Override
            public String getName() {
                return "Vector4iTranslator";
            }
        };
        VECTOR_4_L_DATA_SERIALIZER = new DataTranslator<Vector4l>() {

            final TypeToken<Vector4l> token = TypeToken.of(Vector4l.class);

            @Override
            public TypeToken<Vector4l> getToken() {
                return this.token;
            }

            @Override
            public Vector4l translate(DataMap view) throws InvalidDataException {
                final long x = view.getLong(DataQueries.X_POS).orElseThrow(invalidDataQuery(DataQueries.X_POS));
                final long y = view.getLong(DataQueries.Y_POS).orElseThrow(invalidDataQuery(DataQueries.Y_POS));
                final long z = view.getLong(DataQueries.Z_POS).orElseThrow(invalidDataQuery(DataQueries.Z_POS));
                final long w = view.getLong(DataQueries.W_POS).orElseThrow(invalidDataQuery(DataQueries.W_POS));
                return new Vector4l(x, y, z, w);
            }

            @Override
            public DataMap translate(Vector4l obj, DataMap view) {
                return view.set(DataQueries.X_POS, obj.getX())
                    .set(DataQueries.Y_POS, obj.getY())
                    .set(DataQueries.Z_POS, obj.getZ())
                    .set(DataQueries.W_POS, obj.getW());
            }

            @Override
            public String getId() {
                return "sponge:vector_4_l";
            }

            @Override
            public String getName() {
                return "Vector4lTranslator";
            }
        };
        VECTOR_4_D_DATA_SERIALIZER = new DataTranslator<Vector4d>() {

            final TypeToken<Vector4d> token = TypeToken.of(Vector4d.class);

            @Override
            public TypeToken<Vector4d> getToken() {
                return this.token;
            }

            @Override
            public Vector4d translate(DataMap view) throws InvalidDataException {
                final double x = view.getDouble(DataQueries.X_POS).orElseThrow(invalidDataQuery(DataQueries.X_POS));
                final double y = view.getDouble(DataQueries.Y_POS).orElseThrow(invalidDataQuery(DataQueries.Y_POS));
                final double z = view.getDouble(DataQueries.Z_POS).orElseThrow(invalidDataQuery(DataQueries.Z_POS));
                final double w = view.getDouble(DataQueries.W_POS).orElseThrow(invalidDataQuery(DataQueries.W_POS));
                return new Vector4d(x, y, z, w);
            }

            @Override
            public DataMap translate(Vector4d obj, DataMap view) {
                return view.set(DataQueries.X_POS, obj.getX())
                    .set(DataQueries.Y_POS, obj.getY())
                    .set(DataQueries.Z_POS, obj.getZ())
                    .set(DataQueries.W_POS, obj.getW());
            }

            @Override
            public String getId() {
                return "sponge:vector_4_d";
            }

            @Override
            public String getName() {
                return "Vector4dTranslator";
            }
        };
        COMPLEXD_DATA_SERIALIZER = new DataTranslator<Complexd>() {

            final TypeToken<Complexd> token = TypeToken.of(Complexd.class);

            @Override
            public TypeToken<Complexd> getToken() {
                return this.token;
            }

            @Override
            public Complexd translate(DataMap view) throws InvalidDataException {
                final double x = view.getDouble(DataQueries.X_POS).orElseThrow(invalidDataQuery(DataQueries.X_POS));
                final double y = view.getDouble(DataQueries.Y_POS).orElseThrow(invalidDataQuery(DataQueries.Y_POS));
                return new Complexd(x, y);
            }

            @Override
            public DataMap translate(Complexd obj, DataMap view) {
                return view.set(DataQueries.X_POS, obj.getX())
                    .set(DataQueries.Y_POS, obj.getY());
            }

            @Override
            public String getId() {
                return "sponge:complexd";
            }

            @Override
            public String getName() {
                return "ComplexDTranslator";
            }
        };
        COMPLEXF_DATA_SERIALIZER = new DataTranslator<Complexf>() {

            final TypeToken<Complexf> token = TypeToken.of(Complexf.class);

            @Override
            public TypeToken<Complexf> getToken() {
                return this.token;
            }

            @Override
            public Complexf translate(DataMap view) throws InvalidDataException {
                final double x = view.getDouble(DataQueries.X_POS).orElseThrow(invalidDataQuery(DataQueries.X_POS));
                final double y = view.getDouble(DataQueries.Y_POS).orElseThrow(invalidDataQuery(DataQueries.Y_POS));
                return new Complexf(x, y);
            }

            @Override
            public DataMap translate(Complexf obj, DataMap view) {
                return view.set(DataQueries.X_POS, obj.getX())
                    .set(DataQueries.Y_POS, obj.getY());
            }

            @Override
            public String getId() {
                return "sponge:complexf";
            }

            @Override
            public String getName() {
                return "ComplexFTranslator";
            }
        };
        QUATERNIOND_DATA_SERIALIZER = new DataTranslator<Quaterniond>() {

            final TypeToken<Quaterniond> token = TypeToken.of(Quaterniond.class);

            @Override
            public TypeToken<Quaterniond> getToken() {
                return this.token;
            }

            @Override
            public Quaterniond translate(DataMap view) throws InvalidDataException {
                final double x = view.getDouble(DataQueries.X_POS).orElseThrow(invalidDataQuery(DataQueries.X_POS));
                final double y = view.getDouble(DataQueries.Y_POS).orElseThrow(invalidDataQuery(DataQueries.Y_POS));
                final double z = view.getDouble(DataQueries.Z_POS).orElseThrow(invalidDataQuery(DataQueries.Z_POS));
                final double w = view.getDouble(DataQueries.W_POS).orElseThrow(invalidDataQuery(DataQueries.W_POS));
                return new Quaterniond(x, y, z, w);
            }

            @Override
            public DataMap translate(Quaterniond obj, DataMap view) {
                return view.set(DataQueries.X_POS, obj.getX())
                    .set(DataQueries.Y_POS, obj.getY())
                    .set(DataQueries.Z_POS, obj.getZ())
                    .set(DataQueries.W_POS, obj.getW());
            }

            @Override
            public String getId() {
                return "sponge:quaterniond";
            }

            @Override
            public String getName() {
                return "QuaternionDTranslator";
            }
        };
        QUATERNIONF_DATA_SERIALIZER = new DataTranslator<Quaternionf>() {

            final TypeToken<Quaternionf> token = TypeToken.of(Quaternionf.class);

            @Override
            public TypeToken<Quaternionf> getToken() {
                return this.token;
            }

            @Override
            public Quaternionf translate(DataMap view) throws InvalidDataException {
                final double x = view.getDouble(DataQueries.X_POS).orElseThrow(invalidDataQuery(DataQueries.X_POS));
                final double y = view.getDouble(DataQueries.Y_POS).orElseThrow(invalidDataQuery(DataQueries.Y_POS));
                final double z = view.getDouble(DataQueries.Z_POS).orElseThrow(invalidDataQuery(DataQueries.Z_POS));
                final double w = view.getDouble(DataQueries.W_POS).orElseThrow(invalidDataQuery(DataQueries.W_POS));
                return new Quaternionf(x, y, z, w);
            }

            @Override
            public DataMap translate(Quaternionf obj, DataMap view) {
                return view.set(DataQueries.X_POS, obj.getX())
                    .set(DataQueries.Y_POS, obj.getY())
                    .set(DataQueries.Z_POS, obj.getZ())
                    .set(DataQueries.W_POS, obj.getW());
            }

            @Override
            public String getId() {
                return "sponge:quaternionf";
            }

            @Override
            public String getName() {
                return "QuaternionFTranslator";
            }
        };
        LOCAL_TIME_DATA_SERIALIZER = new DataTranslator<LocalTime>() {

            final TypeToken<LocalTime> token = TypeToken.of(LocalTime.class);

            @Override
            public TypeToken<LocalTime> getToken() {
                return this.token;
            }

            @Override
            public LocalTime translate(DataMap view) throws InvalidDataException {
                final int hour = view.getInt(DataQueries.LOCAL_TIME_HOUR).orElseThrow(invalidDataQuery(DataQueries.LOCAL_TIME_HOUR));
                final int minute = view.getInt(DataQueries.LOCAL_TIME_MINUTE).orElseThrow(invalidDataQuery(DataQueries.LOCAL_TIME_MINUTE));
                final int second = view.getInt(DataQueries.LOCAL_TIME_SECOND).orElseThrow(invalidDataQuery(DataQueries.LOCAL_TIME_SECOND));
                final int nano = view.getInt(DataQueries.LOCAL_TIME_NANO).orElseThrow(invalidDataQuery(DataQueries.LOCAL_TIME_NANO));
                if (!ChronoField.HOUR_OF_DAY.range().isValidValue(hour)) {
                    throw new InvalidDataException("Invalid hour of day: " + hour);
                }
                if (!ChronoField.MINUTE_OF_HOUR.range().isValidValue(minute)) {
                    throw new InvalidDataException("Invalid minute of hour: " + minute);
                }
                if (!ChronoField.SECOND_OF_MINUTE.range().isValidValue(second)) {
                    throw new InvalidDataException("Invalid second of minute: " + second);
                }
                if (!ChronoField.NANO_OF_SECOND.range().isValidValue(nano)) {
                    throw new InvalidDataException("Invalid nanosecond of second: " + nano);
                }
                return LocalTime.of(hour, minute, second, nano);
            }

            @Override
            public DataMap translate(LocalTime obj, DataMap view) {
                return view.set(DataQueries.LOCAL_TIME_HOUR, obj.getHour())
                    .set(DataQueries.LOCAL_TIME_MINUTE, obj.getMinute())
                    .set(DataQueries.LOCAL_TIME_SECOND, obj.getSecond())
                    .set(DataQueries.LOCAL_TIME_NANO, obj.getNano());
            }

            @Override
            public String getId() {
                return "sponge:local_time";
            }

            @Override
            public String getName() {
                return "LocalTimeTranslator";
            }
        };
        LOCAL_DATE_DATA_SERIALIZER = new DataTranslator<LocalDate>() {

            final TypeToken<LocalDate> token = TypeToken.of(LocalDate.class);

            @Override
            public TypeToken<LocalDate> getToken() {
                return this.token;
            }

            @Override
            public LocalDate translate(DataMap view) throws InvalidDataException {
                final int year = view.getInt(DataQueries.LOCAL_DATE_YEAR).orElseThrow(invalidDataQuery(DataQueries.LOCAL_DATE_YEAR));
                final int month = view.getInt(DataQueries.LOCAL_DATE_MONTH).orElseThrow(invalidDataQuery(DataQueries.LOCAL_DATE_MONTH));
                final int day = view.getInt(DataQueries.LOCAL_DATE_DAY).orElseThrow(invalidDataQuery(DataQueries.LOCAL_DATE_DAY));
                if (!ChronoField.YEAR.range().isValidValue(year)) {
                    throw new InvalidDataException("Invalid year: " + year);
                }
                if (!ChronoField.MONTH_OF_YEAR.range().isValidValue(month)) {
                    throw new InvalidDataException("Invalid month of year: " + month);
                }
                if (!ChronoField.DAY_OF_MONTH.range().isValidValue(day)) {
                    throw new InvalidDataException("Invalid day of month: " + day);
                }
                return LocalDate.of(year, month, day);
            }

            @Override
            public DataMap translate(LocalDate obj, DataMap view) {
                return view.set(DataQueries.LOCAL_DATE_YEAR, obj.getYear())
                    .set(DataQueries.LOCAL_DATE_MONTH, obj.getMonth())
                    .set(DataQueries.LOCAL_DATE_DAY, obj.getDayOfMonth());
            }

            @Override
            public String getId() {
                return "sponge:local_date";
            }

            @Override
            public String getName() {
                return "LocalDateTranslator";
            }
        };
        LOCAL_DATE_TIME_DATA_SERIALIZER = new DataTranslator<LocalDateTime>() {

            final TypeToken<LocalDateTime> token = TypeToken.of(LocalDateTime.class);

            @Override
            public TypeToken<LocalDateTime> getToken() {
                return this.token;
            }

            @Override
            public LocalDateTime translate(DataMap view) throws InvalidDataException {
                final int year = view.getInt(DataQueries.LOCAL_DATE_YEAR).orElseThrow(invalidDataQuery(DataQueries.LOCAL_DATE_YEAR));
                final int month = view.getInt(DataQueries.LOCAL_DATE_MONTH).orElseThrow(invalidDataQuery(DataQueries.LOCAL_DATE_MONTH));
                final int day = view.getInt(DataQueries.LOCAL_DATE_DAY).orElseThrow(invalidDataQuery(DataQueries.LOCAL_DATE_DAY));
                final int hour = view.getInt(DataQueries.LOCAL_TIME_HOUR).orElseThrow(invalidDataQuery(DataQueries.LOCAL_TIME_HOUR));
                final int minute = view.getInt(DataQueries.LOCAL_TIME_MINUTE).orElseThrow(invalidDataQuery(DataQueries.LOCAL_TIME_MINUTE));
                final int second = view.getInt(DataQueries.LOCAL_TIME_SECOND).orElseThrow(invalidDataQuery(DataQueries.LOCAL_TIME_SECOND));
                final int nano = view.getInt(DataQueries.LOCAL_TIME_NANO).orElseThrow(invalidDataQuery(DataQueries.LOCAL_TIME_NANO));
                if (!ChronoField.YEAR.range().isValidValue(year)) {
                    throw new InvalidDataException("Invalid year: " + year);
                }
                if (!ChronoField.MONTH_OF_YEAR.range().isValidValue(month)) {
                    throw new InvalidDataException("Invalid month of year: " + month);
                }
                if (!ChronoField.DAY_OF_MONTH.range().isValidValue(day)) {
                    throw new InvalidDataException("Invalid day of month: " + day);
                }
                if (!ChronoField.HOUR_OF_DAY.range().isValidValue(hour)) {
                    throw new InvalidDataException("Invalid hour of day: " + hour);
                }
                if (!ChronoField.MINUTE_OF_HOUR.range().isValidValue(minute)) {
                    throw new InvalidDataException("Invalid minute of hour: " + minute);
                }
                if (!ChronoField.SECOND_OF_MINUTE.range().isValidValue(second)) {
                    throw new InvalidDataException("Invalid second of minute: " + second);
                }
                if (!ChronoField.NANO_OF_SECOND.range().isValidValue(nano)) {
                    throw new InvalidDataException("Invalid nanosecond of second: " + nano);
                }
                return LocalDateTime.of(year, month, day, hour, minute, second, nano);
            }

            @Override
            public DataMap translate(LocalDateTime obj, DataMap view) {
                return view.set(DataQueries.LOCAL_DATE_YEAR, obj.getYear())
                    .set(DataQueries.LOCAL_DATE_MONTH, obj.getMonth())
                    .set(DataQueries.LOCAL_DATE_DAY, obj.getDayOfMonth())
                    .set(DataQueries.LOCAL_TIME_HOUR, obj.getHour())
                    .set(DataQueries.LOCAL_TIME_MINUTE, obj.getMinute())
                    .set(DataQueries.LOCAL_TIME_SECOND, obj.getSecond())
                    .set(DataQueries.LOCAL_TIME_NANO, obj.getNano());
            }

            @Override
            public String getId() {
                return "sponge:local_date_time";
            }

            @Override
            public String getName() {
                return "LocalDateTimeTranslator";
            }
        };
        ZONED_DATE_TIME_DATA_SERIALIZER = new DataTranslator<ZonedDateTime>() {

            final TypeToken<ZonedDateTime> token = TypeToken.of(ZonedDateTime.class);

            @Override
            public TypeToken<ZonedDateTime> getToken() {
                return this.token;
            }

            @Override
            public ZonedDateTime translate(DataMap view) throws InvalidDataException {
                final int year = view.getInt(DataQueries.LOCAL_DATE_YEAR).orElseThrow(invalidDataQuery(DataQueries.LOCAL_DATE_YEAR));
                final int month = view.getInt(DataQueries.LOCAL_DATE_MONTH).orElseThrow(invalidDataQuery(DataQueries.LOCAL_DATE_MONTH));
                final int day = view.getInt(DataQueries.LOCAL_DATE_DAY).orElseThrow(invalidDataQuery(DataQueries.LOCAL_DATE_DAY));
                final int hour = view.getInt(DataQueries.LOCAL_TIME_HOUR).orElseThrow(invalidDataQuery(DataQueries.LOCAL_TIME_HOUR));
                final int minute = view.getInt(DataQueries.LOCAL_TIME_MINUTE).orElseThrow(invalidDataQuery(DataQueries.LOCAL_TIME_MINUTE));
                final int second = view.getInt(DataQueries.LOCAL_TIME_SECOND).orElseThrow(invalidDataQuery(DataQueries.LOCAL_TIME_SECOND));
                final int nano = view.getInt(DataQueries.LOCAL_TIME_NANO).orElseThrow(invalidDataQuery(DataQueries.LOCAL_TIME_NANO));
                final String zoneId = view.getString(DataQueries.ZONE_TIME_ID).orElseThrow(invalidDataQuery(DataQueries.ZONE_TIME_ID));
                if (!ChronoField.YEAR.range().isValidValue(year)) {
                    throw new InvalidDataException("Invalid year: " + year);
                }
                if (!ChronoField.MONTH_OF_YEAR.range().isValidValue(month)) {
                    throw new InvalidDataException("Invalid month of year: " + month);
                }
                if (!ChronoField.DAY_OF_MONTH.range().isValidValue(day)) {
                    throw new InvalidDataException("Invalid day of month: " + day);
                }
                if (!ChronoField.HOUR_OF_DAY.range().isValidValue(hour)) {
                    throw new InvalidDataException("Invalid hour of day: " + hour);
                }
                if (!ChronoField.MINUTE_OF_HOUR.range().isValidValue(minute)) {
                    throw new InvalidDataException("Invalid minute of hour: " + minute);
                }
                if (!ChronoField.SECOND_OF_MINUTE.range().isValidValue(second)) {
                    throw new InvalidDataException("Invalid second of minute: " + second);
                }
                if (!ChronoField.NANO_OF_SECOND.range().isValidValue(nano)) {
                    throw new InvalidDataException("Invalid nanosecond of second: " + nano);
                }
                if (!ZoneId.getAvailableZoneIds().contains(zoneId)) {
                    throw new InvalidDataException("Unrecognized ZoneId: " + zoneId);
                }
                return ZonedDateTime.of(LocalDate.of(year, month, day), LocalTime.of(hour, minute, second, nano), ZoneId.of(zoneId));
            }

            @Override
            public DataMap translate(ZonedDateTime obj, DataMap view) {
                return view.set(DataQueries.LOCAL_DATE_YEAR, obj.getYear())
                    .set(DataQueries.LOCAL_DATE_MONTH, obj.getMonth())
                    .set(DataQueries.LOCAL_DATE_DAY, obj.getDayOfMonth())
                    .set(DataQueries.LOCAL_TIME_HOUR, obj.getHour())
                    .set(DataQueries.LOCAL_TIME_MINUTE, obj.getMinute())
                    .set(DataQueries.LOCAL_TIME_SECOND, obj.getSecond())
                    .set(DataQueries.LOCAL_TIME_NANO, obj.getNano())
                    .set(DataQueries.ZONE_TIME_ID, obj.getZone().getId());
            }

            @Override
            public String getId() {
                return "sponge:zoned_date_time";
            }

            @Override
            public String getName() {
                return "ZonedDateTimeTranslator";
            }
        };
        INSTANT_DATA_SERIALIZER = new DataTranslator<Instant>() {

            final TypeToken<Instant> token = TypeToken.of(Instant.class);

            @Override
            public TypeToken<Instant> getToken() {
                return this.token;
            }

            @Override
            public Instant translate(DataMap view) throws InvalidDataException {
                final int year = view.getInt(DataQueries.LOCAL_DATE_YEAR).orElseThrow(invalidDataQuery(DataQueries.LOCAL_DATE_YEAR));
                final int month = view.getInt(DataQueries.LOCAL_DATE_MONTH).orElseThrow(invalidDataQuery(DataQueries.LOCAL_DATE_MONTH));
                final int day = view.getInt(DataQueries.LOCAL_DATE_DAY).orElseThrow(invalidDataQuery(DataQueries.LOCAL_DATE_DAY));
                final int hour = view.getInt(DataQueries.LOCAL_TIME_HOUR).orElseThrow(invalidDataQuery(DataQueries.LOCAL_TIME_HOUR));
                final int minute = view.getInt(DataQueries.LOCAL_TIME_MINUTE).orElseThrow(invalidDataQuery(DataQueries.LOCAL_TIME_MINUTE));
                final int second = view.getInt(DataQueries.LOCAL_TIME_SECOND).orElseThrow(invalidDataQuery(DataQueries.LOCAL_TIME_SECOND));
                final int nano = view.getInt(DataQueries.LOCAL_TIME_NANO).orElseThrow(invalidDataQuery(DataQueries.LOCAL_TIME_NANO));
                if (!ChronoField.YEAR.range().isValidValue(year)) {
                    throw new InvalidDataException("Invalid year: " + year);
                }
                if (!ChronoField.MONTH_OF_YEAR.range().isValidValue(month)) {
                    throw new InvalidDataException("Invalid month of year: " + month);
                }
                if (!ChronoField.DAY_OF_MONTH.range().isValidValue(day)) {
                    throw new InvalidDataException("Invalid day of month: " + day);
                }
                if (!ChronoField.HOUR_OF_DAY.range().isValidValue(hour)) {
                    throw new InvalidDataException("Invalid hour of day: " + hour);
                }
                if (!ChronoField.MINUTE_OF_HOUR.range().isValidValue(minute)) {
                    throw new InvalidDataException("Invalid minute of hour: " + minute);
                }
                if (!ChronoField.SECOND_OF_MINUTE.range().isValidValue(second)) {
                    throw new InvalidDataException("Invalid second of minute: " + second);
                }
                if (!ChronoField.NANO_OF_SECOND.range().isValidValue(nano)) {
                    throw new InvalidDataException("Invalid nanosecond of second: " + nano);
                }
                return LocalDateTime.of(year, month, day, hour, minute, second, nano).toInstant(ZoneOffset.UTC);
            }

            @Override
            public DataMap translate(Instant obj, DataMap view) {
                final LocalDateTime local = obj.atZone(ZoneOffset.UTC).toLocalDateTime();
                return view.set(DataQueries.LOCAL_DATE_YEAR, local.getYear())
                    .set(DataQueries.LOCAL_DATE_MONTH, local.getMonth())
                    .set(DataQueries.LOCAL_DATE_DAY, local.getDayOfMonth())
                    .set(DataQueries.LOCAL_TIME_HOUR, local.getHour())
                    .set(DataQueries.LOCAL_TIME_MINUTE, local.getMinute())
                    .set(DataQueries.LOCAL_TIME_SECOND, local.getSecond())
                    .set(DataQueries.LOCAL_TIME_NANO, local.getNano());
            }

            @Override
            public String getId() {
                return "sponge:instant";
            }

            @Override
            public String getName() {
                return "JavaInstantTranslator";
            }
        };

    }

    static Supplier<InvalidDataException> invalidDataQuery(DataQuery query) {
        return () -> {
            throw new InvalidDataException("Invalid data located at: " + query.toString());
        };
    }

    public static void registerSerializers(DataManager dataManager) {
        dataManager.registerTranslator(UUID.class, UUID_DATA_SERIALIZER);
        dataManager.registerTranslator(Vector2d.class, VECTOR_2_D_DATA_SERIALIZER);
        dataManager.registerTranslator(Vector2f.class, VECTOR_2_F_DATA_SERIALIZER);
        dataManager.registerTranslator(Vector2i.class, VECTOR_2_I_DATA_SERIALIZER);
        dataManager.registerTranslator(Vector2l.class, VECTOR_2_L_DATA_SERIALIZER);
        dataManager.registerTranslator(Vector3d.class, VECTOR_3_D_DATA_SERIALIZER);
        dataManager.registerTranslator(Vector3f.class, VECTOR_3_F_DATA_SERIALIZER);
        dataManager.registerTranslator(Vector3i.class, VECTOR_3_I_DATA_SERIALIZER);
        dataManager.registerTranslator(Vector3l.class, VECTOR_3_L_DATA_SERIALIZER);
        dataManager.registerTranslator(Vector4d.class, VECTOR_4_D_DATA_SERIALIZER);
        dataManager.registerTranslator(Vector4f.class, VECTOR_4_F_DATA_SERIALIZER);
        dataManager.registerTranslator(Vector4i.class, VECTOR_4_I_DATA_SERIALIZER);
        dataManager.registerTranslator(Vector4l.class, VECTOR_4_L_DATA_SERIALIZER);
        dataManager.registerTranslator(Complexd.class, COMPLEXD_DATA_SERIALIZER);
        dataManager.registerTranslator(Complexf.class, COMPLEXF_DATA_SERIALIZER);
        dataManager.registerTranslator(Quaterniond.class, QUATERNIOND_DATA_SERIALIZER);
        dataManager.registerTranslator(Quaternionf.class, QUATERNIONF_DATA_SERIALIZER);
        dataManager.registerTranslator(LocalTime.class, LOCAL_TIME_DATA_SERIALIZER);
        dataManager.registerTranslator(LocalDate.class, LOCAL_DATE_DATA_SERIALIZER);
        dataManager.registerTranslator(LocalDateTime.class, LOCAL_DATE_TIME_DATA_SERIALIZER);
        dataManager.registerTranslator(ZonedDateTime.class, ZONED_DATE_TIME_DATA_SERIALIZER);
        dataManager.registerTranslator(Instant.class, INSTANT_DATA_SERIALIZER);
    }

}
