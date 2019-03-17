package com.vladsch.plugins.touchTypistsCompletionCaddy

import com.intellij.codeInsight.lookup.CharFilter
import com.intellij.codeInsight.lookup.Lookup
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.lang.Language
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.util.containers.ContainerUtil

class CompletionCharFilter : CharFilter() {
    private val settings = ApplicationSettings.getInstance()

    override fun acceptChar(c: Char, prefixLength: Int, lookup: Lookup): CharFilter.Result? {
        if (c != ' ' || !settings.isDisableAutoPopupCompletionsOnSpace) return null

        val item = lookup.currentItem ?: return null
        if (!lookup.isCompletion) return null
        
        val editor = lookup.editor
        val project = editor.project ?: return null
        val pluginProject = PluginProject.getInstance(project)
        val completionParameters = pluginProject.completionParameters ?: return null
        if (completionParameters.isAutoPopup && !pluginProject.isUserItemSelection) {
            LOG.debug("CompletionCharFilter")
            
            if (settings.isOnlyFor) {
                val element = item.psiElement
                val language: Language? =
                    element?.language ?: if (editor is EditorEx) {
                        val fileType = editor.virtualFile.fileType
                        if (fileType is LanguageFileType) {
                            fileType.language
                        } else {
                            null
                        }
                    } else {
                        null
                    }

                if (language != null && ApplicationSettings.getLanguageSet(settings.onlyForList, true).contains(language.displayName.trim().toLowerCase())) {
                    // TODO: implement options to select prefix
                    // val initializationContext = pluginProject.initializationContext ?: return null
                    // val lookupString = item.lookupString
                    // val completionText = lookup.editor.document.charsSequence.subSequence((completionParameters.offset- lookupString.length).minLimit(0), completionParameters.offset).toString()
                    return CharFilter.Result.HIDE_LOOKUP
                }
            } else {
                return CharFilter.Result.HIDE_LOOKUP
            }
        }
        return null
    }

    companion object {
        private val LOG = Logger.getInstance("com.vladsch.plugins.touchTypistsCompletionCaddy")
        //        val COMPLETION_CHAR_FILTERING: ClassConditionKey<MdPreventPartialCompletion> = ClassConditionKey.create(MdPreventPartialCompletion::class.java)

        private fun willHaveMatchAfterAppendingChar(lookup: LookupImpl, c: Char): Boolean {
            return ContainerUtil.exists(lookup.items) { matchesAfterAppendingChar(lookup, it, c) }
        }

        private fun matchesAfterAppendingChar(lookup: LookupImpl, item: LookupElement, c: Char): Boolean {
            val matcher = lookup.itemMatcher(item)
            return matcher.cloneWithPrefix(matcher.prefix + lookup.additionalPrefix + c).prefixMatches(item)
        }
    }
}
