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

import com.intellij.ide.IdeEventQueue;
import com.intellij.lang.Language;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.ex.DefaultColorSchemesManager;
import com.intellij.openapi.editor.colors.impl.DefaultColorsScheme;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.ui.TextFieldWithAutoCompletion;
import com.intellij.ui.TextFieldWithAutoCompletionListProvider;
import com.intellij.util.textCompletion.DefaultTextCompletionValueDescriptor;
import com.intellij.util.textCompletion.TextCompletionValueDescriptor;
import com.intellij.util.textCompletion.TextFieldWithCompletion;
import com.intellij.util.textCompletion.ValuesCompletionProvider;
import com.intellij.util.ui.UIUtil;
import com.vladsch.plugin.util.ui.Settable;
import com.vladsch.plugin.util.ui.SettableForm;
import com.vladsch.plugin.util.ui.SettingsComponents;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

@SuppressWarnings("WeakerAccess")
public class ApplicationSettingsForm implements Disposable {
    private JPanel myMainPanel;
    private JLabel myVersion;
    final private ApplicationSettings mySettings;
    JCheckBox myDisableAutoPopupCompletionsOnSpace;
    private JCheckBox myOnlyFor;
    private JCheckBox myTextBoxCompletions;
    private TextFieldWithCompletion myOnlyForTextField;
    private JLabel myInstructionLabel;
    private JRadioButton mySpaceOnly;
    private JRadioButton mySpaceAndAll;
    private JRadioButton mySpaceAnd;
    private JTextField mySpaceAndList;

    private final SettingsComponents<ApplicationSettings> components;
    private final EditingCommitter myEditingCommitter;

    private TextCompletionValueDescriptor<Language> myTextCompletionValueDescriptor = null;
    private ValuesCompletionProvider<Language> myValuesCompletionProvider = null;
    private String myDisabledInstructions;
    private String myEnabledInstructions;

    public ApplicationSettingsForm(ApplicationSettings settings) {
        mySettings = settings;

        components = new SettingsComponents<ApplicationSettings>() {
            @Override
            protected Settable<ApplicationSettings>[] createComponents(@NotNull ApplicationSettings i) {
                //noinspection unchecked
                return new Settable[] {
                        //component(AutoLineModeType.ADAPTER, myAutoLineMode, i::getAutoLineMode, i::setAutoLineMode),
                        //component(myPrimaryCaretColor, i::primaryCaretColorRGB, i::primaryCaretColorRGB),
                        component(myDisableAutoPopupCompletionsOnSpace, i::isDisableAutoPopupCompletionsOnSpace, i::setDisableAutoPopupCompletionsOnSpace),
                        component(myOnlyFor, i::isOnlyFor, i::setOnlyFor),
                        component(myTextBoxCompletions, i::isTextBoxCompletions, i::setTextBoxCompletions),
                        component(mySpaceOnly, i::isSpaceOnly, i::setSpaceOnly),
                        component(mySpaceAndAll, i::isSpaceAndAll, i::setSpaceAndAll),
                        component(mySpaceAnd, i::isSpaceAnd, i::setSpaceAnd),
                        component(mySpaceAndList, i::getSpaceAndList, i::setSpaceAndList),
                        component(new SettableForm<ApplicationSettings>() {
                            @Override
                            public void reset(@NotNull final ApplicationSettings settings) {
                                String text = ApplicationSettings.cleanLanguageList(settings.getOnlyForList());
                                myOnlyForTextField.setText(text);
                            }

                            @Override
                            public void apply(@NotNull final ApplicationSettings settings) {
                                String languageList = ApplicationSettings.cleanLanguageList(myOnlyForTextField.getText());
                                settings.setOnlyForList(languageList);
                                myOnlyForTextField.setText(languageList);
                            }

                            @Override
                            public boolean isModified(@NotNull final ApplicationSettings settings) {
                                String languageList = ApplicationSettings.cleanLanguageList(settings.getOnlyForList());
                                String fieldText = ApplicationSettings.cleanLanguageList(myOnlyForTextField.getText());
                                return !languageList.equals(fieldText);
                            }
                        }, i),
                };
            }
        };

        final ActionListener actionListener = e -> updateOptions(false);

        String shortcut = KeymapUtil.getFirstKeyboardShortcutText(ActionManager.getInstance().getAction(IdeActions.ACTION_CODE_COMPLETION));
        myEnabledInstructions = Bundle.message("disable.auto.popup.completions.on.space.only.for.list.enabled.description", shortcut);
        myDisabledInstructions = Bundle.message("disable.auto.popup.completions.on.space.only.for.list.disabled.description");
        myInstructionLabel.setEnabled(false);

        myDisableAutoPopupCompletionsOnSpace.addActionListener(actionListener);
        myOnlyFor.addActionListener(actionListener);
        mySpaceOnly.addActionListener(actionListener);
        mySpaceAndAll.addActionListener(actionListener);
        mySpaceAnd.addActionListener(actionListener);
        mySpaceAndList.addActionListener(actionListener);

        myVersion.setText(String.format("(%s: %s)", Bundle.message("settings.version.label"), Plugin.fullProductVersion()));

        myEditingCommitter = new EditingCommitter();
        IdeEventQueue.getInstance().addDispatcher(myEditingCommitter, this);

        DefaultColorsScheme scheme = DefaultColorSchemesManager.getInstance().getFirstScheme();
        Color color = scheme.getColor(EditorColors.SELECTION_BACKGROUND_COLOR);

        myMainPanel.validate();

        updateOptions(true);
    }

