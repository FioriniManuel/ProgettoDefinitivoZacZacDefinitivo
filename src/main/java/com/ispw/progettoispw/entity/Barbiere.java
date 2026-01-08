package com.ispw.progettoispw.entity;

import com.ispw.progettoispw.enu.GenderCategory;
import com.ispw.progettoispw.enu.Role;

public class Barbiere extends User {
        private GenderCategory specializzazione;
        private boolean active = true;

        public Barbiere() {}
        public Barbiere(String nome,String email, String cognome, String numerodiTelefono,String password,String BarberId,GenderCategory specializzazione) {
            super(nome, email, cognome, numerodiTelefono, password,BarberId);

            this.specializzazione= specializzazione;
        }

    public boolean isActive() {return active;}
    public GenderCategory getSpecializzazione() { return specializzazione;}

    public void setActive(boolean active) {this.active = active;}
    public void setSpecializzazione(GenderCategory specializzazione){ this.specializzazione= specializzazione;}

    @Override
        public Role getRole() {
            return Role.BARBIERE;
        }

}
