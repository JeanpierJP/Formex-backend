package com.superinka.formex.payload.request;

import com.superinka.formex.model.enums.PaymentStatus;
import lombok.Data;

@Data
public class UpdatePaymentStatusRequest {
    private Long studentId;
    private PaymentStatus paymentStatus;
}
