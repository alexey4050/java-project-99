package hexlet.code.component;

import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import hexlet.code.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomUserDetailsService userServer;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        var adminEmail = "hexlet@example.com";
        var admin = new User();
        admin.setEmail(adminEmail);
        admin.setPassword("qwerty");
        userServer.createUser(admin);
    }
}
