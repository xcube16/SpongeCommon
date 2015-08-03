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

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.common.text.SpongeTexts;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

import javax.annotation.Nullable;

public class MarkdownErrorReportFormatter implements ErrorReportFormatter<StringBuilder> {
    public static final ErrorReportFormatter<StringBuilder> INSTANCE = new MarkdownErrorReportFormatter();

    private static final String CODE_BLOCK = "```";
    private static final String BOLD = "**";
    private static final String HEADING_LARGE = "#";
    private static final String HEADING_LARGE_BEGIN = HEADING_LARGE + " ";
    private static final String HEADING_LARGE_END = " " + HEADING_LARGE;
    private static final String HEADING_SMALL = "##";
    private static final String HEADING_SMALL_BEGIN = HEADING_SMALL + " ";
    private static final String HEADING_SMALL_END = " " + HEADING_SMALL;
    private static final String NEWLINE = "\n";
    private static final String KEY_VALUE_SEPARATOR = ":";
    private static final String SPACER = " ";

    @Override
    public StringBuilder createBuffer() {
        return new StringBuilder();
    }

    @Override
    public String bufferToString(StringBuilder stringBuilder) {
        return stringBuilder.toString();
    }

    @Override
    public void appendReportHeading(StringBuilder stringBuilder, String reportHeading) {
        stringBuilder.append(HEADING_LARGE_BEGIN).append(reportHeading).append(HEADING_LARGE_END).append(NEWLINE).append(NEWLINE);
    }

    @Override
    public void appendStacktrace(StringBuilder stringBuilder, Throwable t) {
        final StringWriter writer = new StringWriter();
        t.printStackTrace(new PrintWriter(writer));
        appendCodeSectionEntry(stringBuilder, Texts.of("Stacktrace"), writer.toString(), null);
    }

    @Override
    public void appendSectionHeader(StringBuilder stringBuilder, String header) {
        stringBuilder.append(HEADING_SMALL_BEGIN).append(header).append(HEADING_SMALL_END).append(NEWLINE).append(NEWLINE);
    }

    @Override
    public void appendSectionEntry(StringBuilder stringBuilder, @Nullable Text key, Text value) {
        if (key != null) {
            stringBuilder.append(BOLD).append(textToMarkdownString(key)).append(KEY_VALUE_SEPARATOR).append(BOLD).append(SPACER);
        }
        final String strValue = textToMarkdownString(value);
        if (strValue.contains("\n")) {
            stringBuilder.append(NEWLINE).append(CODE_BLOCK).append(NEWLINE).append(strValue);
            if (!strValue.endsWith(NEWLINE)) {
                stringBuilder.append(NEWLINE);
            }
            stringBuilder.append(CODE_BLOCK).append(NEWLINE).append(NEWLINE);
        } else {
            stringBuilder.append(textToMarkdownString(value)).append(NEWLINE).append(NEWLINE);
        }
    }

    @Override
    public void appendCodeSectionEntry(StringBuilder stringBuilder, @Nullable Text key, String code, @Nullable String language) {
        if (key != null) {
            stringBuilder.append(BOLD).append(textToMarkdownString(key)).append(BOLD).append(NEWLINE);
        }
        stringBuilder.append(CODE_BLOCK);
        if (language != null) {
            stringBuilder.append(language);
        }
        stringBuilder.append(NEWLINE);
        stringBuilder.append(code);
        if (!code.endsWith(NEWLINE)) {
            stringBuilder.append(NEWLINE);
        }
        stringBuilder.append(CODE_BLOCK).append(NEWLINE).append(NEWLINE);
    }

    private String textToMarkdownString(Text text) {
        return SpongeTexts.toPlain(text, Locale.ROOT); // TODO: Handle formatting attributes properly
    }
}
