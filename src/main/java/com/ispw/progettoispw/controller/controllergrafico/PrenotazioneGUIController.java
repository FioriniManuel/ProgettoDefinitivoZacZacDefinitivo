package com.ispw.progettoispw.controller.controllergrafico;

import com.ispw.progettoispw.controller.controllerapplicativo.BookingController;
import com.ispw.progettoispw.enu.GenderCategory;
import com.ispw.progettoispw.bean.BookingBean;
import com.ispw.progettoispw.entity.Servizio;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class PrenotazioneGUIController extends GraphicController {

    @FXML private StackPane root;

    private final BookingController bookingController = new BookingController();
    private final ToggleGroup servicesGroup = new ToggleGroup();

    private TabPane tabPane;
    private Tab tabUomo;
    private Tab tabDonna;
    private VBox uomoBox;
    private VBox donnaBox;

    private Servizio selected;

    @FXML
    private void initialize() {
        tabPane = (TabPane) root.lookup(".tab-pane");
        if (tabPane == null || tabPane.getTabs().size() < 2) {
            throw new IllegalStateException("TabPane (Uomo/Donna) non trovato.");
        }
        tabUomo = tabPane.getTabs().get(0);
        tabDonna = tabPane.getTabs().get(1);

        uomoBox = (VBox) getTabContent(tabUomo);
        donnaBox = (VBox) getTabContent(tabDonna);

        populateCategory(GenderCategory.UOMO, uomoBox);
        populateCategory(GenderCategory.DONNA, donnaBox);

        servicesGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT == null) {
                selected = null;
                tabUomo.setDisable(false);
                tabDonna.setDisable(false);
                return;
            }
            RadioButton rb = (RadioButton) newT;
            selected = (Servizio) rb.getUserData();

            if (selected.getCategory() == GenderCategory.UOMO) {
                tabDonna.setDisable(true);
                tabPane.getSelectionModel().select(tabUomo);
            } else {
                tabUomo.setDisable(true);
                tabPane.getSelectionModel().select(tabDonna);
            }
        });
    }

    private void populateCategory(GenderCategory cat, VBox box) {
        box.getChildren().clear();
        List<Servizio> servizi = bookingController.listServiziByCategory(cat);
        for (Servizio s : servizi) {
            RadioButton rb = new RadioButton(bookingController.buildServiceLabel(s));
            rb.setToggleGroup(servicesGroup);
            rb.setUserData(s);
            box.getChildren().add(rb);
        }
    }

    private Node getTabContent(Tab tab) {
        Node content = tab.getContent();
        if (content instanceof VBox v) return v;
        if (content instanceof ScrollPane sp && sp.getContent() instanceof VBox v2) return v2;
        return content;
    }

    private boolean hasSelection() { return selected != null; }

    private void fillBookingBeanBasics(BookingBean bean) {
        bean.setServiziId(selected.getServiceId());
        bean.setDurataTotaleMin(selected.getDuration());
        bean.setPrezzoTotale(selected.getPrice());
        bean.setCategoria(selected.getCategory());
        bean.setServiceName(selected.getName());
    }

    @FXML
    private void homeButtonOnAction() {
        switchSafe("HomeView.fxml", "Home");
    }

    @FXML
    private void continuaButtonOnAction() {
        if (!hasSelection()) {
            new Alert(Alert.AlertType.WARNING, "Seleziona un servizio.", ButtonType.OK).showAndWait();
            return;
        }

        BookingBean bean = new BookingBean();
        fillBookingBeanBasics(bean);

        bookingController.saveBookingToSession(bean);
        switchSafe("OrarioView.fxml", "Scegli orario");
    }
}
