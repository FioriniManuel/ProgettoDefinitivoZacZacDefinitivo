package com.ispw.progettoispw.entity;

import com.ispw.progettoispw.enu.Role;

public class Cliente extends User{



    public Cliente() {}
    public Cliente(String nome,String email, String cognome, String numerodiTelefono,String password,String clienteid) {
        super(nome, email, cognome, numerodiTelefono, password,clienteid);
    }

    @Override
    public Role getRole() {
        return Role.CLIENTE;
    }
}
