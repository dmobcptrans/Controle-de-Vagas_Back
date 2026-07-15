package com.cptrans.petrocarga.shared.utils;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;

public class Utils {
    public static Expression<String> createUnaccentExpression(CriteriaBuilder cb, Expression<?> expression) {
        if (cb == null || expression == null) return null;
        return cb.function("unaccent", String.class, expression);
    }
}