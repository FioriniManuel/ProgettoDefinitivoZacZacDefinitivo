package com.ispw.progettoispw.Dao;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import com.ispw.progettoispw.Enum.AppointmentStatus;
import com.ispw.progettoispw.entity.Appuntamento;
import com.ispw.progettoispw.pattern.GsonProvider;

import java.io.*;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AppuntamentoDaoFile implements GenericDao<Appuntamento> {

    private static final String FILE_PATH = "appuntamenti.json";

    private final Gson gson;
    private final Object lock = new Object(); // per thread-safety basilare
    private List<Appuntamento> items;

    public AppuntamentoDaoFile() {
        this.gson = GsonProvider.get();
        this.items = loadFromFile();
    }

    /* =================== File I/O =================== */

    private List<Appuntamento> loadFromFile() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return new ArrayList<>();
        try (Reader r = new FileReader(file)) {
            Type listType = new TypeToken<List<Appuntamento>>() {}.getType();
            List<Appuntamento> loaded = gson.fromJson(r, listType);
            return loaded != null ? loaded : new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void saveToFile() {
        try (Writer w = new FileWriter(FILE_PATH)) {
            gson.toJson(items, w);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* =================== GenericDao =================== */

    @Override
    public void create(Appuntamento entity) {
        Objects.requireNonNull(entity, "Appuntamento nullo");
        synchronized (lock) {
            // evita duplicati per id
            if (entity.getId() == null || entity.getId().isBlank()) {
                // se serve, genera id qui (di solito lo fa Appuntamento.newWithId())
                throw new IllegalArgumentException("Id appuntamento mancante. Usa Appuntamento.newWithId().");
            }
            if (read(entity.getId()) != null) {
                throw new IllegalArgumentException("Appuntamento già esistente: " + entity.getId());
            }
            items.add(entity);
            saveToFile();
        }
    }

    @Override
    public Appuntamento read(Object... keys) {
        if (keys == null || keys.length != 1 || !(keys[0] instanceof String id)) {
            throw new IllegalArgumentException("Chiave non valida: atteso un solo String id");
        }
        synchronized (lock) {
            for (Appuntamento a : items) {
                if (id.equals(a.getId())) return a;
            }
        }
        return null;
    }

    @Override
    public void update(Appuntamento entity) {
        Objects.requireNonNull(entity, "Appuntamento nullo");
        String id = entity.getId();
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Id mancante in update()");
        }
        synchronized (lock) {
            for (int i = 0; i < items.size(); i++) {
                if (id.equals(items.get(i).getId())) {
                    items.set(i, entity);
                    saveToFile();
                    return;
                }
            }
        }
        throw new IllegalArgumentException("Appuntamento non trovato: " + id);
    }

    @Override
    public void delete(Object... keys) {
        if (keys == null || keys.length != 1 || !(keys[0] instanceof String id)) {
            throw new IllegalArgumentException("Chiave non valida: atteso un solo String id");
        }
        synchronized (lock) {
            items.removeIf(a -> id.equals(a.getId()));
            saveToFile();
        }
    }

    @Override
    public List<Appuntamento> readAll() {
        synchronized (lock) {
            return new ArrayList<>(items);
        }
    }






}
