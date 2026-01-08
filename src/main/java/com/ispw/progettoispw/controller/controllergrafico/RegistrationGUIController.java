package com.ispw.progettoispw.controller.controllergrafico;

import com.ispw.progettoispw.controller.controllerapplicativo.RegistrazioneController;
import com.ispw.progettoispw.enu.GenderCategory;
import com.ispw.progettoispw.enu.Role;
import com.ispw.progettoispw.exception.DuplicateCredentialException;
import com.ispw.progettoispw.exception.ValidazioneException;
import com.ispw.progettoispw.bean.RegistrationBean;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RegistrationGUIController extends GraphicController {

    @FXML private TextField nomeTextField;
    @FXML private TextField cognomeTextField;
    @FXML private TextField emailTextField;
    @FXML private TextField telefonoTextField;
    @FXML private PasswordField setPasswordField;
    @FXML private PasswordField confirmPassword;

    @FXML private Hyperlink loginHyperLink;
    @FXML private CheckBox barberCheckBox;

    @FXML private Label visibleLabel;
    @FXML private ToggleButton donnaVisibleToggleButton;
    @FXML private ToggleButton uomoVisibleToggleButton;

    private final ToggleGroup genderGroup = new ToggleGroup();
    private final RegistrazioneController registrazioneController = new RegistrazioneController();

    @FXML
    public void initialize() {
        uomoVisibleToggleButton.setToggleGroup(genderGroup);
        donnaVisibleToggleButton.setToggleGroup(genderGroup);

        setGenderControlsVisible(false);

        barberCheckBox.selectedProperty().addListener((obs, oldVal, isBarber) -> {
            setGenderControlsVisible(isBarber);
            if (!isBarber) genderGroup.selectToggle(null);
        });
    }

    private void setGenderControlsVisible(boolean visible) {
        visibleLabel.setVisible(visible);
        donnaVisibleToggleButton.setVisible(visible);
        uomoVisibleToggleButton.setVisible(visible);
    }

    @FXML
    public void registerButtonOnAction(ActionEvent event) {
        RegistrationBean bean = new RegistrationBean();
        bean.setFirstName(safeText(nomeTextField));
        bean.setLastName(safeText(cognomeTextField));
        bean.setEmail(safeText(emailTextField));
        bean.setPhoneNumber(safeText(telefonoTextField));
        bean.setPassword(safeText(setPasswordField));
        bean.setRepeatPassword(safeText(confirmPassword));
        bean.setUserType(barberCheckBox.isSelected() ? Role.BARBIERE : Role.CLIENTE);

        if (barberCheckBox.isSelected()) {
            if (genderGroup.getSelectedToggle() == null) {
                showError("Se sei barbiere devi selezionare Uomo o Donna.");
                return;
            }
            if (uomoVisibleToggleButton.isSelected()) bean.setSpecializzazione(GenderCategory.UOMO);
            else if (donnaVisibleToggleButton.isSelected()) bean.setSpecializzazione(GenderCategory.DONNA);
        }

        try {
            registrazioneController.register(bean);
            showInfo("Registrazione completata!");
            switchSafe("LoginView.fxml", "ZAC ZAC");

        } catch (ValidazioneException e) {
            showError(e.getMessage());

        } catch (DuplicateCredentialException e) {
            showError(e.getMessage());

        } catch (Exception ex) {
            showError("Errore tecnico durante la registrazione.");
        }
    }

    @FXML
    public void loginHyperlinkOnAction(ActionEvent event) {
        switchSafe("LoginView.fxml", "ZAC ZAC");
    }

    private static String safeText(TextInputControl c) {
        String s = (c == null) ? null : c.getText();
        return s == null ? "" : s.trim();
    }
}