    public JComponent getComponent() {
        return myMainPanel;
    }

    public boolean isModified() {
        return components.isModified(mySettings);
    }

    public void apply() {
        components.apply(mySettings);
    }

    public void reset() {
        components.reset(mySettings);
        updateOptions(false);
    }

    @Override
    public void dispose() {
        IdeEventQueue.getInstance().removeDispatcher(myEditingCommitter);
    }

    @SuppressWarnings("unused")
    void updateOptions(boolean typeChanged) {
        boolean enabled = myDisableAutoPopupCompletionsOnSpace.isSelected();
        myOnlyFor.setEnabled(enabled);
        myOnlyForTextField.setEnabled(myOnlyFor.isEnabled() && myOnlyFor.isSelected());
        myInstructionLabel.setText(myOnlyForTextField.isEnabled() ? myEnabledInstructions : myDisabledInstructions);

        mySpaceOnly.setEnabled(enabled);
        mySpaceAndAll.setEnabled(enabled);
        mySpaceAnd.setEnabled(enabled);
        mySpaceAndList.setEnabled(mySpaceAnd.isEnabled() && mySpaceAnd.isSelected());
    }

    @SuppressWarnings("SameParameterValue")
    private void updateValueProvider(String currentText) {
        if (myValuesCompletionProvider == null) {
            Collection<Language> languages = Language.getRegisteredLanguages();
            ArrayList<Language> languageList = new ArrayList<>();
            for (Language language : languages) {
                if (!language.getDisplayName().trim().isEmpty()) {
                    languageList.add(language);
                }
            }

            if (myTextCompletionValueDescriptor == null) {
                myTextCompletionValueDescriptor = new DefaultTextCompletionValueDescriptor<Language>() {
                    @NotNull
                    @Override
                    protected String getLookupString(@NotNull final Language item) {
                        return item.getDisplayName();
                    }

                    @Nullable
                    @Override
                    protected Icon getIcon(@NotNull final Language item) {
                        LanguageFileType fileType = item.getAssociatedFileType();
                        return fileType == null ? null : fileType.getIcon();
                    }
                };
            }

            myValuesCompletionProvider = new ValuesCompletionProvider<>(myTextCompletionValueDescriptor, Collections.singletonList(','), languageList, false);
        }
    }

    private void createUIComponents() {
        updateValueProvider("");

        myOnlyForTextField = new TextFieldWithCompletion(
                ProjectManager.getInstance().getDefaultProject(),
                myValuesCompletionProvider, "", true, true, true, true);
    }

    private static class EditingCommitter implements IdeEventQueue.EventDispatcher {
        @Override
        public boolean dispatch(@NotNull AWTEvent e) {
            if (e instanceof KeyEvent && e.getID() == KeyEvent.KEY_PRESSED && ((KeyEvent) e).getKeyCode() == KeyEvent.VK_ENTER) {
                if ((((KeyEvent) e).getModifiers() & ~(InputEvent.CTRL_DOWN_MASK | InputEvent.CTRL_MASK)) == 0) {
                    Component owner = UIUtil.findParentByCondition(KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner(), component -> component instanceof JTable);

                    if (owner instanceof JTable && ((JTable) owner).isEditing()) {
                        ((JTable) owner).editingStopped(null);
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
