package me.prouge.sealedfluentbuilder.ui;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

final class AppSettingsConfigurable implements Configurable {

    private AppSettingsComponent mySettingsComponent;

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Sealed Fluent Builder";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        mySettingsComponent = new AppSettingsComponent();
        return mySettingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        AppSettingsState settings = AppSettingsState.getInstance();
        boolean modified = mySettingsComponent.getSelectedConstructorModifier() != settings.constructorModifier;
        modified |= mySettingsComponent.getSelectedConstructorWithBuilderModifier() != settings.constructorWithBuilderModifier;
        modified |= mySettingsComponent.getSelectedDropdownIndex() != settings.selectedDropdownIndex;
        return modified;
    }

    @Override
    public void apply() {
        AppSettingsState settings = AppSettingsState.getInstance();
        settings.constructorModifier = mySettingsComponent.getSelectedConstructorModifier();
        settings.constructorWithBuilderModifier = mySettingsComponent.getSelectedConstructorWithBuilderModifier();
        settings.selectedDropdownIndex = mySettingsComponent.getSelectedDropdownIndex();
    }

    @Override
    public void reset() {
        AppSettingsState settings = AppSettingsState.getInstance();
        mySettingsComponent.setSelectedConstructorModifier(settings.constructorModifier);
        mySettingsComponent.setSelectedConstructorWithBuilderModifier(settings.constructorWithBuilderModifier);
        mySettingsComponent.setSelectedDropdownIndex(settings.selectedDropdownIndex);
    }

    @Override
    public void disposeUIResources() {
        mySettingsComponent = null;
    }

}