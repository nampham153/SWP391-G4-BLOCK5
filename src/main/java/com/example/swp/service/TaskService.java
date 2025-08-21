package com.example.swp.service;

import com.example.swp.entity.Manager;
import com.example.swp.entity.Staff;
import com.example.swp.entity.Task;
import com.example.swp.enums.TaskStatus;

import java.util.List;
import java.util.Optional;

public interface TaskService {
    List<Task> getAllTasks();
    List<Task> getTasksByManager(Manager manager);
    List<Task> getTasksByStaff(Staff staff);
    List<Task> getTasksByStatus(TaskStatus status);
    Optional<Task> getTaskById(int id);
    Task createTask(Task task);
    Task updateTask(Task task);
    void deleteTask(int id);
    Task assignTaskToStaff(int taskId, List<Integer> staffIds);
    Task updateTaskStatus(int taskId, TaskStatus status);
}
