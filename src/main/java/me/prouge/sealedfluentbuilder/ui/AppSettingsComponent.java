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

    public AppSettingsComponent() {
        constructorGroup.add(constructorPublicRadioButton);
        constructorGroup.add(constructorProtectedRadioButton);
        constructorGroup.add(constructorPrivateRadioButton);

        constructorWithBuilderGroup.add(constructorWithBuilderPublicRadioButton);
        constructorWithBuilderGroup.add(constructorWithBuilderProtectedRadioButton);
        constructorWithBuilderGroup.add(constructorWithBuilderPrivateRadioButton);

        final JPanel constructorRadioButtonPanel = new JPanel();
        constructorRadioButtonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        constructorRadioButtonPanel.add(new JLabel("Constructor:                       "));
        constructorRadioButtonPanel.add(constructorPublicRadioButton);
        constructorRadioButtonPanel.add(constructorProtectedRadioButton);
        constructorRadioButtonPanel.add(constructorPrivateRadioButton);

        final JPanel constructorWithBuilderRadioButtonPanel = new JPanel();
        constructorWithBuilderRadioButtonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        constructorWithBuilderRadioButtonPanel.add(new JLabel("Constructor with builder: "));
        constructorWithBuilderRadioButtonPanel.add(constructorWithBuilderPublicRadioButton);
        constructorWithBuilderRadioButtonPanel.add(constructorWithBuilderProtectedRadioButton);
        constructorWithBuilderRadioButtonPanel.add(constructorWithBuilderPrivateRadioButton);

        final JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Default selected builder type: "));
        panel.add(dropdown);

        myMainPanel = FormBuilder.createFormBuilder()
                .addComponent(constructorRadioButtonPanel)
                .addComponent(constructorWithBuilderRadioButtonPanel)
                .addComponent(panel)
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
}