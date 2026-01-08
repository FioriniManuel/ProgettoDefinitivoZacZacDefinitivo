package com.ispw.progettoispw.dao;

import java.util.List;

public interface GenericDao<T>{
    void create (T entity);
    T read (Object... keys);
    void update(T entity);
    void delete (Object... keys);
    List<T> readAll();
}
