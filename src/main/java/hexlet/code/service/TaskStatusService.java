package hexlet.code.service;

import hexlet.code.dto.task.TaskStatusCreateDTO;
import hexlet.code.dto.task.TaskStatusDTO;
import hexlet.code.dto.task.TaskStatusUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.repository.TaskStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskStatusService {

    @Autowired
    private TaskStatusRepository statusRepository;

    @Autowired
    private TaskStatusMapper taskStatusMapper;

    public List<TaskStatusDTO> getAll() {
        var statusTasks = statusRepository.findAll();
        return statusTasks.stream()
                .map(taskStatusMapper::map)
                .toList();
    }

    public TaskStatusDTO findById(Long id) {
        var statusTask = statusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task status not found"));
        return taskStatusMapper.map(statusTask);
    }

    public TaskStatusDTO create(TaskStatusCreateDTO statusData) {
        var statusTask = taskStatusMapper.map(statusData);
        statusRepository.save(statusTask);
        return taskStatusMapper.map(statusTask);
    }

    public TaskStatusDTO update(TaskStatusUpdateDTO statusData, Long id) {
        var statusTask = statusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task status noy found"));
        taskStatusMapper.update(statusData, statusTask);
        statusRepository.save(statusTask);
        return taskStatusMapper.map(statusTask);
    }

    public void delete(Long id) {
        statusRepository.deleteById(id);
    }
}
