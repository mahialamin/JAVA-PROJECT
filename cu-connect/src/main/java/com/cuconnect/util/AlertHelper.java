package com.cuconnect.util;

import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;

public class AlertHelper {

    public static void showError(String title, String content) {
        showAlert(Alert.AlertType.ERROR, title, content);
    }

    public static void showInfo(String title, String content) {
        showAlert(Alert.AlertType.INFORMATION, title, content);
    }

    private static void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        
        // Add CSS to Alert dialog to match the Dark Theme
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(AlertHelper.class.getResource("/styles.css").toExternalForm());
        
        alert.showAndWait();
    }
}
