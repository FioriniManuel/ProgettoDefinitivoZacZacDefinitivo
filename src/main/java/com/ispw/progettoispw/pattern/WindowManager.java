package com.ispw.progettoispw.pattern;

import com.ispw.progettoispw.exception.ViewLoadException;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import javafx.scene.Scene;
import javafx.scene.Parent;


public class WindowManager {
    private static WindowManager instance;
    private Stage primaryStage;

    private WindowManager(Stage stage) {
        this.primaryStage = stage;


    }

    public static WindowManager getInstance(Stage stage) {
        if (instance == null) {
            instance = new WindowManager(stage);
        }
        return instance;
    }

    public static WindowManager getInstance(){
        return instance;
    }

    public void switchScene(String fxmlFile, String nome) throws ViewLoadException {
        try {
            // Path assoluto
            URL fxmlLocation = getClass().getResource("/com/ispw/progettoispw/" + fxmlFile);
            if (fxmlLocation == null) {
                throw new ViewLoadException("FXML non trovato: " + fxmlFile);
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle(nome);
            primaryStage.show();
            primaryStage.setOnShown(e -> primaryStage.centerOnScreen());

        } catch (IOException e) {
            // Incapsula l'eccezione tecnica
            throw new ViewLoadException(
                    "Errore nel caricamento della vista: " + fxmlFile,e)
            ;
        }
    }



}