package com.tasks.rest;

import com.fasterxml.jackson.databind.node.TextNode;
import com.tasks.business.ProjectService;
import com.tasks.business.TasksService;
import com.tasks.business.entities.Comment;
import com.tasks.business.entities.Task;
import com.tasks.business.entities.TaskResolution;
import com.tasks.business.entities.TaskState;
import com.tasks.business.exceptions.DuplicatedResourceException;
import com.tasks.business.exceptions.InalidStateException;
import com.tasks.business.exceptions.InstanceNotFoundException;
import com.tasks.config.JwtTokenProvider;
import com.tasks.rest.dto.CommentDto;
import com.tasks.rest.dto.TaskDto;
import com.tasks.rest.json.ErrorDetailsResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import java.net.URI;
import java.security.Principal;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api")
@Api(value = "Tasks Management", tags = { "Tasks Management" })
public class TaskController {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TasksService tasksService;    
    
    @ApiOperation(value = "Get all tasks")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully retrieved the list of tasks", 
                     responseContainer="List", response = Task.class)
    })
    @RequestMapping(value = "/tasks", method = RequestMethod.GET)
    public ResponseEntity<Iterable<Task>> doGetTasks(@RequestParam(value = "owner", required = false) String owner) {
        return ResponseEntity.ok(StringUtils.isNotBlank(owner) ? 
            tasksService.findByOwner(owner) : tasksService.findAll());
    }
    
    @ApiOperation(value = "Find task by id")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully retrieved the task", response = Task.class),
        @ApiResponse(code = 404, message = "The task does not exist", response = ErrorDetailsResponse.class)
    })
    @RequestMapping(value = "/tasks/{id}", method = RequestMethod.GET)
    public ResponseEntity<?> doGetTaskById(@PathVariable("id") Long id) throws InstanceNotFoundException {
        return ResponseEntity.ok(tasksService.findById(id));
    }

    @ApiOperation(value = "Create task", authorizations = {@Authorization(value = "Bearer")})
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Successfully created the task"),
        @ApiResponse(code = 409, message = "The task already exists", response = ErrorDetailsResponse.class)
    })
    @RequestMapping(value = "/tasks", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> doCreateTask(@RequestBody TaskDto task, @RequestHeader (name="Authorization") String token)
            throws DuplicatedResourceException, InstanceNotFoundException {
        boolean isOwner = projectService.isOwner(task.getProject(), jwtTokenProvider.getUser(token).getName());
        if (!isOwner) {
            throw new DuplicatedResourceException("Task", "owner", "You are not authorized to create tasks in this project");
        }

        Task newTask = tasksService.create(task.getName(), task.getDescription(),
                task.getType(), task.getOwner(), task.getProject());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(newTask.getTaskId()).toUri();
        return ResponseEntity.created(location).body(newTask);
    }
    
    @ApiOperation(value = "Update task by id", authorizations = {@Authorization(value = "Bearer")})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully updated the task", response = TextNode.class),
        @ApiResponse(code = 404, message = "The task does not exist or is closed", response = ErrorDetailsResponse.class),
        @ApiResponse(code = 409, message = "The task already exists", response = ErrorDetailsResponse.class)
    })
    @RequestMapping(value = "/tasks/{id}", method = RequestMethod.PUT)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> doUpdateTask(@PathVariable("id") Long id, @RequestBody TaskDto task, @RequestHeader (name="Authorization") String token)
        throws InstanceNotFoundException, InalidStateException, DuplicatedResourceException {      
        boolean isOwner = projectService.isOwner(task.getProject(), jwtTokenProvider.getUser(token).getName());
        if (!isOwner) {
            throw new DuplicatedResourceException("Task", "owner", "You are not authorized to update tasks in this project");
        }

        Task updatedTask = tasksService.update(id, task.getName(), task.getDescription(),
            task.getType(), task.getOwner(), task.getProject());
        return ResponseEntity.ok(updatedTask);
    }
    
    @ApiOperation(value = "Remove task by id", authorizations = {@Authorization(value = "Bearer")})
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Successfully removed the task"),
        @ApiResponse(code = 404, message = "The task does not exist", response = ErrorDetailsResponse.class)
    })
    @RequestMapping(value = "/tasks/{id}", method = RequestMethod.DELETE)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> doRemoveTaskById(@PathVariable("id") Long id, @RequestHeader (name="Authorization") String token) throws InstanceNotFoundException, DuplicatedResourceException {
        Task task = tasksService.findById(id);
        boolean isOwner = projectService.isOwner(task.getProject().getProjectId(), jwtTokenProvider.getUser(token).getName());
        if (!isOwner) {
            throw new DuplicatedResourceException("Task", "owner", "You are not authorized to remove tasks in this project");
        }

        tasksService.removeById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @ApiOperation(value = "Change task state by id", authorizations = {@Authorization(value = "Bearer")})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully changed the task state", response = TextNode.class),
        @ApiResponse(code = 404, message = "The task does not exist", response = ErrorDetailsResponse.class)
    })
    @RequestMapping(value = "/tasks/{id}/changeState", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> doChangeTaskState(@PathVariable("id") Long id,
                                         @RequestBody(required = true) TextNode state, @RequestHeader (name="Authorization") String token)
        throws InstanceNotFoundException, DuplicatedResourceException {   
        Task task = tasksService.findById(id);     
        boolean isOwner = projectService.isOwner(task.getProject().getProjectId(), jwtTokenProvider.getUser(token).getName());
        if (!isOwner) {
            throw new DuplicatedResourceException("Task", "owner", "You are not authorized to change tasks state in this project");
        }

        task = tasksService.changeState(id, TaskState.valueOf(state.asText()));
        return ResponseEntity.ok(task);
    }

    @ApiOperation(value = "Change task resolution by id", authorizations = {@Authorization(value = "Bearer")})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully changed the task state", response = TextNode.class),
        @ApiResponse(code = 404, message = "The task does not exist", response = ErrorDetailsResponse.class)
    })
    @RequestMapping(value = "/tasks/{id}/changeResolution", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> doChangeTaskResolution(@PathVariable("id") Long id,
                                                    @RequestBody(required = true) TextNode resolution, @RequestHeader (name="Authorization") String token)
        throws InstanceNotFoundException, InalidStateException, DuplicatedResourceException {        
        boolean isOwner = tasksService.isOwner(id, jwtTokenProvider.getUser(token).getName());
        Task task = tasksService.findById(id);

        if (!isOwner) {
            throw new DuplicatedResourceException("Task", "owner", "You are not authorized to change this task resolution");
        }

        if (task.getState() == TaskState.CLOSED) {
            throw new InalidStateException("The task is already closed");
        }

        task = tasksService.changeResolution(id, TaskResolution.valueOf(resolution.asText()));
        return ResponseEntity.ok(task);
    }
    @ApiOperation(value = "Change task progess by id", authorizations = {@Authorization(value = "Bearer")})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully changed the task progress", response = TextNode.class),
        @ApiResponse(code = 404, message = "The task does not exist", response = ErrorDetailsResponse.class)
    })
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/tasks/{id}/changeProgress", method = RequestMethod.POST)
    public ResponseEntity<?> doChangeTaskProgress(@PathVariable("id") Long id,
                                                  @RequestBody(required = true) TextNode progress, @RequestHeader (name="Authorization") String token)
        throws InstanceNotFoundException, InalidStateException, DuplicatedResourceException {        
        boolean isOwner = tasksService.isOwner(id, jwtTokenProvider.getUser(token).getName());
        Task task = tasksService.findById(id);
        isOwner = isOwner || projectService.isOwner(task.getProject().getProjectId(), jwtTokenProvider.getUser(token).getName());

        if (!isOwner) {
            throw new DuplicatedResourceException("Task", "owner", "You are not authorized to change this task resolution");    
        }

        if (task.getState() == TaskState.CLOSED) {
            throw new InalidStateException("The task is already closed");
        }

        task = tasksService.changeProgress(id, (byte) progress.asInt());
        return ResponseEntity.ok(task);
    }

    @ApiOperation(value = "Create a comment", authorizations = {@Authorization(value = "Bearer")})
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Successfully created the comment"),
        @ApiResponse(code = 404, message = "The task does not exist or is closed", response = ErrorDetailsResponse.class)
    })
    @RequestMapping(value = "/comments", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> doCreateTaskComment(Principal principal, @RequestBody CommentDto comment) 
        throws InstanceNotFoundException, InalidStateException {
        Comment newComment = tasksService
                .addComment(comment.getTaskId(), principal.getName(), comment.getText());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}").buildAndExpand(newComment.getCommentId()).toUri();
        return ResponseEntity.created(location).build();
    }
    
    @ApiOperation(value = "Find comment by id")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully retrieved the comment", response = Comment.class),
        @ApiResponse(code = 404, message = "The comment does not exist", response = ErrorDetailsResponse.class)
    })
    @RequestMapping(value = "/comments/{id}", method = RequestMethod.GET)
    public ResponseEntity<?> doGetCommentById(@PathVariable("id") Long id) throws InstanceNotFoundException {
        return ResponseEntity.ok(tasksService.findCommentById(id));
    }    
}
