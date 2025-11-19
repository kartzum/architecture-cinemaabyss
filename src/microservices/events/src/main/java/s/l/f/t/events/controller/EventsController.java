package s.l.f.t.events.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import s.l.f.t.events.dto.HealthCheck;
import s.l.f.t.events.dto.MovieResponse;
import s.l.f.t.events.dto.PaymentResponse;
import s.l.f.t.events.dto.UserResponse;
import s.l.f.t.events.service.KafkaSender;

import java.time.Instant;

@RestController
@RequestMapping("api/events/")
public class EventsController {
    @Autowired
    private KafkaSender kafkaSender;

    @GetMapping("health")
    public ResponseEntity<HealthCheck> health() {
        return new ResponseEntity<>(new HealthCheck(true), HttpStatus.OK);
    }

    @PostMapping("movie")
    public ResponseEntity<MovieResponse> processMovie() {
        kafkaSender.sendMessage("movie-events", "movie");
        return new ResponseEntity<>(
                new MovieResponse(10, "10", "10", 20, "success"),
                HttpStatus.CREATED
        );
    }

    @PostMapping("user")
    public ResponseEntity<UserResponse> processUser() {
        kafkaSender.sendMessage("user-events", "user");
        return new ResponseEntity<>(
                new UserResponse(10, "a", "b", Instant.now(), "success"),
                HttpStatus.CREATED
        );
    }

    @PostMapping("payment")
    public ResponseEntity<PaymentResponse> processPayment() {
        kafkaSender.sendMessage("payment-events", "payment");
        return new ResponseEntity<>(
                new PaymentResponse(30, 10, 42.0F, "success", Instant.now(), ""),
                HttpStatus.CREATED
        );
    }
}
