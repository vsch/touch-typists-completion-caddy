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

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.codeInsight.lookup.LookupEvent;
import com.intellij.codeInsight.lookup.LookupListener;
import com.intellij.codeInsight.lookup.LookupManagerListener;
import com.intellij.codeInsight.lookup.impl.LookupImpl;
import com.intellij.ide.IdeEventQueue;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.vladsch.plugin.util.DelayedRunner;
import com.vladsch.plugin.util.LazyFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.KeyEvent;

import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_UP;

public class PluginProject implements Disposable, LookupManagerListener {
    public static final String PLUGIN_ID = "com.vladsch.plugins.touchTypistsCompletionCaddy";
    private static final Logger LOG = Logger.getInstance(PLUGIN_ID);
    public static final String COMPLETION_CHARS = " \t\r\n";
    final private static LazyFunction<Project, PluginProject> DEFAULT = new LazyFunction<>(PluginProject::new);

    public static PluginProject getInstance(@NotNull Project project) {
        if (project.isDefault()) return DEFAULT.getValue(project);
        return project.getService(PluginProject.class);
    }

    private final @NotNull DelayedRunner myDelayedRunner;
    private @Nullable CompletionParameters myCompletionParameters;
    private @Nullable ApplicationSettings mySettings;
    private boolean myUserItemSelection;
    private boolean myLookupShown = false;
    private boolean myIsAutoCompletion = false;

    public PluginProject(@NotNull Project project) {
        myDelayedRunner = new DelayedRunner();
        myUserItemSelection = false;

        mySettings = ApplicationSettings.getInstance();

        project.getMessageBus().connect(this).subscribe(LookupManagerListener.TOPIC, this);

        final IdeEventQueue.EventDispatcher eventDispatcher = e -> {
            // we are only tracking it
            if (myLookupShown && e instanceof KeyEvent && e.getID() == KeyEvent.KEY_PRESSED) {
                int keyCode = ((KeyEvent) e).getKeyCode();
                if (keyCode == VK_UP || keyCode == VK_DOWN) {
                    if (LOG.isDebugEnabled()) LOG.debug("Up/Down 0x" + Integer.toString(keyCode, 16));
                    myUserItemSelection = true;
                } else {
                    char keyChar = ((KeyEvent) e).getKeyChar();
                    if (COMPLETION_CHARS.indexOf(keyChar) == -1) {
                        if (LOG.isDebugEnabled()) LOG.debug("Other keyCode 0x" + Integer.toString(keyCode, 16));
                        myUserItemSelection = false;
                    }
                }
            }
            return false;
        };

        IdeEventQueue.getInstance().addDispatcher(eventDispatcher, this);
        myDelayedRunner.addRunnable(() -> IdeEventQueue.getInstance().removeDispatcher(eventDispatcher));
    }

    @SuppressWarnings("unused")
    @Nullable
    public CompletionParameters getCompletionParameters() {
        return myCompletionParameters;
    }

    public void setCompletionParameters(@Nullable final CompletionParameters completionParameters) {
        myCompletionParameters = completionParameters;
        myIsAutoCompletion = completionParameters != null && completionParameters.isAutoPopup();
    }

    public static IdeaPluginDescriptor getPluginDescriptor() {
        IdeaPluginDescriptor[] plugins = PluginManager.getPlugins();
        for (IdeaPluginDescriptor plugin : plugins) {
            if (PLUGIN_ID.equals(plugin.getPluginId().getIdString())) {
                return plugin;
            }
        }

        throw new IllegalStateException("Unexpected, plugin cannot find its own plugin descriptor");
    }

    public static String fullProductVersion() {
        IdeaPluginDescriptor pluginDescriptor = getPluginDescriptor();
        return pluginDescriptor.getVersion();
    }

    @Override
    public void dispose() {
        myDelayedRunner.runAll();
        mySettings = null;
    }

    public boolean isAutoPopup() {
        return myIsAutoCompletion;
    }

    public void setAutoPopup(final boolean popupCompletion) {
        myIsAutoCompletion = popupCompletion;
    }

    public boolean isUserItemSelection() {
        return myUserItemSelection;
    }

    @Override
    public void activeLookupChanged(@Nullable Lookup oldLookup, @Nullable Lookup newLookup) {
        if (mySettings != null && mySettings.isDisableAutoPopupCompletionsOnSpace()) {
            if (newLookup instanceof LookupImpl) {
                final LookupImpl lookup = (LookupImpl) newLookup;
                myUserItemSelection = false;

                lookup.addLookupListener(new LookupListener() {
                    @Override
                    public void lookupShown(@NotNull final LookupEvent event) {
                        myLookupShown = true;
                    }

                    @Override
                    public boolean beforeItemSelected(@NotNull final LookupEvent event) {
                        if (LOG.isDebugEnabled()) LOG.debug("beforeItemSelected");
                        return true;
                    }

                    @Override
                    public void itemSelected(@NotNull final LookupEvent event) {
                        if (LOG.isDebugEnabled()) LOG.debug("itemSelected");
                        lookupClosed();
                    }

                    @Override
                    public void lookupCanceled(@NotNull final LookupEvent event) {
                        if (LOG.isDebugEnabled()) LOG.debug("lookupCancelled");
                        lookupClosed();
                    }

                    private void lookupClosed() {
                        myLookupShown = false;
                        myCompletionParameters = null;
                        ApplicationManager.getApplication().assertIsDispatchThread();
                        lookup.removeLookupListener(this);
                    }
                });
            }
        } else {
            myCompletionParameters = null;
        }
    }
}
