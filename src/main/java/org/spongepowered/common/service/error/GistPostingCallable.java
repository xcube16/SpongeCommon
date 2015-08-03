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

import com.google.common.io.ByteSource;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.spongepowered.api.service.error.ErrorReport;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;

public class GistPostingCallable implements Callable<URL> {

    private static final URL GIST_POST_URL;
    static {
        try {
            GIST_POST_URL = new URL("https://api.github.com/gists");
        } catch (MalformedURLException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    private final ErrorReport report;

    public GistPostingCallable(ErrorReport report) {
        this.report = report;
    }

    @Override
    public URL call() throws Exception {
        final String textToShorten = this.report.toText();

        OutputStreamWriter requestWriter = null;
        InputStreamReader responseReader = null;
        try {
            final URLConnection conn = GIST_POST_URL.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);

            JsonWriter writer = new JsonWriter((requestWriter = new OutputStreamWriter(conn.getOutputStream())));
            writer.beginObject()
                    .name("description").value("Sponge Error Report")
                    .name("public").value(false)
                    .name("files").beginObject()
                    .name("report.md").beginObject()
                    .name("content").value(textToShorten)
                    .endObject()
                    .endObject()
                    .endObject();
            writer.flush();

            try {
                JsonReader reader = new JsonReader((responseReader = new InputStreamReader(conn.getInputStream())));
                while (reader.hasNext()) {
                    if (reader.peek() != JsonToken.BEGIN_OBJECT && reader.peek() != JsonToken.NAME) {
                        reader.skipValue();
                    }
                    if (reader.peek() == JsonToken.BEGIN_OBJECT) {
                        reader.beginObject();
                    }

                    if (reader.nextName().equals("html_url")) {
                        return new URL(reader.nextString());
                    }
                }
            } catch (IOException ex) {
                if (conn != null) {
                    System.out.println(new ByteSource() {
                        @Override
                        public InputStream openStream() throws IOException {
                            return ((HttpURLConnection) conn).getErrorStream();
                        }
                    }.asCharSource(Charset.defaultCharset()).read());
                    throw ex;
                }

            }
            throw new IOException("Response did not contain 'html_url' parameter!");
        } finally {
            if (requestWriter != null) {
                requestWriter.close();
            }

            if (responseReader != null) {
                responseReader.close();
            }
        }
    }
}
