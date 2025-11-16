package s.l.f.t.events.dto;

import java.time.Instant;

public record UserResponse(Integer user_id, String username, String action, Instant timestamp, String status) {
}
