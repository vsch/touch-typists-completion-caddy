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

import com.vladsch.plugin.util.ui.ComboBoxAdaptable;
import com.vladsch.plugin.util.ui.ComboBoxAdapter;
import com.vladsch.plugin.util.ui.ComboBoxAdapterImpl;
import org.jetbrains.annotations.NotNull;

public enum OnSpaceType implements ComboBoxAdaptable<OnSpaceType> {
    SPACE_ONLY(0, Bundle.message("settings.on-space-type.space-only")),
    SPACE_AND_ALL(1, Bundle.message("settings.on-space-type.space-and-all")),
    SPACE_AND(2, Bundle.message("settings.on-space-type.space-and"));

    public final int intValue;
    public final @NotNull String displayName;

    OnSpaceType(int intValue, @NotNull String displayName) {
        this.intValue = intValue;
        this.displayName = displayName;
    }

    public static final OnSpaceType DEFAULT = SPACE_ONLY;
    public static final Static<OnSpaceType> ADAPTER = new Static<>(new ComboBoxAdapterImpl<>(DEFAULT));

    public boolean isEnabledOn(char c, @NotNull String spaceAndList) {
        switch (this) {
            case SPACE_ONLY:
                return c == ' ';

            case SPACE_AND_ALL:
                return true;

            case SPACE_AND:
                return c == ' ' || spaceAndList.indexOf(c) != -1;
        }
        return false;
    }

    @NotNull
    @Override
    public ComboBoxAdapter<OnSpaceType> getAdapter() {
        return ADAPTER;
    }

    @Override
    public int getIntValue() { return intValue; }

    @NotNull
    public String getDisplayName() { return displayName; }

    @NotNull
    public OnSpaceType[] getValues() { return values(); }
}
