package com.bob.pay.domain.model.service;

import java.math.BigDecimal;
import java.util.List;

public record ServiceItem(
        String id,
        String name,
        String slogan,
        int durationMinutes,
        BigDecimal price,
        BigDecimal originalPrice,
        int soldCount,
        String imageUrl,
        ServiceDetail detail
) {
    public record ServiceDetail(
            List<String> highlights,
            List<String> process,
            List<String> materials,
            List<String> notices,
            List<String> contraindications,
            List<String> reasons
    ) {
    }
}
