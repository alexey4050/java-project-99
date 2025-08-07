package hexlet.code.service;

import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Task;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    public List<Task> getAll() {
        return taskRepository.findAll();
    }

    public Task findById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));
    }

    public Task create(TaskCreateDTO taskCreateDTO) {
        var task = taskMapper.map(taskCreateDTO);

        var status = taskStatusRepository.findBySlug(taskCreateDTO.getStatus())
                .orElseThrow(() -> new ResourceNotFoundException("Status not found"));
        task.setTaskStatus(status);

        if (taskCreateDTO.getAssigneeId() != null) {
            var assignee = userRepository.findById(taskCreateDTO.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));
            task.setAssignee(assignee);
        }

        return taskRepository.save(task);
    }

    public Task update(TaskUpdateDTO taskUpdateDTO, Long id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));
        taskMapper.update(taskUpdateDTO, task);

        if (taskUpdateDTO.getStatus() != null && taskUpdateDTO.getStatus().isPresent()) {
            var status = taskStatusRepository.findBySlug(taskUpdateDTO.getStatus().get())
                    .orElseThrow(() -> new ResourceNotFoundException("Status not found"));
            task.setTaskStatus(status);
        }

        if (taskUpdateDTO.getAssigneeId() != null) {
            if (taskUpdateDTO.getAssigneeId().isPresent()) {
                var assignee = userRepository.findById(taskUpdateDTO.getAssigneeId().get())
                        .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));
                task.setAssignee(assignee);
            } else {
                task.setAssignee(null);
            }
        }
        return taskRepository.save(task);
    }

    public void delete(Long id) {
        taskRepository.deleteById(id);
    }
}
