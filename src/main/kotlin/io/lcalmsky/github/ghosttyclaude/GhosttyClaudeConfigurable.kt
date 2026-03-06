package io.lcalmsky.github.ghosttyclaude

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextArea
import javax.swing.JScrollPane
import java.awt.Font

class GhosttyClaudeConfigurable : Configurable {

    private var skipPermissionsCheckBox: JBCheckBox? = null
    private var verboseCheckBox: JBCheckBox? = null
    private var autoFocusCheckBox: JBCheckBox? = null
    private var additionalArgsField: JBTextField? = null
    private var windowPositionCombo: ComboBox<WindowPosition>? = null
    private var tmuxSetupCommandsArea: JTextArea? = null
    private var panel: JPanel? = null

    override fun getDisplayName(): String = "Ghostty Claude"

    override fun createComponent(): JComponent {
        skipPermissionsCheckBox = JBCheckBox("Skip permission prompts (--dangerously-skip-permissions)")
        verboseCheckBox = JBCheckBox("Verbose output (--verbose)")
        autoFocusCheckBox = JBCheckBox("Auto-focus Ghostty window after sending context")
        additionalArgsField = JBTextField().apply { columns = 40 }
        windowPositionCombo = ComboBox(DefaultComboBoxModel(WindowPosition.entries.toTypedArray())).apply {
            renderer = object : javax.swing.DefaultListCellRenderer() {
                override fun getListCellRendererComponent(
                    list: javax.swing.JList<*>?, value: Any?, index: Int,
                    isSelected: Boolean, cellHasFocus: Boolean
                ) = super.getListCellRendererComponent(
                    list, (value as? WindowPosition)?.label ?: value, index, isSelected, cellHasFocus
                )
            }
        }
        tmuxSetupCommandsArea = JTextArea(8, 40).apply {
            lineWrap = true
            wrapStyleWord = false
            font = Font(Font.MONOSPACED, Font.PLAIN, 12)
        }

        panel = FormBuilder.createFormBuilder()
            .addComponent(JBLabel("Claude Code Options"), 0)
            .addComponent(skipPermissionsCheckBox!!, 5)
            .addComponent(verboseCheckBox!!, 5)
            .addSeparator(10)
            .addLabeledComponent(JBLabel("Additional arguments:"), additionalArgsField!!, 5, false)
            .addComponentToRightColumn(
                JBLabel("<html><font color='gray'>e.g. --model sonnet</font></html>"), 0
            )
            .addSeparator(10)
            .addComponent(JBLabel("Window"), 0)
            .addLabeledComponent(JBLabel("Window position:"), windowPositionCombo!!, 5, false)
            .addSeparator(10)
            .addComponent(JBLabel("Behavior"), 0)
            .addComponent(autoFocusCheckBox!!, 5)
            .addSeparator(10)
            .addComponent(JBLabel("Tmux"), 0)
            .addLabeledComponent(JBLabel("Setup commands:"), JScrollPane(tmuxSetupCommandsArea!!), 5, true)
            .addComponentToRightColumn(
                JBLabel("<html><font color='gray'>Each line is a tmux command. Lines starting with # are ignored.</font></html>"), 0
            )
            .addComponentFillVertically(JPanel(), 0)
            .panel

        reset()
        return panel!!
    }

    override fun isModified(): Boolean {
        val s = GhosttyClaudeSettings.getInstance().state
        return skipPermissionsCheckBox?.isSelected != s.skipPermissions ||
                verboseCheckBox?.isSelected != s.verbose ||
                autoFocusCheckBox?.isSelected != s.autoFocusGhostty ||
                additionalArgsField?.text != s.additionalArgs ||
                (windowPositionCombo?.selectedItem as? WindowPosition)?.name != s.windowPosition ||
                tmuxSetupCommandsArea?.text != s.tmuxSetupCommands
    }

    override fun apply() {
        val s = GhosttyClaudeSettings.getInstance().state
        s.skipPermissions = skipPermissionsCheckBox?.isSelected ?: false
        s.verbose = verboseCheckBox?.isSelected ?: false
        s.autoFocusGhostty = autoFocusCheckBox?.isSelected ?: true
        s.additionalArgs = additionalArgsField?.text?.trim() ?: ""
        s.windowPosition = (windowPositionCombo?.selectedItem as? WindowPosition)?.name ?: "DEFAULT"
        s.tmuxSetupCommands = tmuxSetupCommandsArea?.text?.trim() ?: ""
    }

    override fun reset() {
        val s = GhosttyClaudeSettings.getInstance().state
        skipPermissionsCheckBox?.isSelected = s.skipPermissions
        verboseCheckBox?.isSelected = s.verbose
        autoFocusCheckBox?.isSelected = s.autoFocusGhostty
        additionalArgsField?.text = s.additionalArgs
        val pos = try { WindowPosition.valueOf(s.windowPosition) } catch (_: Exception) { WindowPosition.DEFAULT }
        windowPositionCombo?.selectedItem = pos

        // 빈 값이면 기본값 사용
        val tmuxCommands = if (s.tmuxSetupCommands.isBlank()) {
            "tmux set mouse off\ntmux set history-limit 50000\ntmux set-window-option mode-keys vi\ntmux set escape-time 0"
        } else {
            s.tmuxSetupCommands
        }
        tmuxSetupCommandsArea?.text = tmuxCommands
    }

    override fun disposeUIResources() {
        skipPermissionsCheckBox = null
        verboseCheckBox = null
        autoFocusCheckBox = null
        additionalArgsField = null
        windowPositionCombo = null
        tmuxSetupCommandsArea = null
        panel = null
    }
}
