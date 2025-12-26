package ru.yandex.practicum.commerce.interactionapi.dto.payment;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDto {

    @NonNull
    private UUID paymentId;

    private BigDecimal totalPayment;

    private BigDecimal deliveryTotal;

    private BigDecimal feeTotal;

}
