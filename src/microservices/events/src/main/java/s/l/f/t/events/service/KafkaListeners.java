package s.l.f.t.events.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaListeners {
    @KafkaListener(
            topics = "user-events", groupId = "u1"
    )
    void listenUser(String data) {
        System.out.printf("listenUser. %s", data);
    }

    @KafkaListener(
            topics = "movie-events", groupId = "m1"
    )
    void listenMovie(String data) {
        System.out.printf("listenMovie. %s", data);
    }

    @KafkaListener(
            topics = "payment-events", groupId = "p1"
    )
    void listenPayment(String data) {
        System.out.printf("listenPayment. %s", data);
    }
}
