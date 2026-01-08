package com.ispw.progettoispw.controller.controllerApplicativo;

import com.ispw.progettoispw.enu.StorageOption;
import com.ispw.progettoispw.factory.DaoFactory;

public class MainController {

    public  MainController(){}
    public void persistenza(){
        DaoFactory.setStorageOption(StorageOption.FILE);

    }

    public void memory() {
        DaoFactory.setStorageOption(StorageOption.INMEMORY);
    }
}
