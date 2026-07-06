package com.bob.pay.domain.model.order;

import jakarta.validation.constraints.NotBlank;

public record Address(
        @NotBlank String contactName,
        @NotBlank String phone,
        @NotBlank String region,
        @NotBlank String detail,
        boolean defaultAddress
) {
}
