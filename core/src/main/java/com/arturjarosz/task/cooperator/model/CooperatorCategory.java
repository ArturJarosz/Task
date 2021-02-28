package com.arturjarosz.task.cooperator.model;

public enum CooperatorCategory {
    //Supplier types
    SHOP_WITH_BATHROOM_FITTINGS("shopWithBathroomFittings"),
    SHOP_WITH_TILES("shopWithTiles"),
    BATHROOM_CERAMICS_SHOP("bathroomCeramicsShop"),
    PAINT_SHOP("painShop"),
    FLOORING_SHOP("flooringShop"),
    LIGHTING_SHOP("lightingShop"),

    //Contractor types
    GENERAL("general"),
    PLUMBER("plumber"),
    ELECTRICIAN("electrician"),
    TILER("tiler"),
    CARPENTER("carpenter"),
    ARTIST("artist");

    private final String name;

    CooperatorCategory(String name) {
        this.name = name;
    }

    public enum ContractorCategory {
        ARTIST {
            @Override
            public CooperatorCategory asCooperatorCategory() {
                return CooperatorCategory.ARTIST;
            }
        },
        CARPENTER {
            @Override
            public CooperatorCategory asCooperatorCategory() {
                return CooperatorCategory.CARPENTER;
            }
        },
        ELECTRICIAN {
            @Override
            public CooperatorCategory asCooperatorCategory() {
                return CooperatorCategory.ELECTRICIAN;
            }
        },
        GENERAL {
            @Override
            public CooperatorCategory asCooperatorCategory() {
                return CooperatorCategory.GENERAL;
            }
        },
        PLUMBER {
            @Override
            public CooperatorCategory asCooperatorCategory() {
                return CooperatorCategory.PLUMBER;
            }
        },
        TILER {
            @Override
            public CooperatorCategory asCooperatorCategory() {
                return CooperatorCategory.TILER;
            }
        };

        /*
        Return CooperatorCategory. No default value.
         */
        public CooperatorCategory asCooperatorCategory() {
            return null;
        }
    }

    public enum SupplierCategory {
        SHOP_WITH_BATHROOM_FITTINGS {
            @Override
            public CooperatorCategory asCooperatorCategory() {
                return CooperatorCategory.SHOP_WITH_BATHROOM_FITTINGS;
            }
        },
        SHOP_WITH_TILES {
            @Override
            public CooperatorCategory asCooperatorCategory() {
                return CooperatorCategory.SHOP_WITH_TILES;
            }
        },
        BATHROOM_CERAMICS_SHOP {
            @Override
            public CooperatorCategory asCooperatorCategory() {
                return CooperatorCategory.BATHROOM_CERAMICS_SHOP;
            }
        },
        PAINT_SHOP {
            @Override
            public CooperatorCategory asCooperatorCategory() {
                return CooperatorCategory.PAINT_SHOP;
            }
        },
        FLOORING_SHOP {
            @Override
            public CooperatorCategory asCooperatorCategory() {
                return CooperatorCategory.FLOORING_SHOP;
            }
        },
        LIGHTING_SHOP {
            @Override
            public CooperatorCategory asCooperatorCategory() {
                return CooperatorCategory.LIGHTING_SHOP;
            }
        };

        /*
        Return CooperatorCategory. No default value.
        */
        public CooperatorCategory asCooperatorCategory() {
            return null;
        }
    }
}
