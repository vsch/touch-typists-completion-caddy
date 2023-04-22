package com.vladsch.plugins.touchTypistsCompletionCaddy;

import com.intellij.application.options.CodeCompletionOptionsCustomSection;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;

public class ApplicationConfigurable implements SearchableConfigurable, CodeCompletionOptionsCustomSection {

    @Nullable private ApplicationSettingsForm myForm = null;
    @NotNull final private ApplicationSettings myApplicationSettings;

    public ApplicationConfigurable() {
        myApplicationSettings = ApplicationSettings.getInstance();
    }

    @NotNull
    @Override
    public String getId() {
        return "MarkdownNavigator.Settings.Application";
    }

    @Nls
    @Override
    public String getDisplayName() {
        return Bundle.message("plugin.name");
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @NotNull
    @Override
    public JComponent createComponent() {
        return getForm().getComponent();
    }

    @NotNull
    private ApplicationSettingsForm getForm() {
        if (myForm == null) {
            myForm = new ApplicationSettingsForm(myApplicationSettings);
        }
        return myForm;
    }

    @Override
    public boolean isModified() {
        return getForm().isModified();
    }

    @Override
    public void apply() {
        // save update stream
        getForm().apply();
    }

    @Override
    public void reset() {
        // reset update stream
        getForm().reset();
    }

    @Override
    public void disposeUIResources() {
        if (myForm != null) {
            Disposer.dispose(myForm);
            myForm = null;
        }
    }
}
