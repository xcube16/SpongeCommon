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

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ReportedException;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.service.error.ErrorReport;
import org.spongepowered.api.service.error.Reportable;
import org.spongepowered.api.service.error.UserErrorException;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.Functional;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.source.LocatedSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.interfaces.crash.IMixinCrashReport;
import org.spongepowered.common.service.error.CommandSourceDispatchCallback;
import org.spongepowered.common.service.error.ErrorReportFormatter;
import org.spongepowered.common.service.error.GistPostingCallable;
import org.spongepowered.common.service.error.MarkdownErrorReportFormatter;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Mixin(CrashReport.class)
public abstract class MixinCrashReport implements ErrorReport, IMixinCrashReport {

    @Shadow private String description;

    @Shadow private Throwable cause;

    @Shadow public abstract CrashReportCategory makeCategory(String name);
    @Shadow private static String getWittyComment() {return null;};
    @Shadow public abstract String getCauseStackTraceOrString();
    @Shadow private CrashReportCategory theReportCategory;
    @Shadow private List crashReportSections;

    private CompletableFuture<URL> pastebinUrl;

    private final Set<Reportable> addedReportables = new HashSet<Reportable>();

    private boolean userError;

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    public void handleInit(String description, Throwable cause, CallbackInfo ci) {
        userError = this.cause instanceof UserErrorException;
    }

    @Override
    public Optional<Throwable> getCause() {
        return Optional.ofNullable(this.cause);
    }

    @Override
    public boolean isUserError() {
        return this.userError;
    }

    @Override
    public ErrorReport setUserError(boolean isUserError) {
        this.userError = isUserError;
        return this;
    }

    @Override
    public String getCauseDescription() {
        if (this.description != null) {
            return this.description;
        } else if (this.cause != null) {
            return this.cause.getMessage();
        } else {
            return "Unknown";
        }

    }

    @Override
    public Section appendSection(String title) {
        pastebinUrl = null;
        return (Section) makeCategory(checkNotNull(title, "title"));
    }

    @Override
    public ErrorReport addReportable(Reportable reportable) {
        checkNotNull(reportable, "reportable");
        if (addedReportables.add(reportable)) {
            reportable.decorateErrorReport(this);
        }
        return this;
    }

    @Override
    public void dispatchFatal() {
        try {
            Sponge.getGame().getEventManager().post(SpongeEventFactory.createErrorReportEvent(this));
        } catch (Throwable t) {
            appendSection("Notifying about report").setException(t);
        }
        throw new ReportedException((CrashReport) (Object) this);
    }

    @Override
    public void dispatchToCommandSource(final CommandSource source) {
        // Add server and world-specific info
        this.addReportable(Sponge.getGame().getServer());
        if (source instanceof LocatedSource) {
            this.addReportable(((LocatedSource) source).getWorld());
        }

        try {
            Sponge.getGame().getEventManager().post(SpongeEventFactory.createErrorReportEvent(this));
        } catch (Throwable t) {
            appendSection("Notifying about report").setException(t);
        }
        toPastebin().handle(new CommandSourceDispatchCallback(this, source));
    }

    @Override
    public void dispatchToConsole() {
        dispatchToCommandSource((CommandSource) MinecraftServer.getServer());
    }

    @Override
    public String toText() {
        return getCompleteReport();
    }

    @Override
    public void markDirty() {
        this.pastebinUrl = null;
    }

    @Override
    public CompletableFuture<URL> toPastebin() {
        CompletableFuture<URL> ret = this.pastebinUrl;
        if (ret == null) {
            this.pastebinUrl = ret = Functional.asyncFailableFuture(new GistPostingCallable(this), null); // TODO
        }
        return ret;
    }

    @Overwrite
    public String getCompleteReport() {
        ErrorReportFormatter<StringBuilder> fmt = MarkdownErrorReportFormatter.INSTANCE;
        StringBuilder buffer = fmt.createBuffer();
        fmt.appendReportHeading(buffer, "Minecraft Crash Report");
        fmt.appendSectionEntry(buffer, null, Texts.of(getWittyComment()));
        buffer.append('\n');
        fmt.appendSectionEntry(buffer, Texts.of("Time"), Texts.of(new SimpleDateFormat().format(new Date())));
        fmt.appendSectionEntry(buffer, Texts.of("Description"), Texts.of(this.description));
        fmt.appendSectionEntry(buffer, null, Texts.of("A detailed walkthrough of the error, its code path and all known details is as follows:"));
        fmt.appendCodeSectionEntry(buffer, Texts.of("Stacktrace"), getCauseStackTraceOrString(), null);

        for (Object o : crashReportSections) {
            ((CrashReportCategory) o).appendToStringBuilder(buffer);
        }
        this.theReportCategory.appendToStringBuilder(buffer);

        return buffer.toString();
    }

}
