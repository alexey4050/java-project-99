package hexlet.code.util;

import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import org.instancio.Instancio;
import org.instancio.Model;
import org.instancio.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.UUID;

@Component
public class ModelGenerator {

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Model<User> getUserModel() {
        return Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .ignore(Select.field(User::getCreatedAt))
                .ignore(Select.field(User::getUpdatedAt))
                .supply(Select.field(User::getFirstName), () -> "FirstName-" + UUID.randomUUID())
                .supply(Select.field(User::getLastName), () -> "LastName-" + UUID.randomUUID())
                .supply(Select.field(User::getEmail), () -> "user-" + UUID.randomUUID() + "@example.com")
                .supply(Select.field(User::getPassword), () -> passwordEncoder.encode("password"))
                .toModel();
    }

    public Model<TaskStatus> getTaskStatusModel() {
        return Instancio.of(TaskStatus.class)
                .ignore(Select.field(TaskStatus::getId))
                .ignore(Select.field(TaskStatus::getCreatedAt))
                .supply(Select.field(TaskStatus::getName), () -> "Status-" + UUID.randomUUID())
                .supply(Select.field(TaskStatus::getSlug), () -> "status-" + "status-" + UUID.randomUUID())
                .toModel();
    }

    public Model<Label> getLabelModel() {
        return Instancio.of(Label.class)
                .ignore(Select.field(Label::getId))
                .ignore(Select.field(Label::getCreatedAt))
                .ignore(Select.field(Label::getTasks))
                .supply(Select.field(Label::getName), () -> "Label-" + UUID.randomUUID())
                .toModel();
    }

    public Model<Task> getTaskModel() {
        return Instancio.of(Task.class)
                .ignore(Select.field(Task::getId))
                .ignore(Select.field(Task::getCreatedAt))
                .supply(Select.field(Task::getName), () -> "Task-" + UUID.randomUUID())
                .supply(Select.field(Task::getDescription), () -> "Description-" + UUID.randomUUID())
                .supply(Select.field(Task::getIndex), () -> 1000)
                .supply(Select.field(Task::getLabels), () -> new HashSet<>())
                .toModel();
    }
}
