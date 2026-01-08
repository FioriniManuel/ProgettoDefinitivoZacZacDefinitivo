package com.ispw.progettoispw.factory;

import com.ispw.progettoispw.dao.GenericDao;
import com.ispw.progettoispw.dao.ReadOnlyDao;
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
