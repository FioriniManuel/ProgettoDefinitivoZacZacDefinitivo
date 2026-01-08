package com.ispw.progettoispw.controller.controllerApplicativo;

import com.ispw.progettoispw.enu.StorageOption;
import com.ispw.progettoispw.factory.DaoFactory;

public class MainController {

    /**
     * Costruttore vuoto intenzionale.
     * Necessario per consentire l'istanziazione standard del controller
     * e mantenere il disaccoppiamento dalla logica di configurazione.
     */
    public MainController() {
        // intentionally empty
    }

    public void persistenza() {
        DaoFactory.setStorageOption(StorageOption.FILE);
    }

    public void memory() {
        DaoFactory.setStorageOption(StorageOption.INMEMORY);
    }
}
