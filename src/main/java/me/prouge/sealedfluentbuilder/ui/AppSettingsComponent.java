package me.prouge.sealedfluentbuilder.ui;

import com.intellij.util.ui.FormBuilder;
import me.prouge.sealedfluentbuilder.utils.ConstructorModifier;

import javax.swing.*;
import java.awt.*;

public class AppSettingsComponent {

    private final JPanel myMainPanel;

    final JRadioButton constructorPublicRadioButton = new JRadioButton("public");
    final JRadioButton constructorProtectedRadioButton = new JRadioButton("protected");
    final JRadioButton constructorPrivateRadioButton = new JRadioButton("private");

    final JRadioButton constructorWithBuilderPublicRadioButton = new JRadioButton("public");
    final JRadioButton constructorWithBuilderProtectedRadioButton = new JRadioButton("protected");
    final JRadioButton constructorWithBuilderPrivateRadioButton = new JRadioButton("private");

    final ButtonGroup constructorGroup = new ButtonGroup();
    final ButtonGroup constructorWithBuilderGroup = new ButtonGroup();

    final JComboBox<String> dropdown = new JComboBox<>(new String[]{"Constructor", "Constructor with builder", "With setters"});

    final JTextField prefixTextField = new JTextField();


    public AppSettingsComponent() {
        constructorGroup.add(constructorPublicRadioButton);
        constructorGroup.add(constructorProtectedRadioButton);
        constructorGroup.add(constructorPrivateRadioButton);

        constructorWithBuilderGroup.add(constructorWithBuilderPublicRadioButton);
        constructorWithBuilderGroup.add(constructorWithBuilderProtectedRadioButton);
        constructorWithBuilderGroup.add(constructorWithBuilderPrivateRadioButton);

        final JPanel constructorRadioButtonPanel = new JPanel(new BorderLayout());
        constructorRadioButtonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 100));
        constructorRadioButtonPanel.add(new JLabel("Constructor:"), BorderLayout.WEST);

        JPanel constructorRadioButtonPanelRadios = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        constructorRadioButtonPanelRadios.add(constructorPublicRadioButton);
        constructorRadioButtonPanelRadios.add(constructorProtectedRadioButton);
        constructorRadioButtonPanelRadios.add(constructorPrivateRadioButton);
        constructorRadioButtonPanel.add(constructorRadioButtonPanelRadios, BorderLayout.EAST);


        final JPanel constructorWithBuilderRadioButtonPanel = new JPanel(new BorderLayout());

        JPanel constructorWithBuilderRadioButtonPanelRadios = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        constructorWithBuilderRadioButtonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 100));
        constructorWithBuilderRadioButtonPanel.add(new JLabel("Constructor with builder:"), BorderLayout.WEST);

        constructorWithBuilderRadioButtonPanelRadios.add(constructorWithBuilderPublicRadioButton);
        constructorWithBuilderRadioButtonPanelRadios.add(constructorWithBuilderProtectedRadioButton);
        constructorWithBuilderRadioButtonPanelRadios.add(constructorWithBuilderPrivateRadioButton);
        constructorWithBuilderRadioButtonPanel.add(constructorWithBuilderRadioButtonPanelRadios, BorderLayout.EAST);

        final JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 100));
        panel.add(new JLabel("Default selected builder type:"), BorderLayout.WEST);
        panel.add(dropdown, BorderLayout.EAST);

        final JPanel prefixPanel = new JPanel(new BorderLayout());
        prefixPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 100));
        prefixPanel.add(new JLabel("Prefix for builder method:"), BorderLayout.WEST);

        prefixTextField.setColumns(18);
        prefixPanel.add(prefixTextField, BorderLayout.EAST);


        myMainPanel = FormBuilder.createFormBuilder()
                .addComponent(constructorRadioButtonPanel)
                .addComponent(constructorWithBuilderRadioButtonPanel)
                .addComponent(panel)
                .addComponent(prefixPanel)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    public JPanel getPanel() {
        return myMainPanel;
    }


    public ConstructorModifier getSelectedConstructorModifier() {
        if (constructorPublicRadioButton.isSelected()) {
            return ConstructorModifier.PUBLIC;
        }
        if (constructorProtectedRadioButton.isSelected()) {
            return ConstructorModifier.PROTECTED;
        }
        return ConstructorModifier.PRIVATE;
    }

    public ConstructorModifier getSelectedConstructorWithBuilderModifier() {
        if (constructorWithBuilderPublicRadioButton.isSelected()) {
            return ConstructorModifier.PUBLIC;
        }
        if (constructorWithBuilderProtectedRadioButton.isSelected()) {
            return ConstructorModifier.PROTECTED;
        }
        return ConstructorModifier.PRIVATE;
    }

    public void setSelectedConstructorModifier(final ConstructorModifier modifier) {
        this.constructorPublicRadioButton.setSelected(false);
        this.constructorProtectedRadioButton.setSelected(false);
        this.constructorPrivateRadioButton.setSelected(false);

        if (modifier == ConstructorModifier.PUBLIC) {
            this.constructorPublicRadioButton.setSelected(true);
        }
        if (modifier == ConstructorModifier.PROTECTED) {
            this.constructorProtectedRadioButton.setSelected(true);
        }
        if (modifier == ConstructorModifier.PRIVATE) {
            this.constructorPrivateRadioButton.setSelected(true);
        }

    }

    public void setSelectedConstructorWithBuilderModifier(final ConstructorModifier modifier) {
        this.constructorWithBuilderPublicRadioButton.setSelected(false);
        this.constructorWithBuilderProtectedRadioButton.setSelected(false);
        this.constructorWithBuilderPrivateRadioButton.setSelected(false);

        if (modifier == ConstructorModifier.PUBLIC) {
            this.constructorWithBuilderPublicRadioButton.setSelected(true);
        }
        if (modifier == ConstructorModifier.PROTECTED) {
            this.constructorWithBuilderProtectedRadioButton.setSelected(true);
        }
        if (modifier == ConstructorModifier.PRIVATE) {
            this.constructorWithBuilderPrivateRadioButton.setSelected(true);
        }

    }

    public int getSelectedDropdownIndex() {
        return this.dropdown.getSelectedIndex();
    }

    public void setSelectedDropdownIndex(final int selectedDropdownIndex) {
        this.dropdown.setSelectedIndex(selectedDropdownIndex);
    }

    public String getPrefix() {
        return this.prefixTextField.getText();
    }

    public void setPrefix(final String prefix) {
        this.prefixTextField.setText(prefix);
    }


}