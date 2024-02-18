package com.arturjarosz.task.supplier.model;

public enum SupplierCategory {
    SHOP_WITH_BATHROOM_FITTINGS("shopWithBathroomFittings"),
    SHOP_WITH_TILES("shopWithTiles"),
    BATHROOM_CERAMICS_SHOP("bathroomCeramicsShop"),
    PAINT_SHOP("painShop"),
    FLOORING_SHOP("flooringShop"),
    GENERAL_SHOP("generalShop"),
    LIGHTING_SHOP("lightingShop"),
    FURNITURE_SHOP("furnitureShop");

    private final String name;

    SupplierCategory(String name) {
        this.name = name;
    }
}
