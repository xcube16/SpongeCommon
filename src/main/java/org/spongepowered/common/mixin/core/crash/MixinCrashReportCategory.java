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
package org.spongepowered.common.mixin.core.crash;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import org.spongepowered.api.service.error.ErrorReport;
import org.spongepowered.api.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.interfaces.crash.IMixinCrashReport;
import org.spongepowered.common.interfaces.crash.IMixinCrashReportCategoryEntry;
import org.spongepowered.common.service.error.ErrorReportFormatter;
import org.spongepowered.common.service.error.MarkdownErrorReportFormatter;

import java.util.List;

@Mixin(CrashReportCategory.class)
public abstract class MixinCrashReportCategory implements ErrorReport.Section {
    @Shadow private CrashReport crashReport;
    @Shadow public abstract void addCrashSection(String sectionName, Object value);

    @Shadow
    @SuppressWarnings("rawtypes")
    private List children;

    @Shadow private String name;
    @Shadow private StackTraceElement[] stackTrace;

    private Throwable exception;

    @Override
    public ErrorReport.Section addEntry(Text content) {
        addEntry(null, content);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ErrorReport.Section addEntry(Text key, Text value) {
        IMixinCrashReportCategoryEntry entry = (IMixinCrashReportCategoryEntry) new CrashReportCategory.Entry(null, null);
        entry.setKey(key);
        entry.setValue(value);
        this.children.add(entry);
        ((IMixinCrashReport) this.crashReport).markDirty();
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ErrorReport.Section addCodeEntry(Text key, String value, String language) {
        IMixinCrashReportCategoryEntry entry = (IMixinCrashReportCategoryEntry) new CrashReportCategory.Entry(null, null);
        entry.setKey(key);
        entry.setCodeValue(value);
        entry.setCodeLanguage(language);
        this.children.add(entry);
        ((IMixinCrashReport) this.crashReport).markDirty();
        return this;
    }

    @Override
    public ErrorReport.Section setException(Throwable t) {
        this.exception = t;
        ((IMixinCrashReport) this.crashReport).markDirty();
        return this;
    }

    @Overwrite
    public void appendToStringBuilder(StringBuilder builder) {
        ErrorReportFormatter<StringBuilder> ret = MarkdownErrorReportFormatter.INSTANCE;

        ret.appendSectionHeader(builder, this.name);

        for (Object child : this.children) {
            IMixinCrashReportCategoryEntry entry = (IMixinCrashReportCategoryEntry) child;
            if (entry.getCodeValue() != null) {
                ret.appendCodeSectionEntry(builder, entry.getKey(), entry.getCodeValue(), entry.getCodeLanguage());
            } else {
                ret.appendSectionEntry(builder, entry.getKey(), entry.getValue());
            }
        }

        Throwable t = getThrowable();
        if (t != null) {
            ret.appendStacktrace(builder, t);
        }
    }

    private Throwable getThrowable() {
        if (this.exception == null && this.stackTrace != null && this.stackTrace.length > 0) {
            this.exception = new Throwable();
            this.exception.setStackTrace(this.stackTrace);
        }
        return this.exception;
    }

    @Override
    public ErrorReport parent() {
        return (ErrorReport) this.crashReport;
    }
}
