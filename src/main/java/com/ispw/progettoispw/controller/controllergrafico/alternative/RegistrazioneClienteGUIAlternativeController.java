package com.ispw.progettoispw.controller.controllergrafico.alternative;

import com.ispw.progettoispw.controller.controllerapplicativo.RegistrazioneController;
import com.ispw.progettoispw.controller.controllergrafico.GraphicController;
import com.ispw.progettoispw.enu.Role;
import com.ispw.progettoispw.exception.DuplicateCredentialException;
import com.ispw.progettoispw.exception.ValidazioneException;
import com.ispw.progettoispw.bean.RegistrationBean;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RegistrazioneClienteGUIAlternativeController extends GraphicController {

    @FXML private TextField nameLabel;
    @FXML private TextField cognomeLabel;
    @FXML private TextField emailLabel;
    @FXML private TextField phoneLabel;
    @FXML private PasswordField passwordLabel;
    @FXML private PasswordField confirmPassword;

    private final RegistrazioneController appController = new RegistrazioneController();

    @FXML
    private void onRegister() {
        RegistrationBean bean = new RegistrationBean();
        bean.setUserType(Role.CLIENTE);

        bean.setFirstName(safeTrim(nameLabel.getText()));
        bean.setLastName(safeTrim(cognomeLabel.getText()));
        bean.setEmail(safeTrim(emailLabel.getText()));
        bean.setPhoneNumber(safeTrim(phoneLabel.getText()));
        bean.setPassword(nullSafe(passwordLabel.getText()));
        bean.setRepeatPassword(nullSafe(confirmPassword.getText()));

        try {
            appController.register(bean);
            showInfo("Registrazione completata! Ora effettua il login.");
            switchSafe("LoginViewAlternative.fxml", "Login");

        } catch (ValidazioneException e) {
            showError(e.getMessage());
        } catch (DuplicateCredentialException e) {
            showError(e.getMessage());
        } catch (Exception ex) {
            showError("Errore tecnico durante la registrazione.");
        }
    }

    @FXML
    private void onLogin() {
        switchSafe("LoginViewAlternative.fxml", "Login");
    }

    @FXML
    private void onLink() {
        switchSafe("RegistrationBarbiereViewAlternative.fxml", "Registrazione Barbiere");
    }

    private static String safeTrim(String s) { return s == null ? "" : s.trim(); }
    private static String nullSafe(String s) { return s == null ? "" : s; }
}
