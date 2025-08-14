package hexlet.code.controller.api;

import io.sentry.Sentry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SentryTestController {
    @GetMapping("/sentry-test")
    public String triggerException() {
        try {
            throw new RuntimeException("Test Sentry exception");
        } catch (Exception e) {
            Sentry.captureException(e);
            return "Error captured and sent to Sentry!";
        }
    }
}
