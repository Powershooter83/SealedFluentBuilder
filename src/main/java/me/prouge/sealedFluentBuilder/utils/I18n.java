package me.prouge.sealedFluentBuilder.utils;



import java.util.Locale;
import java.util.ResourceBundle;


public class I18n {

    private static ResourceBundle resourceBundle;


    public static String getMessage(Message message) {
        if(resourceBundle == null) {
            resourceBundle = ResourceBundle.getBundle("languages/messages", Locale.getDefault());
        }
        return resourceBundle.getString(message.name());
    }





}
