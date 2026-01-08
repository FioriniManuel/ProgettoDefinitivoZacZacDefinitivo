package com.ispw.progettoispw.controller.controllerGrafico.alternative;

import com.ispw.progettoispw.controller.controllerApplicativo.BookingController;
import com.ispw.progettoispw.controller.controllerApplicativo.LoginController;
import com.ispw.progettoispw.controller.controllerGrafico.GraphicController;
import com.ispw.progettoispw.enu.GenderCategory;
import com.ispw.progettoispw.bean.BarbiereBean;
import com.ispw.progettoispw.bean.BookingBean;
import com.ispw.progettoispw.bean.ServizioBean;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;
import java.util.List;

public class PrenotazioniGUIAlternativeController extends GraphicController {

    @FXML private ListView<ServizioBean> uomoList;
    @FXML private ListView<ServizioBean> donnaList;
    @FXML private ComboBox<BarbiereBean> barberCombo;

    private final BookingController bookingController = new BookingController();

    private String selectedServiceId = null;
    private GenderCategory selectedGender = null;

    @FXML
    public void initialize() {
        uomoList.setItems(FXCollections.observableArrayList(
                bookingController.listServiziByCategoryVM(GenderCategory.UOMO)
        ));
        donnaList.setItems(FXCollections.observableArrayList(
                bookingController.listServiziByCategoryVM(GenderCategory.DONNA)
        ));

        uomoList.setPlaceholder(new Label("Nessun servizio uomo disponibile."));
        donnaList.setPlaceholder(new Label("Nessun servizio donna disponibile."));

        uomoList.setCellFactory(lv -> new ServiceCell());
        donnaList.setCellFactory(lv -> new ServiceCell());

        barberCombo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(BarbiereBean b, boolean empty) {
                super.updateItem(b, empty);
                setText(empty || b == null ? null : b.getDisplayName());
            }
        });
        barberCombo.setCellFactory(cb -> new ListCell<>() {
            @Override protected void updateItem(BarbiereBean b, boolean empty) {
                super.updateItem(b, empty);
                setText(empty || b == null ? null : b.getDisplayName());
            }
        });
        barberCombo.setPromptText("Seleziona un barbiere");
        barberCombo.setDisable(true);
    }

    private void refreshLists() {
        uomoList.refresh();
        donnaList.refresh();
    }

    private void loadBarbersBySelectedGender() {
        if (selectedGender == null) {
            barberCombo.getItems().clear();
            barberCombo.setDisable(true);
            return;
        }

        List<BarbiereBean> barbers = bookingController.listBarbersByGenderVM(selectedGender);
        barberCombo.getItems().setAll(barbers);

        boolean empty = barbers.isEmpty();
        barberCombo.setDisable(empty);
        barberCombo.setPromptText(empty ? "Nessun barbiere per questa specializzazione" : "Seleziona un barbiere");
        if (!empty) barberCombo.getSelectionModel().clearSelection();
    }

    private class ServiceCell extends ListCell<ServizioBean> {
        private final Label label = new Label();
        private final Button confirmBtn = new Button("Conferma");
        private final HBox box = new HBox(12, label, confirmBtn);

        ServiceCell() {
            confirmBtn.setOnAction(e -> {
                ServizioBean s = getItem();
                if (s == null) return;

                if (s.getId().equals(selectedServiceId)) {
                    selectedServiceId = null;
                    selectedGender = null;
                    loadBarbersBySelectedGender();
                    refreshLists();
                    return;
                }

                selectedServiceId = s.getId();
                selectedGender = (getListView() == uomoList) ? GenderCategory.UOMO : GenderCategory.DONNA;

                loadBarbersBySelectedGender();
                refreshLists();
            });
        }

        @Override
        protected void updateItem(ServizioBean s, boolean empty) {
            super.updateItem(s, empty);
            if (empty || s == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            label.setText(bookingController.buildServiceLabel(s));

            boolean otherSelected = selectedServiceId != null && !s.getId().equals(selectedServiceId);
            confirmBtn.setDisable(otherSelected);
            box.setOpacity(otherSelected ? 0.55 : 1.0);

            setText(null);
            setGraphic(box);
        }
    }

    @FXML
    private void onBack() {
        switchSafe("HomeViewAlternative.fxml", "Home");
    }

    @FXML
    private void onContinua() {
        if (selectedServiceId == null) {
            new Alert(Alert.AlertType.WARNING, "Seleziona un servizio e confermalo.").showAndWait();
            return;
        }

        BarbiereBean selectedBarber = barberCombo.getSelectionModel().getSelectedItem();
        if (selectedBarber == null) {
            new Alert(Alert.AlertType.WARNING, "Seleziona un professionista.").showAndWait();
            return;
        }

        BookingBean bean = new BookingBean();
        bean.setServiziId(selectedServiceId);
        bean.setBarbiereId(selectedBarber.getId());
        bean.setBarbiereDisplay(selectedBarber.getDisplayName());
        bean.setCategoria(selectedGender);

        ServizioBean sVM = bookingController.getServizioVM(selectedServiceId);
        if (sVM != null) {
            bean.setServiceName(sVM.getName());
            bean.setDurataTotaleMin(sVM.getDurationMin());
            BigDecimal price = (sVM.getPrice() == null) ? BigDecimal.ZERO : sVM.getPrice();
            bean.setPrezzoTotale(price);
        }

        String clientId = LoginController.getId();
        if (clientId != null && !clientId.isBlank()) {
            bean.setClienteId(clientId);
        }

        bookingController.saveBookingToSession(bean);
        switchSafe("OrarioViewAlternative.fxml", "Scegli Orario");
    }
}
