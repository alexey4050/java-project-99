package hexlet.code.component;

import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

@Component
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusRepository statusRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        var adminEmail = "hexlet@example.com";
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("qwerty"));
            userRepository.save(admin);
            initDefaultStatuses();
        }
    }

    private void initDefaultStatuses() {
        var statuses = Map.of(
                "draft", "Draft",
                "to_review", "ToReview",
                "to_be_fixed", "ToBeFixed",
                "to_publish", "ToPublish",
                "published", "Published"
        );

        statuses.forEach((slug, name) -> {
            if (statusRepository.findBySlug(slug).isEmpty()) {
                System.out.println("Creating status: " + name + " (" + slug + ")");
                var status = new TaskStatus();
                status.setName(name);
                status.setSlug(slug);
                status.setCreatedAt(LocalDate.now());
                statusRepository.save(status);
            }
        });
        System.out.println("Total statuses: " + statusRepository.count());
    }
}
