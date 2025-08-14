package hexlet.code.controller.api;

import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.TaskStatusDTO;
import hexlet.code.dto.TaskStatusUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.repository.TaskStatusRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TaskStatusController {
    @Autowired
    private TaskStatusRepository statusRepository;

    @Autowired
    private TaskStatusMapper statusMapper;

    @GetMapping("/task_statuses")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<TaskStatusDTO>> index() {
        var statuses = statusRepository.findAll();
        var result = statuses.stream()
                .map(statusMapper::map)
                .toList();
        var headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(statuses.size()));
        return new ResponseEntity<>(result, headers, HttpStatus.OK);
    }

    @GetMapping("/task_statuses/{id}")
    public TaskStatusDTO show(@PathVariable Long id) {
        var status = statusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Status not found"));
        return statusMapper.map(status);
    }

    @PostMapping("/task_statuses")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    public TaskStatusDTO create(@Valid @RequestBody TaskStatusCreateDTO statusData) {
        var status = statusMapper.map(statusData);
        statusRepository.save(status);
        return statusMapper.map(status);
    }

    @PutMapping("/task_statuses/{id}")
    @PreAuthorize("isAuthenticated()")
    public TaskStatusDTO update(@Valid @RequestBody TaskStatusUpdateDTO statusData,
                                @PathVariable Long id) {
        var status = statusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Status not found"));
        statusMapper.update(statusData, status);
        statusRepository.save(status);
        return statusMapper.map(status);
    }

    @DeleteMapping("/task_statuses/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    public void delete(@PathVariable Long id) {
        statusRepository.deleteById(id);
    }
}
