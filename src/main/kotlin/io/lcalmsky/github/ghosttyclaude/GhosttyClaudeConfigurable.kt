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

class GhosttyClaudeConfigurable : Configurable {

    private var skipPermissionsCheckBox: JBCheckBox? = null
    private var verboseCheckBox: JBCheckBox? = null
    private var autoFocusCheckBox: JBCheckBox? = null
    private var additionalArgsField: JBTextField? = null
    private var windowPositionCombo: ComboBox<WindowPosition>? = null
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
                (windowPositionCombo?.selectedItem as? WindowPosition)?.name != s.windowPosition
    }

    override fun apply() {
        val s = GhosttyClaudeSettings.getInstance().state
        s.skipPermissions = skipPermissionsCheckBox?.isSelected ?: false
        s.verbose = verboseCheckBox?.isSelected ?: false
        s.autoFocusGhostty = autoFocusCheckBox?.isSelected ?: true
        s.additionalArgs = additionalArgsField?.text?.trim() ?: ""
        s.windowPosition = (windowPositionCombo?.selectedItem as? WindowPosition)?.name ?: "DEFAULT"
    }

    override fun reset() {
        val s = GhosttyClaudeSettings.getInstance().state
        skipPermissionsCheckBox?.isSelected = s.skipPermissions
        verboseCheckBox?.isSelected = s.verbose
        autoFocusCheckBox?.isSelected = s.autoFocusGhostty
        additionalArgsField?.text = s.additionalArgs
        val pos = try { WindowPosition.valueOf(s.windowPosition) } catch (_: Exception) { WindowPosition.DEFAULT }
        windowPositionCombo?.selectedItem = pos
    }

    override fun disposeUIResources() {
        skipPermissionsCheckBox = null
        verboseCheckBox = null
        autoFocusCheckBox = null
        additionalArgsField = null
        windowPositionCombo = null
        panel = null
    }
}
