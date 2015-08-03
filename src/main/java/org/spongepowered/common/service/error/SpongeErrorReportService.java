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

import net.minecraft.crash.CrashReport;
import org.spongepowered.api.service.error.ErrorReport;
import org.spongepowered.api.service.error.ErrorReportService;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.text.SpongeTexts;

import java.util.Locale;

public class SpongeErrorReportService implements ErrorReportService {
    public static final SpongeErrorReportService INSTANCE = new SpongeErrorReportService();

    @Override
    public ErrorReport createReport(Throwable throwable, Text message) {
        return (ErrorReport) new CrashReport(SpongeTexts.toPlain(message, Locale.getDefault()), throwable);
    }

    @Override
    public ErrorReport createReport(Text message) {
        return createReport(null, message);
    }

    @Override
    public ErrorReport createReport(Throwable throwable) {
        return createReport(throwable, null);
    }
}
