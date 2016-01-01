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
package org.spongepowered.common.service.permission.base;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.MemorySubjectData;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.util.Tristate;

import java.util.List;
import java.util.Set;

public abstract class SpongeSubject implements Subject {
    private static final List<Context> GLOBAL_CONTEXT = ImmutableList.of();

    @Override
    public SubjectData getTransientSubjectData() {
        return getSubjectData();
    }

    @Override
    public abstract MemorySubjectData getSubjectData();

    @Override
    public boolean hasPermission(List<Context> contexts, String permission) {
        return getPermissionValue(contexts, permission) == Tristate.TRUE;
    }

    @Override
    public boolean hasPermission(String permission) {
        return hasPermission(getActiveContexts(), permission);
    }

    @Override
    public Tristate getPermissionValue(List<Context> contexts, String permission) {
        return getDataPermissionValue(getSubjectData(), permission);
    }

    protected Tristate getDataPermissionValue(MemorySubjectData subject, String permission) {
        Tristate res = subject.getNodeTree(SubjectData.GLOBAL_CONTEXT).get(permission);

        if (res == Tristate.UNDEFINED) {
            for (Subject parent : subject.getParents(SubjectData.GLOBAL_CONTEXT)) {
                Tristate tempRes = parent.getPermissionValue(GLOBAL_CONTEXT, permission);
                if (tempRes != Tristate.UNDEFINED) {
                    res = tempRes;
                    break;
                }
            }
        }
        return res;
    }

    @Override
    public boolean isChildOf(Subject parent) {
        return isChildOf(getActiveContexts(), parent);
    }

    @Override
    public boolean isChildOf(List<Context> contexts, Subject parent) {
        return getSubjectData().getParents(ImmutableSet.copyOf(contexts)).contains(parent);
    }

    @Override
    public List<Subject> getParents() {
        return getParents(getActiveContexts());
    }

    @Override
    public List<Subject> getParents(List<Context> contexts) {
        return getSubjectData().getParents(ImmutableSet.copyOf(contexts));
    }

    @Override
    public List<Context> getActiveContexts() {
        return GLOBAL_CONTEXT;
    }
}
