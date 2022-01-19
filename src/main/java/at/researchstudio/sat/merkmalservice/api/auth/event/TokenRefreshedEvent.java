package at.researchstudio.sat.merkmalservice.api.auth.event;

import java.time.Clock;
import org.springframework.context.ApplicationEvent;

public class TokenRefreshedEvent extends ApplicationEvent {
    public TokenRefreshedEvent(Object source) {
        super(source);
    }

    public TokenRefreshedEvent(Object source, Clock clock) {
        super(source, clock);
    }
}
