package com.ispw.progettoispw.dao;

import com.ispw.progettoispw.entity.Appuntamento;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * DAO in-memory per Appuntamento con doppio indice:
 * - byId:        id -> Appuntamento
 * - bySlotKey:   barberId|date|slot -> appointmentId   (per bloccare collisioni sullo stesso slot)
 */
public class AppuntamentoDaoMemory implements GenericDao<Appuntamento> {

    /** Indice principale: id -> Appuntamento */
    private final Map<String, Appuntamento> byId = new ConcurrentHashMap<>();

    /** Indice slot: barberId|date|slot -> id appuntamento */
    private final Map<String, String> bySlotKey = new ConcurrentHashMap<>();

    /* ================== Helpers ================== */

    private static String slotKey(String barberId, LocalDate date, LocalTime slotIndex) {
        if (barberId == null || date == null) {
            throw new IllegalArgumentException("barberId e date obbligatori per la chiave slot");
        }
        return barberId + "|" + date + "|" + slotIndex;
    }


    @Override
    public void create(Appuntamento a) {
        if (a == null) throw new IllegalArgumentException("Appuntamento null");

        // assicura un id
        if (a.getId() == null || a.getId().isBlank()) {
            a.setId(UUID.randomUUID().toString());
        }

        // blocco collisioni slot in modo atomico
        final String key = slotKey(a.getBarberId(), a.getDate(), a.getSlotIndex());
        String prev = bySlotKey.putIfAbsent(key, a.getId());
        if (prev != null) {
            throw new IllegalStateException("Slot già occupato per questo barbiere");
        }

        // inserisci
        Appuntamento old = byId.putIfAbsent(a.getId(), a);
        if (old != null) {
            // rollback chiave slot se l'id era già presente
            bySlotKey.remove(key, a.getId());
            throw new IllegalArgumentException("Appuntamento già presente: " + a.getId());
        }
    }

    @Override
    public Appuntamento read(Object... keys) {
        if (keys.length != 1 || !(keys[0] instanceof String id)) {
            throw new IllegalArgumentException("Chiave id non valida");
        }
        Appuntamento a = byId.get(id);
        if (a == null) throw new NoSuchElementException("Appuntamento non trovato: " + id);
        return a;
    }

    @Override
    public void update(Appuntamento a) {
        if (a == null || a.getId() == null || a.getId().isBlank()) {
            throw new IllegalArgumentException("Appuntamento o id null");
        }
        Appuntamento current = byId.get(a.getId());
        if (current == null) throw new NoSuchElementException("Appuntamento inesistente: " + a.getId());

        // se è cambiato barber/date/slot, aggiorna l'indice slot con controllo collisioni
        boolean slotChanged =
                !Objects.equals(current.getBarberId(), a.getBarberId()) ||
                        !Objects.equals(current.getDate(),     a.getDate())     ||
                        current.getSlotIndex() != a.getSlotIndex();

        if (slotChanged) {
            // libera la vecchia chiave
            String oldKey = slotKey(current.getBarberId(), current.getDate(), current.getSlotIndex());
            bySlotKey.remove(oldKey, current.getId());

            // registra la nuova (se libera)
            String newKey = slotKey(a.getBarberId(), a.getDate(), a.getSlotIndex());
            String prev = bySlotKey.putIfAbsent(newKey, a.getId());
            if (prev != null && !prev.equals(a.getId())) {
                // ripristina la vecchia chiave per coerenza
                bySlotKey.put(oldKey, current.getId());
                throw new IllegalStateException("Nuovo slot già occupato per questo barbiere");
            }
        }

        byId.put(a.getId(), a);
    }

    @Override
    public void delete(Object... keys) {
        if (keys.length != 1 || !(keys[0] instanceof String id)) {
            throw new IllegalArgumentException("Chiave id non valida");
        }
        Appuntamento removed = byId.remove(id);
        if (removed != null) {
            String key = slotKey(removed.getBarberId(), removed.getDate(), removed.getSlotIndex());
            bySlotKey.remove(key, id);
        }
    }

    @Override
    public List<Appuntamento> readAll() {
        return byId.values().stream()
                .sorted(Comparator.comparing(Appuntamento::getDate)
                        .thenComparing(Appuntamento::getSlotIndex))
                .toList();
    }




}
