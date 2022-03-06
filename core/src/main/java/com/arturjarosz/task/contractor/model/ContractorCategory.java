package com.arturjarosz.task.contractor.model;

public enum ContractorCategory {

    GENERAL("general"),
    PLUMBER("plumber"),
    ELECTRICIAN("electrician"),
    TILER("tiler"),
    CARPENTER("carpenter"),
    ARTIST("artist");

    private final String name;

    ContractorCategory(String name) {
        this.name = name;
    }
}
