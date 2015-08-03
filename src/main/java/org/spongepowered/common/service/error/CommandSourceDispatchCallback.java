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
package org.spongepowered.common.service.error;

import static org.spongepowered.api.util.command.CommandMessageFormatting.error;
import static org.spongepowered.common.util.SpongeCommonTranslationHelper.t;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.api.service.error.ErrorReport;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.common.Sponge;

import java.net.URL;
import java.util.function.BiFunction;

public class CommandSourceDispatchCallback implements BiFunction<URL, Throwable, URL> {

    private final ErrorReport report;
    private final CommandSource source;

    public CommandSourceDispatchCallback(ErrorReport report, CommandSource source) {
        this.report = report;
        this.source = source;
    }

    @Override
    public URL apply(URL url, Throwable throwable) {
        if (throwable == null) {
            final CommandSource console = (CommandSource) MinecraftServer.getServer();
            Text linkText = Texts.builder(url.toString()).onClick(TextActions.openUrl(url)).style(TextStyles.UNDERLINE).build();
            if (source != console) {
                source.sendMessage(error(t("Unfortunately an error occurred (%s). See more information at %s or in the console!",
                        this.report.getCauseDescription(), linkText)));
            }
            console.sendMessage(error(t("An error occurred caused by %s with the message %s. See more information at %s", this.source.getName(),
                    this.report.getCauseDescription(), linkText)));

        } else {
            Sponge.getLogger().error("Unable to dispatch error report " + this.report.getCauseDescription() + ", full text is below", throwable);
            Sponge.getLogger().error(this.report.toText());
        }
        return url;
    }
}
