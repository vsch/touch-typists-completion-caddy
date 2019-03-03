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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.BaseComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

@State(
        name = "TouchTypistsCompletionCaddy",
        storages = @Storage("touch-typists-completion-caddy-settings.xml")
)
@SuppressWarnings("WeakerAccess")
public class ApplicationSettings implements BaseComponent, PersistentStateComponent<ApplicationSettings> {
    final static String[] EMPTY_STRINGS = new String[0];
    
    private boolean disableAutoPopupCompletionsOnSpace = true;
    private boolean onlyFor = true;
    private String onlyForList = "Kotlin";

    @NotNull
    public static String cleanLanguageList(final String list) {
        String[] languageSet = getLanguageSet(list, false).toArray(EMPTY_STRINGS);
        Arrays.sort(languageSet);
        
        StringBuilder text = new StringBuilder();
        String sep = "";
        for (String language : languageSet) {
            text.append(sep);
            sep = ", ";
            text.append(language.trim());
        }
        return text.toString();
    }

    @NotNull
    public static Set<String> getLanguageSet(final String list, final boolean toLowerCase) {
        String[] languageList = list.trim().split(",");
        LinkedHashSet<String> languageSet = new LinkedHashSet<>();

        for (String language : languageList) {
            String trimmed = language.trim();
            if (trimmed.isEmpty()) continue;
            
            if (toLowerCase) {
                languageSet.add(trimmed.toLowerCase());
            } else {
                languageSet.add(trimmed);
            }
        }
        return languageSet;
    }

    public boolean isDisableAutoPopupCompletionsOnSpace() {
        return disableAutoPopupCompletionsOnSpace;
    }

    public void setDisableAutoPopupCompletionsOnSpace(final boolean disableAutoPopupCompletionsOnSpace) {
        this.disableAutoPopupCompletionsOnSpace = disableAutoPopupCompletionsOnSpace;
    }

    public boolean isOnlyFor() {
        return onlyFor;
    }

    public void setOnlyFor(final boolean onlyFor) {
        this.onlyFor = onlyFor;
    }

    public String getOnlyForList() {
        return onlyForList;
    }

    public void setOnlyForList(final String onlyForList) {
        this.onlyForList = onlyForList;
    }

    @Nullable
    public ApplicationSettings getState() {
        return this;
    }

    public void loadState(@NotNull ApplicationSettings applicationSettings) {
        XmlSerializerUtil.copyBean(applicationSettings, this);
    }

    public void initComponent() {
    }

    public void disposeComponent() {
    }

    @NotNull
    public String getComponentName() {
        return this.getClass().getName();
    }

    public static ApplicationSettings getInstance() {
        return ApplicationManager.getApplication().getComponent(ApplicationSettings.class);
    }
}
