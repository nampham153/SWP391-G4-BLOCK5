package com.example.swp.service.impl;

import com.example.swp.entity.Manager;
import com.example.swp.entity.Staff;
import com.example.swp.entity.Task;
import com.example.swp.enums.TaskStatus;
import com.example.swp.repository.StaffRepository;
import com.example.swp.repository.TaskRepository;
import com.example.swp.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Override
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @Override
    public List<Task> getTasksByManager(Manager manager) {
        return taskRepository.findByAssignedManagerOrderByIdAsc(manager);
    }

    @Override
    public List<Task> getTasksByStaff(Staff staff) {
        return taskRepository.findByAssignedStaffContainingOrderByIdAsc(staff);
    }

    @Override
    public List<Task> getTasksByStatus(TaskStatus status) {
        return taskRepository.findByStatus(status);
    }

    @Override
    public Optional<Task> getTaskById(int id) {
        return taskRepository.findById(id);
    }

    @Override
    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    @Override
    public Task updateTask(Task task) {
        return taskRepository.save(task);
    }

    @Override
    public void deleteTask(int id) {
        taskRepository.deleteById(id);
    }

    @Override
    public Task assignTaskToStaff(int taskId, List<Integer> staffIds) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);

        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            List<Staff> staffList = new ArrayList<>();

            for (Integer staffId : staffIds) {
                Optional<Staff> staffOpt = staffRepository.findById(staffId);
                if (staffOpt.isPresent()) {
                    staffList.add(staffOpt.get());
                }
            }

            task.setAssignedStaff(staffList);
            return taskRepository.save(task);
        }
        return null;
    }

    @Override
    public Task updateTaskStatus(int taskId, TaskStatus status) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            task.setStatus(status);
            return taskRepository.save(task);
        }
        return null;
    }
}
