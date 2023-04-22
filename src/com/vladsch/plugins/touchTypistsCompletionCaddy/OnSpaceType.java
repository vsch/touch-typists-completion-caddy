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
