package at.researchstudio.sat.merkmalservice.api.auth.event;

import java.time.Clock;
import org.springframework.context.ApplicationEvent;

public class UserLoggedInEvent extends ApplicationEvent {
    public UserLoggedInEvent(Object source) {
        super(source);
    }

    public UserLoggedInEvent(Object source, Clock clock) {
        super(source, clock);
    }
}
