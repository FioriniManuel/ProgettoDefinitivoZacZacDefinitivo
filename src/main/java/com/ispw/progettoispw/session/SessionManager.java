package com.ispw.progettoispw.session;

import com.ispw.progettoispw.enu.Role;
import com.ispw.progettoispw.bean.BookingBean;

import java.util.Optional;

public final class SessionManager {
    private BookingBean currentBooking;


    // ---------- Singleton ----------
    private static volatile SessionManager instance;
    private SessionManager() {}
    public static SessionManager getInstance() {
        SessionManager ref = instance;
        if (ref == null) {
            synchronized (SessionManager.class) {
                ref = instance;
                if (ref == null) {
                    ref = instance = new SessionManager();
                }
            }
        }
        return ref;
    }

    // ---------- Stato ----------
    private Session currentSession;

    // ---------- API ----------
    /** Da chiamare SOLO dopo aver validato credenziali a monte (DAO/Service). */
    public synchronized void login(Session session) {
        this.currentSession = session;
    }

    /** Svuota la sessione corrente. */
    public synchronized void logout() {
        this.currentSession = null;
    }

    public boolean isAuthenticated() {
        return currentSession != null;
    }

    public Session getCurrentSession() {
        return currentSession;
    }

    public Optional<Role> getRole() {
        return Optional.ofNullable(currentSession).map(Session::getRole);
    }



public BookingBean getCurrentBooking() { return currentBooking; }
public void setCurrentBooking(BookingBean currentBooking) { this.currentBooking = currentBooking; }
public void clearCurrentBooking() { this.currentBooking = null; }



}



