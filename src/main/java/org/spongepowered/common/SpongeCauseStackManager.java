/*
 * Copyright (c) 2015-2016 VoxelBox <http://engine.thevoxelbox.com>.
 * All Rights Reserved.
 */
package org.spongepowered.common;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;

import java.util.Deque;
import java.util.Map;
import java.util.Optional;

public class SpongeCauseStackManager implements CauseStackManager {

    public static final SpongeCauseStackManager instance = new SpongeCauseStackManager();

    private final Deque<Object> cause = Queues.newArrayDeque();
    private final Deque<CauseStackFrame> frames = Queues.newArrayDeque();
    private Map<String, Object> ctx = Maps.newHashMap();

    private int min_depth = 0;
    private Cause cached_cause;
    private EventContext cached_ctx;

    private SpongeCauseStackManager() {

    }

    @Override
    public Cause getCurrentCause() {
        if (this.cached_cause == null) {
            this.cached_cause = Cause.of(getCurrentContext(), this.cause);
        }
        return this.cached_cause;
    }

    @Override
    public EventContext getCurrentContext() {
        if (this.cached_ctx == null) {
            this.cached_ctx = EventContext.of(this.ctx);
        }
        return this.cached_ctx;
    }

    @Override
    public void pushCause(Object obj) {
        this.cached_cause = null;
        this.cause.push(obj);
    }

    @Override
    public Object popCause() {
        if (this.cause.size() == this.min_depth) {
            throw new IllegalStateException("Cause stack corruption, tried to pop more objects off than were pushed since last frame.");
        }
        this.cached_cause = null;
        return this.cause.pop();
    }

    @Override
    public CauseStackFrame pushCauseFrame() {
        CauseStackFrame frame = new CauseStackFrame(this.min_depth, this.ctx);
        this.frames.push(frame);
        this.ctx = Maps.newHashMap(this.ctx);
        this.min_depth = this.cause.size();
        return frame;
    }

    @Override
    public void popCauseFrame(Object oldFrame) {
        CauseStackFrame frame = this.frames.pop();
        if (frame != oldFrame) {
            throw new IllegalStateException("Cause stack frame corruption! Attempted to pop a frame which was not the head of the frame stack.");
        }
        this.ctx = frame.stored_ctx;
        this.min_depth = frame.old_min_depth;
    }

    @Override
    public AutoCloseable createCauseFrame() {
        return pushCauseFrame();
    }

    @Override
    public void addContext(String key, Object value) {
        this.cached_ctx = null;
        this.ctx.put(key, value);
    }

    @Override
    public Optional<?> clearContext(String key) {
        this.cached_ctx = null;
        return Optional.ofNullable(this.ctx.remove(key));
    }

    private static class CauseStackFrame implements AutoCloseable {

        public final Map<String, Object> stored_ctx;
        public int old_min_depth;

        public CauseStackFrame(int old_depth, Map<String, Object> ctx) {
            this.stored_ctx = ctx;
            this.old_min_depth = old_depth;
        }

        @Override
        public void close() throws Exception {
            SpongeCauseStackManager.instance.popCauseFrame(this);
        }

    }

}
