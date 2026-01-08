package com.ispw.progettoispw.ControllerApplicativo;

import com.ispw.progettoispw.Controller.ControllerApplicativo.RegistrazioneController;
import com.ispw.progettoispw.Dao.GenericDao;
import com.ispw.progettoispw.Enum.GenderCategory;
import com.ispw.progettoispw.Enum.Role;
import com.ispw.progettoispw.Exception.DuplicateCredentialException;
import com.ispw.progettoispw.Exception.ValidazioneException;
import com.ispw.progettoispw.Factory.DaoFactory;
import com.ispw.progettoispw.bean.RegistrationBean;
import com.ispw.progettoispw.entity.Barbiere;
import com.ispw.progettoispw.entity.Cliente;
import com.ispw.progettoispw.entity.LoyaltyAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RegistrationControllerTest {

    private RegistrazioneController controller;
    private GenericDao<Cliente> clienteDao;
    private GenericDao<Barbiere> barbiereDao;
    private GenericDao<LoyaltyAccount> loyaltyDao;

    @BeforeEach
    void setUp() {
        controller = new RegistrazioneController();
        DaoFactory f = DaoFactory.getInstance();
        clienteDao  = f.getClienteDao();
        barbiereDao = f.getBarbiereDao();
        loyaltyDao  = f.getLoyaltyAccountDao();

        // pulizia sandbox
        clienteDao.readAll().forEach(c -> clienteDao.delete(c.getId()));
        barbiereDao.readAll().forEach(b -> barbiereDao.delete(b.getId()));
        loyaltyDao.readAll().forEach(a -> loyaltyDao.delete(a.getClientId()));
    }

    // ---------- helpers ----------
    private RegistrationBean makeClientBean(String email, String phone) {
        RegistrationBean b = new RegistrationBean();
        b.setUserType(Role.CLIENTE);
        b.setFirstName("Mario");
        b.setLastName("Rossi");
        b.setEmail(email);
        b.setPhoneNumber(phone);
        b.setPassword("pwd123");
        b.setRepeatPassword("pwd123");
        return b;
    }

    private RegistrationBean makeBarberBean(String email, String phone, GenderCategory spec) {
        RegistrationBean b = new RegistrationBean();
        b.setUserType(Role.BARBIERE);
        b.setFirstName("Luigi");
        b.setLastName("Bianchi");
        b.setEmail(email);
        b.setPhoneNumber(phone);
        b.setPassword("pwd456");
        b.setRepeatPassword("pwd456");
        b.setSpecializzazione(spec);
        return b;
    }

    private Optional<Cliente> findClienteByEmail(String email) {
        String norm = email == null ? null : email.trim().toLowerCase();
        return clienteDao.readAll().stream()
                .filter(c -> c.getEmail() != null && c.getEmail().trim().toLowerCase().equals(norm))
                .findFirst();
    }

    private Optional<Barbiere> findBarbiereByEmail(String email) {
        String norm = email == null ? null : email.trim().toLowerCase();
        return barbiereDao.readAll().stream()
                .filter(b -> b.getEmail() != null && b.getEmail().trim().toLowerCase().equals(norm))
                .findFirst();
    }

    // ---------- tests ----------

    @Test
    void registerCliente_success_creaLoyaltyAccount() throws Exception {
        RegistrationBean bean = makeClientBean("m.rossi@example.com", "333 123 4567");

        assertDoesNotThrow(() -> controller.register(bean));

        Cliente c = findClienteByEmail("m.rossi@example.com").orElse(null);
        assertNotNull(c, "Il cliente deve essere stato creato");

        LoyaltyAccount acc = loyaltyDao.read(c.getId());
        assertNotNull(acc, "LoyaltyAccount deve esistere per il nuovo cliente");
        assertEquals(c.getId(), acc.getClientId());
    }

    @Test
    void registerCliente_fallisce_seEmailGiaUsataDaBarbiere() {
        // seed barbiere
        Barbiere b = new Barbiere();
        b.setFirstName("Test");
        b.setLastName("Barber");
        b.setEmail("dup@example.com");
        b.setphoneNumber("3330001111");
        b.setPassword("x");
        b.setSpecializzazione(GenderCategory.UOMO);
        b.setActive(true);
        barbiereDao.create(b);

        RegistrationBean bean = makeClientBean("dup@example.com", "3332223333");

        DuplicateCredentialException ex = assertThrows(
                DuplicateCredentialException.class,
                () -> controller.register(bean)
        );

        // opzionale: se nel tuo DuplicateCredentialException metti un messaggio chiaro
        // assertTrue(ex.getMessage().toLowerCase().contains("email"));

        // assicurati che NON abbia creato il cliente
        assertTrue(findClienteByEmail("dup@example.com").isEmpty());
    }

    @Test
    void registerBarbiere_success_activeTrueESpecializzazione() throws Exception {
        RegistrationBean bean = makeBarberBean("barber@example.com", "+39 333-987-6543", GenderCategory.DONNA);

        assertDoesNotThrow(() -> controller.register(bean));

        Barbiere saved = findBarbiereByEmail("barber@example.com").orElse(null);
        assertNotNull(saved, "Il barbiere deve essere stato creato");
        assertTrue(saved.isActive(), "Il barbiere deve essere attivo di default");
        assertEquals(GenderCategory.DONNA, saved.getSpecializzazione(), "Specializzazione corretta");
    }

    @Test
    void registerBarbiere_fallisce_seTelefonoGiaUsato_daCliente_conNormalizzazione() {
        // seed cliente con telefono “formattato”
        Cliente c = new Cliente();
        c.setFirstName("Anna");
        c.setLastName("Verdi");
        c.setEmail("anna@example.com");
        c.setphoneNumber("+39 (333) 111-2222"); // verrà normalizzato nel controller quando confronta
        c.setPassword("x");
        clienteDao.create(c);

        // barbiere che prova con lo stesso numero (normalizzato)
        RegistrationBean bean = makeBarberBean("newbarb@example.com", "3331112222", GenderCategory.UOMO);

        assertThrows(DuplicateCredentialException.class, () -> controller.register(bean));

        // assicurati che NON sia stato creato il barbiere
        assertTrue(findBarbiereByEmail("newbarb@example.com").isEmpty());
    }

    @Test
    void register_validationError_passwordMismatch() {
        RegistrationBean bean = makeClientBean("bad@example.com", "3334445555");
        bean.setRepeatPassword("diversa");

        ValidazioneException ex = assertThrows(
                ValidazioneException.class,
                () -> controller.register(bean)
        );

        // opzionale: se vuoi controllare che contenga qualcosa di utile
        // assertTrue(ex.getMessage().toLowerCase().contains("password"));

        assertTrue(findClienteByEmail("bad@example.com").isEmpty());
    }

    @Test
    void register_validationError_tipoUtenteNonValido() {
        RegistrationBean bean = new RegistrationBean();
        // NON setto userType volutamente
        bean.setFirstName("X");
        bean.setLastName("Y");
        bean.setEmail("x@y.it");
        bean.setPhoneNumber("3330009999");
        bean.setPassword("pwd123");
        bean.setRepeatPassword("pwd123");

        assertThrows(ValidazioneException.class, () -> controller.register(bean));
    }
}
