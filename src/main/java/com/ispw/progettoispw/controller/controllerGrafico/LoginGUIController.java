package com.ispw.progettoispw.Controller.ControllerGrafico;


import com.ispw.progettoispw.Controller.ControllerApplicativo.LoginController;
import com.ispw.progettoispw.Enum.Role;
import com.ispw.progettoispw.Exception.AutenticazioneException;
import com.ispw.progettoispw.Exception.ValidazioneException;

import com.ispw.progettoispw.bean.LoginBean;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;

public class LoginGUIController extends GraphicController {

    @FXML private Hyperlink registrationHyperlink;
    @FXML private Label loginMessageLabel;
    @FXML private TextField emailTextField;
    @FXML private PasswordField enterPasswordField;
    @FXML private Button loginButton;
    @FXML private ImageView brandingImageView;
    @FXML private CheckBox barbiereCheckBox;

    private final LoginController loginService = new LoginController();

    @FXML
    public void initialize() {
        File brandingFile = new File("Imagini/Logo.png");
        if (brandingFile.exists()) {
            Image brandingImage = new Image(brandingFile.toURI().toString());
            brandingImageView.setImage(brandingImage);
        }
        clear(loginMessageLabel);
    }

    @FXML
    public void loginButtonAction(ActionEvent event) {
        clear(loginMessageLabel);

        LoginBean bean = new LoginBean();
        bean.setEmail(trimOrEmpty(emailTextField));
        bean.setPassword(trimOrEmpty(enterPasswordField));
        bean.setUserType((barbiereCheckBox != null && barbiereCheckBox.isSelected())
                ? Role.BARBIERE
                : Role.CLIENTE);

        try {
            Role role = loginService.login(bean);

            // messaggio ok (usa il tuo showInfo del GraphicController)
            showInfo("Accesso effettuato. Benvenuto!");

            // navigazione
            if (role == Role.CLIENTE) {
                switchSafe("HomeView.fxml", "Home");
            } else {
               switchSafe("HomeBarbiereView.fxml", "Home Barbiere");
            }

        } catch (ValidazioneException | AutenticazioneException e) {
            // errori utente: su label
            setError(loginMessageLabel, e.getMessage());

        }
    }

    @FXML
    public void registrationHyperlinkOnAction(ActionEvent event) {
       switchSafe("RegistrationView.fxml", "Registrazione");

    }

    // ---- helpers ----
    private static String trimOrEmpty(TextField f) {
        String s = (f != null) ? f.getText() : null;
        return (s == null) ? "" : s.trim();
    }

    private static String trimOrEmpty(PasswordField f) {
        String s = (f != null) ? f.getText() : null;
        return (s == null) ? "" : s.trim();
    }
}
