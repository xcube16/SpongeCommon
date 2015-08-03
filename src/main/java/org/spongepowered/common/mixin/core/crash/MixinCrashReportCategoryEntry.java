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

import net.minecraft.crash.CrashReportCategory;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.interfaces.crash.IMixinCrashReportCategoryEntry;

import javax.annotation.Nullable;

@Mixin(CrashReportCategory.Entry.class)
public abstract class MixinCrashReportCategoryEntry implements IMixinCrashReportCategoryEntry {
    @Shadow
    private String key;
    @Shadow
    private String value;

    private Text textKey;
    private Text textValue;
    private String codeValue;
    private String codeLanguage;

    @Override
    public void setKey(Text key) {
        this.textKey = key;
    }

    @Override
    public void setValue(Text value) {
        this.textValue = value;
    }

    @Nullable
    @Override
    public Text getKey() {
        if (this.textKey == null) {
            if (this.key != null) {
                this.textKey = Texts.of(this.key);
            }
        }
        return this.textKey;
    }

    @Nullable
    @Override
    public Text getValue() {
        if (this.textValue == null) {
            if (this.value != null) {
                this.textValue = Texts.of(this.value);
            }
        }
        return this.textValue;
    }

    @Override
    public String getCodeValue() {
        return this.codeValue;
    }

    @Override
    public String getCodeLanguage() {
        return this.codeLanguage;
    }

    @Override
    public void setCodeValue(String codeValue) {
        this.codeValue = codeValue;
    }

    @Override
    public void setCodeLanguage(String codeLanguage) {
        this.codeLanguage = codeLanguage;
    }
}
