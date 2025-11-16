package s.l.f.t.events.dto;

import java.time.Instant;

public record PaymentResponse(
        Integer payment_id,
        Integer user_id,
        Float amount,
        String status,
        Instant timestamp,
        String method_type
) {
}
