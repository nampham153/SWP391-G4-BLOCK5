package com.example.swp.controller.website;

import com.example.swp.annotation.LogActivity;
import com.example.swp.entity.Manager;
import com.example.swp.entity.Staff;
import com.example.swp.entity.Storage;
import com.example.swp.entity.Task;
import com.example.swp.enums.TaskStatus;
import com.example.swp.security.MyUserDetail;
import com.example.swp.service.StaffService;
import com.example.swp.service.StorageService;
import com.example.swp.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private StaffService staffService;

    @Autowired
    private StorageService storageService;

    @GetMapping("/task-list")
    public String showTaskList(@RequestParam(defaultValue = "0") int page, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof MyUserDetail userDetail) {
            Manager manager = (Manager) userDetail.getUser();
            List<Task> allTasks = taskService.getTasksByManager(manager);

            int pageSize = 5;
            int totalTasks = allTasks.size();
            int totalPages = (int) Math.ceil((double) totalTasks / pageSize);

            int startIndex = page * pageSize;
            int endIndex = Math.min(startIndex + pageSize, totalTasks);

            List<Task> tasks = allTasks.subList(startIndex, endIndex);

            model.addAttribute("tasks", tasks);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("totalTasks", totalTasks);
        }

        List<Staff> staffList = staffService.getAllStaff();
        List<Storage> storageList = storageService.getAll();
        model.addAttribute("staffList", staffList);
        model.addAttribute("storageList", storageList);
        model.addAttribute("taskStatuses", TaskStatus.values());

        return "task-list";
    }

    @LogActivity(action = "Tạo mới công việc")
    @PostMapping("/task-list/create")
    public String createTask(@RequestParam("title") String title,
                           @RequestParam("description") String description,
                           @RequestParam("dueDate") String dueDateStr,
                           @RequestParam("staffIds") List<Integer> staffIds) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof MyUserDetail userDetail) {
            Manager manager = (Manager) userDetail.getUser();

            Task task = new Task();
            task.setTitle(title);
            task.setDescription(description);
            task.setAssignedManager(manager);
            task.setStatus(TaskStatus.PENDING);

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date dueDate = sdf.parse(dueDateStr);
                task.setDueDate(dueDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Task savedTask = taskService.createTask(task);
            if (staffIds != null && !staffIds.isEmpty()) {
                taskService.assignTaskToStaff(savedTask.getId(), staffIds);
            }
        }

        return "redirect:/admin/task-list";
    }



    @GetMapping("/task-list/view/{id}")
    public String viewTask(@PathVariable int id, Model model) {
        Optional<Task> taskOpt = taskService.getTaskById(id);
        if (taskOpt.isPresent()) {
            model.addAttribute("task", taskOpt.get());
            return "task-detail";
        }
        return "redirect:/admin/task-list";
    }

    @LogActivity(action = "Chỉnh sửa công việc")
    @GetMapping("/task-list/edit/{id}")
    public String editTaskForm(@PathVariable int id, Model model) {
        Optional<Task> taskOpt = taskService.getTaskById(id);
        if (taskOpt.isPresent()) {
            model.addAttribute("task", taskOpt.get());
            List<Staff> staffList = staffService.getAllStaff();
            model.addAttribute("staffList", staffList);
            model.addAttribute("taskStatuses", TaskStatus.values());
            return "task-edit";
        }
        return "redirect:/admin/task-list";
    }

    @PostMapping("/task-list/edit/{id}")
    public String updateTask(@PathVariable int id,
                           @RequestParam("title") String title,
                           @RequestParam("description") String description,
                           @RequestParam("dueDate") String dueDateStr,
                           @RequestParam(value = "staffIds", required = false) List<Integer> staffIds,
                           @RequestParam("status") TaskStatus status) {

        Optional<Task> taskOpt = taskService.getTaskById(id);
        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            task.setTitle(title);
            task.setDescription(description);
            task.setStatus(status);

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date dueDate = sdf.parse(dueDateStr);
                task.setDueDate(dueDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            taskService.updateTask(task);

            if (staffIds != null && !staffIds.isEmpty()) {
                taskService.assignTaskToStaff(id, staffIds);
            }
        }

        return "redirect:/admin/task-list";
    }



    @PostMapping("/task-list/update-status/{id}")
    public String updateTaskStatus(@PathVariable int id, @RequestParam("status") TaskStatus status) {
        taskService.updateTaskStatus(id, status);
        return "redirect:/admin/task-list";
    }
}
