package com.ispw.progettoispw.controller.controllerapplicativo;

import com.ispw.progettoispw.dao.GenericDao;
import com.ispw.progettoispw.enu.Role;
import com.ispw.progettoispw.exception.AutenticazioneException;
import com.ispw.progettoispw.exception.ValidazioneException;
import com.ispw.progettoispw.factory.DaoFactory;
import com.ispw.progettoispw.session.Session;
import com.ispw.progettoispw.session.SessionManager;
import com.ispw.progettoispw.bean.LoginBean;
import com.ispw.progettoispw.entity.Barbiere;
import com.ispw.progettoispw.entity.Cliente;
import com.ispw.progettoispw.entity.User;

import java.util.Objects;
import java.util.logging.Logger;

public class LoginController {

    private final Logger logger = Logger.getLogger(getClass().getName());
    private final GenericDao<Cliente> clienteDao;
    private final GenericDao<Barbiere> barbiereDao;

    public LoginController() {
        DaoFactory factory = DaoFactory.getInstance();
        this.clienteDao  = factory.getClienteDao();
        this.barbiereDao = factory.getBarbiereDao();
    }

    /**
     * Autentica l’utente.
     * @return Role.CLIENTE o Role.BARBIERE se login OK
     * @throws ValidazioneException se email/password mancanti
     * @throws AutenticazioneException se utente non trovato o password errata
     */
    public Role login(LoginBean bean) throws ValidazioneException, AutenticazioneException {

        if (!basicValidate(bean)) {
            throw new ValidazioneException("Inserisci email e password.");
        }

        final String emailNorm = normalizeEmail(bean.getEmail());
        final String rawPassword = Objects.requireNonNullElse(bean.getPassword(), "");
        final Role requestedRole = (bean.getUserType() != null) ? bean.getUserType() : Role.CLIENTE;

        if (requestedRole == Role.BARBIERE) {
            Barbiere b = findBarbiereByEmail(emailNorm);
            if (b == null) {
                throw new AutenticazioneException("Utente non trovato. Controlla l'email o registrati.");
            }
            if (!passwordMatches(b.getPassword(), rawPassword)) {
                throw new AutenticazioneException("Credenziali errate. Riprova.");
            }
            openSession(b, Role.BARBIERE);
            return Role.BARBIERE;

        } else { // CLIENTE
            Cliente c = findClienteByEmail(emailNorm);
            if (c == null) {
                throw new AutenticazioneException("Utente non trovato. Controlla l'email o registrati.");
            }
            if (!passwordMatches(c.getPassword(), rawPassword)) {
                throw new AutenticazioneException("Credenziali errate. Riprova.");
            }
            openSession(c, Role.CLIENTE);
            return Role.CLIENTE;
        }
    }

    private boolean basicValidate(LoginBean bean) {
        if (bean == null) return false;
        String email = bean.getEmail();
        String pwd   = bean.getPassword();
        if (email == null || email.isBlank()) return false;
        if (pwd == null || pwd.isBlank())     return false;
        return true;
    }

    private Cliente findClienteByEmail(String emailNorm) {
        return clienteDao.readAll().stream()
                .filter(u -> u.getEmail() != null && emailNorm.equals(normalizeEmail(u.getEmail())))
                .findFirst()
                .orElse(null);
    }

    private Barbiere findBarbiereByEmail(String emailNorm) {
        return barbiereDao.readAll().stream()
                .filter(u -> u.getEmail() != null && emailNorm.equals(normalizeEmail(u.getEmail())))
                .findFirst()
                .orElse(null);
    }

    private void openSession(User user, Role role) {
        Session session = new Session(
                user.getphoneNumber(),
                user.getId(),
                user.getEmail(),
                user.getFirstName() + " " + user.getLastName(),
                role
        );

        SessionManager.getInstance().login(session);
        logger.info(String.format("Login OK: %s (%s) %s", user.getEmail(), role, user.getId()));

    }

    private static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private static boolean passwordMatches(String stored, String rawInput) {
        return Objects.equals(stored, rawInput);
    }

    // ---- tuoi metodi rimangono invariati ----
    public static String getName(){
        Session session = SessionManager.getInstance().getCurrentSession();
        if(session == null) return null;
        return session.getDisplayName();
    }

    public static String getId(){
        Session session = SessionManager.getInstance().getCurrentSession();
        if(session == null) return null;
        return session.getId();
    }

    public static synchronized void logOut(){
        SessionManager.getInstance().logout();
    }

    public String getEmail() {
        Session session = SessionManager.getInstance().getCurrentSession();
        if(session == null) return null;
        return session.getEmail();
    }
}
