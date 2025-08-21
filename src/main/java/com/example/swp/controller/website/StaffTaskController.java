package com.example.swp.controller.website;

import com.example.swp.entity.Staff;
import com.example.swp.entity.Task;
import com.example.swp.enums.TaskStatus;
import com.example.swp.security.MyUserDetail;
import com.example.swp.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/staff")
public class StaffTaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping("/tasks")
    public String showMyTasks(@RequestParam(defaultValue = "0") int page, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof MyUserDetail userDetail) {
            Staff staff = (Staff) userDetail.getUser();
            List<Task> allTasks = taskService.getTasksByStaff(staff);

            int pageSize = 5;
            int totalTasks = allTasks.size();
            int totalPages = totalTasks > 0 ? (int) Math.ceil((double) totalTasks / pageSize) : 0;

            List<Task> tasks;
            if (totalTasks > 0) {
                int startIndex = page * pageSize;
                int endIndex = Math.min(startIndex + pageSize, totalTasks);
                tasks = allTasks.subList(startIndex, endIndex);
            } else {
                tasks = allTasks;
            }

            model.addAttribute("tasks", tasks);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("totalTasks", totalTasks);
        }

        model.addAttribute("taskStatuses", TaskStatus.values());
        return "staff-tasks";
    }

    @PostMapping("/tasks/update-status")
    public String updateMyTaskStatus(@RequestParam("taskId") int taskId,
                                   @RequestParam("status") TaskStatus status) {
        taskService.updateTaskStatus(taskId, status);
        return "redirect:/staff/tasks";
    }

    @GetMapping("/tasks/view/{id}")
    public String viewTask(@PathVariable int id, Model model) {
        Optional<Task> taskOpt = taskService.getTaskById(id);
        if (taskOpt.isPresent()) {
            model.addAttribute("task", taskOpt.get());
            return "staff-task-detail";
        }
        return "redirect:/staff/tasks";
    }


}
