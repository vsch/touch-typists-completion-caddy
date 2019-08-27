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
import com.intellij.codeInsight.lookup.LookupEvent;
import com.intellij.codeInsight.lookup.LookupListener;
import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.codeInsight.lookup.impl.LookupImpl;
import com.intellij.ide.IdeEventQueue;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.BaseComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.vladsch.plugin.util.DelayedRunner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.AWTEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_UP;

public class PluginProject implements BaseComponent, Disposable, PropertyChangeListener {
    private static final Logger LOG = Logger.getInstance("com.vladsch.plugins.touchTypistsCompletionCaddy");
    public static final String COMPLETION_CHARS = " \t\u0009\r\n";

    private final @NotNull Project myProject;
    private final @NotNull DelayedRunner myDelayedRunner;
    private @Nullable CompletionParameters myCompletionParameters;
    private @Nullable ApplicationSettings mySettings;
    private boolean myUserItemSelection;
    private Character myLastCompletionChar = null;
    private boolean myLookupShown = false;
    private boolean myIsAutoCompletion = false;

    public PluginProject(@NotNull Project project) {
        myProject = project;
        myDelayedRunner = new DelayedRunner();
        myUserItemSelection = false;
    }

    @Override
    public void dispose() {
        myDelayedRunner.runAll();
    }

    @Override
    public void initComponent() {
        mySettings = ApplicationSettings.getInstance();

        LookupManager lookupManager = LookupManager.getInstance(myProject);
        lookupManager.addPropertyChangeListener(this, myProject);
        myDelayedRunner.addRunnable(() -> {
            lookupManager.removePropertyChangeListener(this);
        });

        final IdeEventQueue.EventDispatcher eventDispatcher = new IdeEventQueue.EventDispatcher() {
            @Override
            public boolean dispatch(@NotNull final AWTEvent e) {
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
                            myLastCompletionChar = null;
                            myUserItemSelection = false;
                        }
                    }
                }
                return false;
            }
        };

        IdeEventQueue.getInstance().addDispatcher(eventDispatcher, this);
        myDelayedRunner.addRunnable(() -> {
            IdeEventQueue.getInstance().removeDispatcher(eventDispatcher);
        });
    }

    @Nullable
    public CompletionParameters getCompletionParameters() {
        return myCompletionParameters;
    }

    public void setCompletionParameters(@Nullable final CompletionParameters completionParameters) {
        myCompletionParameters = completionParameters;
        myIsAutoCompletion = completionParameters != null && completionParameters.isAutoPopup();
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

    public Character getLastCompletionChar() {
        return myLastCompletionChar;
    }

    public void setLastCompletionChar(final Character lastCompletionChar) {
        if (LOG.isDebugEnabled()) LOG.debug("Char typed " + Character.getName(lastCompletionChar));
        myLastCompletionChar = lastCompletionChar;
        myUserItemSelection = false;
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if (mySettings != null && mySettings.isDisableAutoPopupCompletionsOnSpace()) {
            if (evt.getPropertyName().equals(LookupManager.PROP_ACTIVE_LOOKUP)) {
                Object newValue = evt.getNewValue();
                if (newValue instanceof LookupImpl) {
                    final LookupImpl lookup = (LookupImpl) newValue;
                    myUserItemSelection = false;
                    myLastCompletionChar = null;

                    lookup.addLookupListener(new LookupListener() {
                        boolean currentItemChanged = false;

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

                        @Override
                        public void currentItemChanged(@NotNull final LookupEvent event) {

                        }

                        @Override
                        public void uiRefreshed() {

                        }

                        @Override
                        public void focusDegreeChanged() {

                        }

                        private void lookupClosed() {
                            myLookupShown = false;
                            myCompletionParameters = null;
                            ApplicationManager.getApplication().assertIsDispatchThread();
                            lookup.removeLookupListener(this);
                        }
                    });
                }
            }
        } else {
            myCompletionParameters = null;
        }
    }

    @Override
    public void disposeComponent() {
        mySettings = null;
        myDelayedRunner.runAll();
    }

    @NotNull
    @Override
    public String getComponentName() {
        return this.getClass().getName();
    }

    public static PluginProject getInstance(@NotNull Project project) {
        return project.getComponent(PluginProject.class);
    }
}
