package me.prouge.sealedfluentbuilder.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import me.prouge.sealedfluentbuilder.utils.ConstructorModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
        name = "me.prouge.sealedfluentbuilder.ui.AppSettingsState",
        storages = @Storage("SealedFluentBuilderPluginSettings.xml")
)
public final class AppSettingsState implements PersistentStateComponent<AppSettingsState> {

    public ConstructorModifier constructorModifier = ConstructorModifier.PRIVATE;
    public ConstructorModifier constructorWithBuilderModifier = ConstructorModifier.PRIVATE;

    public int selectedDropdownIndex = 0;


    public static AppSettingsState getInstance() {
        return ApplicationManager.getApplication().getService(AppSettingsState.class);
    }

    @Nullable
    @Override
    public AppSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull AppSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }

}