package com.ispw.progettoispw.factory;

import com.ispw.progettoispw.dao.*;
import com.ispw.progettoispw.dao.GenericDao;
import com.ispw.progettoispw.dao.ReadOnlyDao;
import com.ispw.progettoispw.enu.StorageOption;
import com.ispw.progettoispw.entity.*;

 public final class DaoFactory implements DaoFactoryInterface {

    private static StorageOption storage=StorageOption.FILE;

    // Singleton
    private static volatile DaoFactory instance;

     private final GenericDao<Cliente> clienteDao;
     private final GenericDao<Barbiere> barbiereDao;
     private final ReadOnlyDao<Servizio> servizioDao;
     private final GenericDao<Appuntamento> appuntamentoDao;
     private final GenericDao<LoyaltyAccount> loyaltyAccountDao;
     private final GenericDao<PersonalCoupon> personalCouponDao;
     private final ReadOnlyDao<PrizeOption> prizeOptionDao;


     private DaoFactory() {
         // inizializza una sola volta in base a storage
         if (storage == StorageOption.INMEMORY) {
             clienteDao        = new ClienteDaoMemory();
             barbiereDao       = new BarbiereDaoMemory();
             servizioDao       = new ServizioDaoMemory();
             appuntamentoDao   = new AppuntamentoDaoMemory();
             loyaltyAccountDao = new LoyaltyAccountDaoMemory();
             personalCouponDao = new PersonalCouponDaoMemory();
             prizeOptionDao    = new PrizeOptionDaoMemory();

         } else { // FILE (default)
             clienteDao        = new ClienteDaoFile();
             barbiereDao       = new BarbiereDaoFile();
             servizioDao       = new ServizioDaoFile();
             appuntamentoDao   = new AppuntamentoDaoFile();
             loyaltyAccountDao = new LoyaltyAccountDaoFile();
             personalCouponDao = new PersonalCouponDaoFile();
             prizeOptionDao    = new PrizeOptionDaoFile();
         }
     }


     public static DaoFactory getInstance() {
        if (instance == null) {
            synchronized (DaoFactory.class) {
                if (instance == null) instance = new DaoFactory();
            }
        }
        return instance;
    }

    // getters: ora restituiscono sempre le stesse istanze
    @Override public GenericDao<Cliente> getClienteDao() { return clienteDao; }
    @Override public GenericDao<Barbiere> getBarbiereDao() { return barbiereDao; }
    @Override public ReadOnlyDao<Servizio> getServizioDao() { return servizioDao; }

    @Override public GenericDao<Appuntamento> getAppuntamentoDao() { return appuntamentoDao; }

    @Override public GenericDao<LoyaltyAccount> getLoyaltyAccountDao() { return loyaltyAccountDao; }
    @Override public GenericDao<PersonalCoupon> getPersonalCouponDao() { return personalCouponDao; }
     @Override public ReadOnlyDao<PrizeOption>  getPrizeOptionDao() {return prizeOptionDao;}

    public  static void setStorageOption(StorageOption option) {
        if (option != null && instance == null) {
            storage = option; // deve essere impostato PRIMA della prima getInstance()
        }
    }
}
