package com.bob.pay.domain.model.order;

public record GeoPoint(
        double latitude,
        double longitude,
        String label
) {
}
