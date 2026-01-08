package com.ispw.progettoispw.controller.controllerGrafico.alternative;

import com.ispw.progettoispw.controller.controllerApplicativo.LoginController;
import com.ispw.progettoispw.controller.controllerGrafico.GraphicController;
import com.ispw.progettoispw.enu.Role;
import com.ispw.progettoispw.exception.AutenticazioneException;
import com.ispw.progettoispw.exception.ValidazioneException;

import com.ispw.progettoispw.bean.LoginBean;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginGUIAlternativeController extends GraphicController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private RadioButton clienteRadio;
    @FXML private RadioButton barbiereRadio;
    @FXML private Button loginButton;
    @FXML private Button registrazioneButton;

    private ToggleGroup roleGroup;
    private final LoginController appController = new LoginController();

    @FXML
    public void initialize() {
        roleGroup = new ToggleGroup();
        clienteRadio.setToggleGroup(roleGroup);
        barbiereRadio.setToggleGroup(roleGroup);

        clienteRadio.setSelected(true);
        loginButton.setDefaultButton(true);
    }

    @FXML
    private void onLogin(ActionEvent event) {
        String email = safeTrim(emailField.getText());
        String pwd = (passwordField.getText() == null) ? "" : passwordField.getText().trim();

        LoginBean bean = new LoginBean();
        bean.setEmail(email);
        bean.setPassword(pwd);
        bean.setUserType(clienteRadio.isSelected() ? Role.CLIENTE : Role.BARBIERE);

        try {
            Role role = appController.login(bean);

            showInfo("Accesso effettuato. Benvenuto!");

            if (role == Role.CLIENTE) {
                switchSafe("HomeViewAlternative.fxml", "Home");
            } else {
                switchSafe("HomeBarbiereViewAlternative.fxml", "Home Barbiere");
            }

        } catch (ValidazioneException e) {
            // input mancante/errato
            showError(e.getMessage());

        } catch (AutenticazioneException e) {
            // utente non trovato / password errata
            showError(e.getMessage());
        }
    }

    @FXML
    private void onRegistrazione(ActionEvent event) {
        if (clienteRadio.isSelected()) {
            switchSafe("RegistrationClienteViewAlternative.fxml", "Registrazione");
        } else if (barbiereRadio.isSelected()) {
            switchSafe("RegistrationBarbiereViewAlternative.fxml", "Registrazione");
        } else {
            showError("Seleziona Cliente o Barbiere prima di registrarti.");
        }
    }

    // --- cambio view: showAlert SOLO qui (come richiesto) ---


    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }
}
