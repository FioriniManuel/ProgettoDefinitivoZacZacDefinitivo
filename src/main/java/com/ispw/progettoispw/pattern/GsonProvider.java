package com.ispw.progettoispw.pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ispw.progettoispw.Adapter.LocalDateAdapter;
import com.ispw.progettoispw.Adapter.LocalTimeAdapter;


import java.time.LocalDate;
import java.time.LocalTime;

public final class GsonProvider {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
            .setPrettyPrinting()
            .create();

    private GsonProvider() {}

    public static Gson get() {
        return GSON;
    }
}
