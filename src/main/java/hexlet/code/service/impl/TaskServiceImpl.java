package hexlet.code.service.impl;

import hexlet.code.dto.task.TaskCreateDTO;
import hexlet.code.dto.task.TaskFilterParams;
import hexlet.code.dto.task.TaskUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.service.TaskService;
import hexlet.code.specification.TaskSpecification;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class TaskServiceImpl  implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final  LabelRepository labelRepository;
    private final TaskSpecification taskSpecification;
    public List<Task> getAll() {
        return taskRepository.findAll();
    }

    public Task findById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));
    }

    public Task create(TaskCreateDTO taskCreateDTO) {
        Task task = taskMapper.map(taskCreateDTO);
        return taskRepository.save(task);
    }

    public Task update(TaskUpdateDTO taskUpdateDTO, Long id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        taskMapper.update(taskUpdateDTO, task);

        if (taskUpdateDTO.getLabelsToIds() != null && taskUpdateDTO.getLabelsToIds().isPresent()) {
            Set<Label> labels = new HashSet<>(labelRepository.findAllById(taskUpdateDTO.getLabelsToIds().get()));
            task.setLabels(labels);
        }
        return taskRepository.save(task);
    }

    public void delete(Long id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        task.getLabels().clear();
        taskRepository.delete(task);
    }

    public List<Task> getAllFiltered(TaskFilterParams filterParams) {
        var specific = taskSpecification.build(filterParams);
        return taskRepository.findAll(specific);
    }
}
