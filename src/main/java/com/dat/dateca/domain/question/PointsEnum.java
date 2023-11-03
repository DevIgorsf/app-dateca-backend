package com.dat.dateca.domain.question;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum PointsEnum {
    EASY(5, "Fácil"),
    REGULAR(7, "Médio"),
    HARD(10, "Difícil");

    private int key;
    private String description;

    PointsEnum(int key, String description) {
        this.key = key;
        this.description = description;
    }

    public int getKey() {
        return key;
    }

    public String getDescription() {
        return description;
    }

    public static final Map<Integer, PointsEnum> pointsEnumMap = new HashMap<Integer, PointsEnum>();
    static {
        for (PointsEnum pointsEnum : EnumSet.allOf(PointsEnum.class)) {
            pointsEnumMap.put(pointsEnum.getKey(), pointsEnum);
        }
    }

    public static PointsEnum get(String string) {
        return pointsEnumMap.get(string);
    }

    @JsonCreator
    public static PointsEnum fromString(String value) {
        for (PointsEnum pointsEnum : PointsEnum.values()) {
            if (pointsEnum.getDescription().equalsIgnoreCase(value)) {
                return pointsEnum;
            }
        }
        throw new IllegalArgumentException("Invalid PointsEnum value: " + value);
    }

    }
