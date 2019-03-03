/*
 * Copyright (c) 2016-2018 Vladimir Schneider <vladimir.schneider@gmail.com>
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.vladsch.plugins.touchTypistsCompletionCaddy;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.BaseComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.vladsch.plugin.util.DelayedRunner;
import com.vladsch.plugin.util.HelpersKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class Plugin implements BaseComponent, Disposable {
    private static final Logger LOG = Logger.getInstance("com.vladsch.plugins.touchTypistsCompletionCaddy");

    final protected @NotNull DelayedRunner myDelayedRunner;

    public Plugin() {
        myDelayedRunner = new DelayedRunner();
    }

    @Override
    public void dispose() {
        myDelayedRunner.runAll();
    }

    @Override
    public void initComponent() {

    }

    @Nullable
    public static Editor getEditorEx(final @Nullable FileEditor fileEditor) {
        if (fileEditor != null) {
            if (fileEditor instanceof TextEditor) {
                Editor editor = ((TextEditor) fileEditor).getEditor();
                return editor;
            }
        }
        return null;
    }

    static String getStringContent(Transferable content) {
        if (content != null) {
            try {
                return (String) content.getTransferData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException | IOException ignore) { }
        }
        return null;
    }

    static boolean isBlank(CharSequence text) {
        final int iMax = text.length();
        for (int i = 0; i < iMax; i++) {
            char c = text.charAt(i);
            if (c != ' ' && c != '\t' && c != '\n') return false;
        }
        return true;
    }

    @Override
    public void disposeComponent() {
        myDelayedRunner.runAll();
    }

    @NotNull
    @Override
    public String getComponentName() {
        return this.getClass().getName();
    }

    public static Plugin getInstance() {
        return ApplicationManager.getApplication().getComponent(Plugin.class);
    }

    public static String getProductId() {
        return Bundle.message("plugin.product-id");
    }

    private static String getProductDisplayName() {
        return Bundle.message("plugin.name");
    }

    private static final String PLUGIN_ID = "com.vladsch.plugins.touchTypistsCompletionCaddy";

    public static IdeaPluginDescriptor getPluginDescriptor() {
        IdeaPluginDescriptor[] plugins = PluginManager.getPlugins();
        for (IdeaPluginDescriptor plugin : plugins) {
            if (PLUGIN_ID.equals(plugin.getPluginId().getIdString())) {
                return plugin;
            }
        }

        throw new IllegalStateException("Unexpected, plugin cannot find its own plugin descriptor");
    }

    public static String productVersion() {
        IdeaPluginDescriptor pluginDescriptor = getPluginDescriptor();
        String version = pluginDescriptor.getVersion();
        // truncate version to 3 digits and if had more than 3 append .x, that way
        // no separate product versions need to be created
        String[] parts = version.split("\\.", 4);
        if (parts.length <= 3) {
            return version;
        }

        String sep = "";
        StringBuilder newVersion = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            newVersion.append(sep);
            sep = ".";
            newVersion.append(parts[i]);
        }
        newVersion.append(".x");
        return newVersion.toString();
    }

    public static String fullProductVersion() {
        IdeaPluginDescriptor pluginDescriptor = getPluginDescriptor();
        return pluginDescriptor.getVersion();
    }

    @Nullable
    public static String getPluginCustomPath() {
        String[] variants = { PathManager.getHomePath(), PathManager.getPluginsPath() };

        for (String variant : variants) {
            String path = variant + "/" + getProductId();
            if (LocalFileSystem.getInstance().findFileByPath(path) != null) {
                return path;
            }
        }
        return null;
    }

    @Nullable
    public static String getPluginPath() {
        String[] variants = { PathManager.getPluginsPath() };

        for (String variant : variants) {
            String path = variant + "/" + getProductId();
            if (LocalFileSystem.getInstance().findFileByPath(path) != null) {
                return path;
            }
        }
        return null;
    }

    @Nullable
    public static String getPluginFilePath(String fileName) {
        String path = getPluginCustomPath();
        return path == null ? null : HelpersKt.suffixWith(path, '/') + fileName;
    }
}
