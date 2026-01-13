package com.ispw.progettoispw.dao;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ispw.progettoispw.entity.Barbiere;
import com.ispw.progettoispw.pattern.GsonProvider;


import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BarbiereDaoFile implements GenericDao<Barbiere> {
    private static final String FILE_PATH = "barbiere.json";
    private final Gson gson;
    private List<Barbiere> barbiere;
    private final Logger logger = Logger.getLogger(getClass().getName());
    public BarbiereDaoFile() {
        this.gson = GsonProvider.get();
        barbiere = loadFromFile();
    }

    private List<Barbiere> loadFromFile() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (Reader reader = new FileReader(FILE_PATH)) {
            Type listType = new TypeToken<List<Barbiere>>() {}.getType();
            List<Barbiere> loadedClients = gson.fromJson(reader, listType);
            return loadedClients != null ? loadedClients : new ArrayList<>();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Errore durante la lettura dei dati", e);
            return new ArrayList<>();
        }
    }

    private void saveToFile() {
        try (Writer writer = new FileWriter(FILE_PATH)) {
            gson.toJson(barbiere, writer);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Errore durante la lettura dei dati", e);
        }
    }

    @Override
    public void create(Barbiere entity) {
        if (read(entity.getEmail()) != null) {
            throw new IllegalArgumentException("Client already exists: " + entity.getEmail());
        }
        if (entity.getId() == null || entity.getId().isEmpty()) {
            entity.setId(UUID.randomUUID().toString());
        }
        barbiere.add(entity);
        saveToFile();
    }

    @Override
    public Barbiere read(Object... keys) {
        if (keys.length != 1 || !(keys[0] instanceof String)) {
            throw new IllegalArgumentException("Invalid keys for reading Client.");
        }
        String email = (String) keys[0];

        return barbiere.stream()
                .filter(b-> b.getEmail().equals(email))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void update(Barbiere entity) {
        for (int i = 0; i < barbiere.size(); i++) {
            if (barbiere.get(i).getEmail().equals(entity.getEmail())) {
                barbiere.set(i, entity);
                saveToFile();
                return;
            }
        }
        throw new IllegalArgumentException("Barbiere not found: " + entity.getEmail());
    }

    @Override
    public void delete(Object... keys) {
        if (keys.length != 1 || !(keys[0] instanceof String)) {
            throw new IllegalArgumentException("Invalid keys for deleting Client.");
        }
        String email = (String) keys[0];

        barbiere.removeIf(b -> b.getEmail().equals(email));
        saveToFile();
    }

    @Override
    public List<Barbiere> readAll() {
        return new ArrayList<>(barbiere);
    }
}

