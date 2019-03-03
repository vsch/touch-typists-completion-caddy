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
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.BaseComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.vladsch.plugin.util.DelayedRunner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class PluginProject implements BaseComponent, Disposable, PropertyChangeListener {
    private static final Logger LOG = Logger.getInstance("com.vladsch.plugins.touchTypistsCompletionCaddy");

    private final @NotNull Project myProject;
    private final @NotNull DelayedRunner myDelayedRunner;
    private @Nullable CompletionParameters myCompletionParameters;
    private @Nullable ApplicationSettings mySettings;
    private boolean myUserItemSelection;

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
    }

    @Nullable
    public CompletionParameters getCompletionParameters() {
        return myCompletionParameters;
    }

    public void setCompletionParameters(@Nullable final CompletionParameters completionParameters) {
        myCompletionParameters = completionParameters;
    }

    public boolean isUserItemSelection() {
        return myUserItemSelection;
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if (mySettings != null && mySettings.isDisableAutoPopupCompletionsOnSpace()) {
            if (evt.getPropertyName().equals(LookupManager.PROP_ACTIVE_LOOKUP)) {
                Object newValue = evt.getNewValue();
                if (newValue instanceof LookupImpl) {
                    final LookupImpl lookup = (LookupImpl) newValue;
                    myUserItemSelection = false;
                    
                    lookup.addLookupListener(new LookupListener() {
                        long uiRefreshedAt = 0;
                        boolean currentItemChanged = false;

                        @Override
                        public void lookupShown(@NotNull final LookupEvent event) {
                            uiRefreshedAt = System.currentTimeMillis();
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
                            long now = System.currentTimeMillis();
                            long sinceUiRefresh = now - uiRefreshedAt;
                            if (LOG.isDebugEnabled()) LOG.debug("Current item changed, " + sinceUiRefresh + " since ui refresh");
                            currentItemChanged = true;
                            myUserItemSelection = sinceUiRefresh > 250;  // more than 250 ms assume it is the user
                        }

                        @Override
                        public void uiRefreshed() {
                            if (currentItemChanged) {
                                currentItemChanged = false;
                            } else {
                                uiRefreshedAt = System.currentTimeMillis();
                                if (LOG.isDebugEnabled()) LOG.debug("uiRefreshed at " + uiRefreshedAt);
                            }
                        }

                        @Override
                        public void focusDegreeChanged() {
                            if (LOG.isDebugEnabled()) LOG.debug("focusDegreeChanged");
                        }

                        private void lookupClosed() {
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
