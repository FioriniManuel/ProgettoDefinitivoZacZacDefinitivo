package com.ispw.progettoispw.factory;

import com.ispw.progettoispw.dao.*;
import com.ispw.progettoispw.enu.StorageOption;
import com.ispw.progettoispw.entity.*;

public final class DaoFactory implements DaoFactoryInterface {

    private static StorageOption storage = StorageOption.FILE;

    private GenericDao<Cliente> clienteDao;
    private GenericDao<Barbiere> barbiereDao;
    private ReadOnlyDao<Servizio> servizioDao;

    private GenericDao<Appuntamento> appuntamentoDao;

    private GenericDao<LoyaltyAccount> loyaltyAccountDao;
    private GenericDao<PersonalCoupon> personalCouponDao;
    private ReadOnlyDao<PrizeOption> prizeOptionDao;

    /**
     * Costruttore privato: Singleton necessario per garantire una sola
     * configurazione di persistenza (StorageOption) e un set coerente di DAO.
     * L'inizializzazione è lazy e thread-safe tramite Holder idiom.
     */
    private DaoFactory() {
        initDaos(storage);
    }

    private void initDaos(StorageOption option) {
        switch (option) {
            case INMEMORY -> {
                clienteDao = new ClienteDaoMemory();
                barbiereDao = new BarbiereDaoMemory();
                servizioDao = new ServizioDaoMemory();

                appuntamentoDao = new AppuntamentoDaoMemory();

                loyaltyAccountDao = new LoyaltyAccountDaoMemory();
                personalCouponDao = new PersonalCouponDaoMemory();
                prizeOptionDao = new PrizeOptionDaoMemory();
            }
            case FILE -> {
                clienteDao = new ClienteDaoFile();
                barbiereDao = new BarbiereDaoFile();
                servizioDao = new ServizioDaoFile();

                appuntamentoDao = new AppuntamentoDaoFile();

                loyaltyAccountDao = new LoyaltyAccountDaoFile();
                personalCouponDao = new PersonalCouponDaoFile();
                prizeOptionDao = new PrizeOptionDaoFile();
            }
        }
    }

    // Lazy + thread-safe (niente volatile, niente double-check locking)
    private static final class Holder {
        private static final DaoFactory INSTANCE = new DaoFactory();
    }

    public static DaoFactory getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Imposta lo storage PRIMA della prima chiamata a getInstance().
     * Dopo la creazione dell'istanza non è più modificabile, per evitare
     * incoerenze (DAO già istanziati su storage diverso).
     */
    public static synchronized void setStorageOption(StorageOption option) {
        if (option == null) return;

        // Se l'istanza è già stata creata, impediamo il cambio a runtime
        if (isInitialized()) {
            throw new IllegalStateException(
                    "StorageOption non modificabile dopo l'inizializzazione del DaoFactory"
            );
        }
        storage = option;
    }

    // Trucco semplice per capire se Holder.INSTANCE è già stato inizializzato
    // senza forzare la creazione (evita side effects).
    private static boolean isInitialized() {
        try {
            Class.forName(DaoFactory.Holder.class.getName(), false, DaoFactory.class.getClassLoader());
            return false; // il caricamento della classe Holder non forza INSTANCE
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    // getters: restituiscono sempre le stesse istanze
    @Override public GenericDao<Cliente> getClienteDao() { return clienteDao; }
    @Override public GenericDao<Barbiere> getBarbiereDao() { return barbiereDao; }
    @Override public ReadOnlyDao<Servizio> getServizioDao() { return servizioDao; }

    @Override public GenericDao<Appuntamento> getAppuntamentoDao() { return appuntamentoDao; }

    @Override public GenericDao<LoyaltyAccount> getLoyaltyAccountDao() { return loyaltyAccountDao; }
    @Override public GenericDao<PersonalCoupon> getPersonalCouponDao() { return personalCouponDao; }
    @Override public ReadOnlyDao<PrizeOption> getPrizeOptionDao() { return prizeOptionDao; }
}
