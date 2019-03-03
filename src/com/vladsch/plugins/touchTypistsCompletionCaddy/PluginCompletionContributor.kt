package com.vladsch.plugins.touchTypistsCompletionCaddy

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.editor.Editor

class PluginCompletionContributor : CompletionContributor() {
    private val settings = ApplicationSettings.getInstance()
    
    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        // store completion parameters for our char completion filter
        if (settings.isDisableAutoPopupCompletionsOnSpace) {
            val project = parameters.editor.project ?: return
            val pluginProject = PluginProject.getInstance(project)
            pluginProject.completionParameters = parameters
        }
    }

    override fun beforeCompletion(context: CompletionInitializationContext) {
        
    }

    override fun handleEmptyLookup(parameters: CompletionParameters, editor: Editor?): String? {
        return null
    }

    override fun handleAutoCompletionPossibility(context: AutoCompletionContext): AutoCompletionDecision? {
        return null
    }

    override fun duringCompletion(context: CompletionInitializationContext) {
        
    }
}
