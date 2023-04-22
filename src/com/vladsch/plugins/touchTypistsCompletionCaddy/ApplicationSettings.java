package com.vladsch.plugins.touchTypistsCompletionCaddy;

import com.intellij.openapi.application.ApplicationManager;
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
public class ApplicationSettings implements PersistentStateComponent<ApplicationSettings> {
    final static String[] EMPTY_STRINGS = new String[0];

    public static ApplicationSettings getInstance() {
        return ApplicationManager.getApplication().getService(ApplicationSettings.class);
    }

    private boolean disableAutoPopupCompletionsOnSpace = true;
    private boolean onlyFor = true;
    private String onlyForList = "Kotlin";
    private int onSpace = OnSpaceType.DEFAULT.intValue;

    private String spaceAndList = "";
    private boolean textBoxCompletions = false;

    public OnSpaceType getOnSpaceType() {
        return OnSpaceType.ADAPTER.get(onSpace);
    }

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

    public boolean isTextBoxCompletions() {
        return textBoxCompletions;
    }

    public void setTextBoxCompletions(final boolean textBoxCompletions) {
        this.textBoxCompletions = textBoxCompletions;
    }

    public String getOnlyForList() {
        return onlyForList;
    }

    public boolean isSpaceOnly() {
        return onSpace == OnSpaceType.SPACE_ONLY.intValue;
    }

    public void setSpaceOnly(final boolean spaceOnly) {
        if (spaceOnly) onSpace = OnSpaceType.SPACE_ONLY.intValue;
    }

    public boolean isSpaceAndAll() {
        return onSpace == OnSpaceType.SPACE_AND_ALL.intValue;
    }

    public void setSpaceAndAll(final boolean spaceAndAll) {
        if (spaceAndAll) onSpace = OnSpaceType.SPACE_AND_ALL.intValue;
    }

    public boolean isSpaceAnd() {
        return onSpace == OnSpaceType.SPACE_AND.intValue;
    }

    public void setSpaceAnd(final boolean spaceAnd) {
        if (spaceAnd) onSpace = OnSpaceType.SPACE_AND.intValue;
    }

    public String getSpaceAndList() {
        return spaceAndList;
    }

    public void setSpaceAndList(final String spaceAndList) {
        this.spaceAndList = spaceAndList;
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
}
