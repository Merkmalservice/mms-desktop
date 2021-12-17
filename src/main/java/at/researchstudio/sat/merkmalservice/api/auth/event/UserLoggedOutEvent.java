package at.researchstudio.sat.merkmalservice.api.auth.event;

import java.time.Clock;
import org.springframework.context.ApplicationEvent;

public class UserLoggedOutEvent extends ApplicationEvent {
    public UserLoggedOutEvent(Object source) {
        super(source);
    }

    public UserLoggedOutEvent(Object source, Clock clock) {
        super(source, clock);
    }
}
