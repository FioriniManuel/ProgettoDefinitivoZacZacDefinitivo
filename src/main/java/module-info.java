module com.ispw.progettoispw {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;
    requires com.google.gson;


    opens com.ispw.progettoispw to javafx.fxml;
    exports com.ispw.progettoispw;
    opens com.ispw.progettoispw.entity to com.google.gson, javafx.fxml;
    exports com.ispw.progettoispw.entity;



    exports com.ispw.progettoispw.controller.controllerGrafico;
    opens com.ispw.progettoispw.controller.controllerGrafico to javafx.fxml;

    exports com.ispw.progettoispw.enu;
    opens com.ispw.progettoispw.enu to javafx.fxml;






    opens com.ispw.progettoispw.dao to com.google.gson;
    exports com.ispw.progettoispw.controller.controllerGrafico.alternative;
    opens com.ispw.progettoispw.controller.controllerGrafico.alternative to javafx.fxml;
}