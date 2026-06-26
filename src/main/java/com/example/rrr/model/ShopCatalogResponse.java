package com.example.rrr.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ShopCatalogResponse {

    private int coins;
    private List<ShopListing> listings;
}
