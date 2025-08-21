package com.example.swp.repository;

import com.example.swp.entity.Manager;
import com.example.swp.entity.Staff;
import com.example.swp.entity.Task;
import com.example.swp.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {
    List<Task> findByAssignedStaffContaining(Staff staff);
    List<Task> findByAssignedManager(Manager manager);
    List<Task> findByStatus(TaskStatus status);
    List<Task> findByAssignedStaffContainingAndStatus(Staff staff, TaskStatus status);
    List<Task> findByAssignedManagerOrderByIdAsc(Manager manager);
    List<Task> findByAssignedStaffContainingOrderByDueDateAsc(Staff staff);
    List<Task> findByAssignedStaffContainingOrderByIdAsc(Staff staff);
}
