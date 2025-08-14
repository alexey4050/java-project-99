package hexlet.code.specification;

import hexlet.code.dto.TaskFilterParams;
import hexlet.code.model.Task;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class TaskSpecification {
    public Specification<Task> build(TaskFilterParams filterParams) {
        return withTitleCont(filterParams.getTitleCont())
                .and(withAssigneeId(filterParams.getAssigneeId()))
                .and(withStatusSlug(filterParams.getStatus()))
                .and(withLabelId(filterParams.getLabelId()));
    }

    private Specification<Task> withTitleCont(String titleCont) {
        return ((root, query, criteriaBuilder) ->
                titleCont == null ? null : criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),
                        "%" + titleCont.toLowerCase() + "%"));
    }

    private Specification<Task> withAssigneeId(Long assigneeId) {
        return ((root, query, criteriaBuilder) ->
                assigneeId == null ? null : criteriaBuilder.equal(root.get("assignee")
                        .get("id"), assigneeId));
    }

    private Specification<Task> withStatusSlug(String status) {
        return ((root, query, criteriaBuilder) ->
                status == null ? null : criteriaBuilder.equal(root.get("taskStatus")
                        .get("slug"), status));
    }

    private Specification<Task> withLabelId(Long labelId) {
        return ((root, query, criteriaBuilder) -> {
            if (labelId == null) {
                return null;
            }
            var taskLabel = root.join("label");
            return criteriaBuilder.equal(taskLabel.get("id"), labelId);
        });
    }

}
