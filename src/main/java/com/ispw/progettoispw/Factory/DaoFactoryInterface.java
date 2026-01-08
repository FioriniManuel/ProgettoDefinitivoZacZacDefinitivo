package com.ispw.progettoispw.Factory;

import com.ispw.progettoispw.Dao.GenericDao;
import com.ispw.progettoispw.Dao.ReadOnlyDao;
import com.ispw.progettoispw.entity.*;

public interface DaoFactoryInterface {

    GenericDao<Cliente>        getClienteDao();
    GenericDao<Barbiere>       getBarbiereDao();
    ReadOnlyDao<Servizio> getServizioDao();

    GenericDao<Appuntamento>   getAppuntamentoDao();

    GenericDao<LoyaltyAccount> getLoyaltyAccountDao();
    GenericDao<PersonalCoupon> getPersonalCouponDao();
    ReadOnlyDao<PrizeOption>   getPrizeOptionDao();
}
