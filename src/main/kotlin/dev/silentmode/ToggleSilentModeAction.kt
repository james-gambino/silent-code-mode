package dev.silentmode

import com.intellij.codeInsight.CodeInsightSettings
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

/** Application-wide: each IDE installation maintains its own independent mode. */
@Service(Service.Level.APP)
@State(name = "SilentCodeModeState", storages = [Storage("SilentCodeMode.xml")])
class SilentModeState : PersistentStateComponent<SilentModeState.Data> {
    class Data {
        var active: Boolean = false
        var saved: Boolean = false
        var completion: Boolean = true
        var parameterInfo: Boolean = true
        var documentation: Boolean = false
        var completionParameterHints: Boolean = false
        var extraValues: MutableMap<String, Boolean> = mutableMapOf()
    }

    private var data = Data()
    override fun getState(): Data = data
    override fun loadState(state: Data) { data = state }

    @Synchronized
    fun setEnabled(enabled: Boolean) {
        if (enabled == data.active) return
        if (enabled) {
            capture()
            data.active = true
            silence()
        } else {
            restore()
            data.active = false
            data.saved = false
            data.extraValues.clear()
        }
    }

    fun isEnabled(): Boolean = data.active

    /** Called when a project opens: an IDE restart must not silently revert the mode. */
    @Synchronized
    fun reapply() {
        if (data.active) silence()
    }

    private fun capture() {
        val s = CodeInsightSettings.getInstance()
        data.completion = s.AUTO_POPUP_COMPLETION_LOOKUP
        data.parameterInfo = s.AUTO_POPUP_PARAMETER_INFO
        data.documentation = s.AUTO_POPUP_JAVADOC_INFO
        data.completionParameterHints = s.SHOW_PARAMETER_NAME_HINTS_ON_COMPLETION
        data.extraValues.clear()
        for (option in OPTIONAL_OPTIONS) {
            readBoolean(option)?.let { data.extraValues[option.key] = it }
        }
        data.saved = true
    }

    private fun silence() {
        val s = CodeInsightSettings.getInstance()
        s.AUTO_POPUP_COMPLETION_LOOKUP = false
        s.AUTO_POPUP_PARAMETER_INFO = false
        s.AUTO_POPUP_JAVADOC_INFO = false
        s.SHOW_PARAMETER_NAME_HINTS_ON_COMPLETION = false
        for (option in OPTIONAL_OPTIONS) writeBoolean(option, false)
    }

    private fun restore() {
        if (!data.saved) return
        val s = CodeInsightSettings.getInstance()
        s.AUTO_POPUP_COMPLETION_LOOKUP = data.completion
        s.AUTO_POPUP_PARAMETER_INFO = data.parameterInfo
        s.AUTO_POPUP_JAVADOC_INFO = data.documentation
        s.SHOW_PARAMETER_NAME_HINTS_ON_COMPLETION = data.completionParameterHints
        for (option in OPTIONAL_OPTIONS) {
            data.extraValues[option.key]?.let { writeBoolean(option, it) }
        }
    }

    private data class BooleanOption(val owner: String, val getter: String, val setter: String) {
        val key: String get() = "$owner#$getter"
    }

    /** Optional APIs are accessed defensively: missing/changed APIs never prevent toggling. */
    private val OPTIONAL_OPTIONS = listOf(
        BooleanOption("com.intellij.openapi.editor.ex.EditorSettingsExternalizable", "isShowIntentionBulb", "setShowIntentionBulb"),
        BooleanOption("com.intellij.openapi.editor.ex.EditorSettingsExternalizable", "isShowParameterNameHints", "setShowParameterNameHints"),
        BooleanOption("com.intellij.codeInsight.hints.InlayHintsSettings", "isEnabledGlobally", "setEnabledGlobally")
    )

    private fun instance(option: BooleanOption): Any? = runCatching {
        val klass = Class.forName(option.owner)
        klass.getMethod("getInstance").invoke(null)
    }.getOrNull()

    private fun readBoolean(option: BooleanOption): Boolean? = runCatching {
        val obj = instance(option) ?: return null
        obj.javaClass.getMethod(option.getter).invoke(obj) as? Boolean
    }.getOrNull()

    private fun writeBoolean(option: BooleanOption, value: Boolean) {
        runCatching {
            val obj = instance(option) ?: return
            obj.javaClass.getMethod(option.setter, Boolean::class.javaPrimitiveType).invoke(obj, value)
        }
    }
}

class ToggleSilentModeAction : ToggleAction() {
    override fun isSelected(e: AnActionEvent): Boolean = ApplicationManager.getApplication().service<SilentModeState>().isEnabled()

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        ApplicationManager.getApplication().service<SilentModeState>().setEnabled(state)
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        val on = isSelected(e)
        e.presentation.text = if (on) "Silent Code Mode: ON" else "Silent Code Mode: OFF"
        e.presentation.description = if (on) "Click to restore your previous editor settings" else "Click to disable automatic code hints"
    }
}

class SilentModeStartup : ProjectActivity {
    override suspend fun execute(project: Project) {
        ApplicationManager.getApplication().service<SilentModeState>().reapply()
    }
}
