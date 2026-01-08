package com.ispw.progettoispw.Controller.ControllerGrafico.Alternative;

import com.ispw.progettoispw.Controller.ControllerApplicativo.RegistrazioneController;
import com.ispw.progettoispw.Controller.ControllerGrafico.GraphicController;
import com.ispw.progettoispw.Enum.GenderCategory;
import com.ispw.progettoispw.Enum.Role;
import com.ispw.progettoispw.Exception.DuplicateCredentialException;
import com.ispw.progettoispw.Exception.ValidazioneException;
import com.ispw.progettoispw.bean.RegistrationBean;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RegistrazioneBarberGUIAlternativeController extends GraphicController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField repeatPasswordField;

    @FXML private RadioButton donnaRadio;
    @FXML private RadioButton uomoRadio;

    private final ToggleGroup genderGroup = new ToggleGroup();
    private final RegistrazioneController appController = new RegistrazioneController();

    @FXML
    public void initialize() {
        donnaRadio.setToggleGroup(genderGroup);
        uomoRadio.setToggleGroup(genderGroup);
        donnaRadio.setSelected(true);
    }

    @FXML
    private void onRegister() {
        RegistrationBean bean = new RegistrationBean();
        bean.setUserType(Role.BARBIERE);
        bean.setFirstName(safeTrim(firstNameField.getText()));
        bean.setLastName(safeTrim(lastNameField.getText()));
        bean.setEmail(safeTrim(emailField.getText()));
        bean.setPhoneNumber(safeTrim(phoneField.getText()));
        bean.setPassword(passwordField.getText() == null ? "" : passwordField.getText());
        bean.setRepeatPassword(repeatPasswordField.getText() == null ? "" : repeatPasswordField.getText());

        if (genderGroup.getSelectedToggle() == null) {
            showError("Seleziona Donna o Uomo.");
            return;
        }
        bean.setSpecializzazione(donnaRadio.isSelected() ? GenderCategory.DONNA : GenderCategory.UOMO);

        try {
            appController.register(bean);
            showInfo("Registrazione completata!");
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
    private void onGoLogin() {
        switchSafe("LoginViewAlternative.fxml", "Login");
    }

    @FXML
    private void onGoClientRegistration() {
        switchSafe("RegistrationClienteViewAlternative.fxml", "Registrazione Cliente");
    }

    private static String safeTrim(String s) { return s == null ? "" : s.trim(); }
}
