package com.ecommerce.aplication.records.PaymentRecords;

import jakarta.validation.constraints.NotNull;

public record DataPaymentsRequest(@NotNull Boolean forceFail) {
}
