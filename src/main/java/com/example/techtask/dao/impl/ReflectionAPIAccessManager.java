package com.example.techtask.dao.impl;

import java.lang.reflect.Field;
import java.util.List;


class ReflectionAPIAccessManager {

    static void openFieldsAccess(List<Field> fieldList) {
        fieldList.forEach(f -> f.setAccessible(Boolean.TRUE));
    }

    static void closeFieldsAccess(List<Field> fieldList) {
        fieldList.forEach(f -> f.setAccessible(Boolean.FALSE));
    }

}
