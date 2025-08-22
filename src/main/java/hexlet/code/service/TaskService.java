package hexlet.code.service;

import hexlet.code.dto.task.TaskCreateDTO;
import hexlet.code.dto.task.TaskFilterParams;
import hexlet.code.dto.task.TaskUpdateDTO;
import hexlet.code.model.Task;

import java.util.List;

public interface TaskService  {
    List<Task> getAll();
    Task findById(Long id);
    Task create(TaskCreateDTO taskCreateDTO);
    Task update(TaskUpdateDTO taskUpdateDTO, Long id);
    void delete(Long id);
    List<Task> getAllFiltered(TaskFilterParams filterParams);
}
