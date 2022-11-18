package com.tasks.rest;

import com.tasks.business.ProjectService;
import com.tasks.business.TasksService;
import com.tasks.business.entities.Project;
import com.tasks.business.entities.Task;
import com.tasks.business.exceptions.DuplicatedResourceException;
import com.tasks.business.exceptions.InstanceNotFoundException;
import com.tasks.rest.dto.ProjectDto;
import com.tasks.rest.json.ErrorDetailsResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import java.net.URI;
import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api")
@Api(value = "Projects Management", tags = { "Projects Management" })
public class ProjectController {
    
    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private TasksService tasksService;
    
    @ApiOperation(value = "Get all projects")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully retrieved the list of projects", 
                     responseContainer="List", response = Project.class)
    })
    @RequestMapping(value = "/projects", method = RequestMethod.GET)
    public ResponseEntity<Iterable<Project>> doGetProjects() {
        return ResponseEntity.ok( projectService.findAll());
    }
    
    @ApiOperation(value = "Find project by id")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully retrieved the project", response = Project.class),
        @ApiResponse(code = 404, message = "The project does not exist", response = ErrorDetailsResponse.class)
    })
    @RequestMapping(value = "/projects/{id}", method = RequestMethod.GET)
    public ResponseEntity<?> doGetProjectById(@PathVariable("id") Long id) throws InstanceNotFoundException {
        return ResponseEntity.ok(projectService.findById(id));
    }

    @ApiOperation(value = "Create project", authorizations = {@Authorization(value = "Bearer")})
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Successfully created the project"),
        @ApiResponse(code = 409, message = "The task already exists", response = ErrorDetailsResponse.class)
    })
    @RequestMapping(value = "/projects", method = RequestMethod.POST)
    public ResponseEntity<?> doCreateProject(Principal principal, @RequestBody ProjectDto project) 
            throws DuplicatedResourceException, InstanceNotFoundException {
        Project newProject = projectService.create(project.getName(), project.getDescription(),
                principal.getName());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(newProject.getProjectId()).toUri();
        return ResponseEntity.created(location).body(newProject);
    } 
    
    @ApiOperation(value = "Update project by id", authorizations = {@Authorization(value = "Bearer")})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully updated the project", response = Project.class),
        @ApiResponse(code = 404, message = "The project does not exist", response = ErrorDetailsResponse.class),
        @ApiResponse(code = 409, message = "The project already exists", response = ErrorDetailsResponse.class)
    })
    @RequestMapping(value = "/projects/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> doUpdateProject(@PathVariable("id") Long id, @RequestBody ProjectDto project) 
        throws InstanceNotFoundException, DuplicatedResourceException {        
        Project updatedProject = projectService.update(id, project.getName(), project.getDescription());
        return ResponseEntity.ok(updatedProject);
    }

    @ApiOperation(value = "Remove project by id", authorizations = {@Authorization(value = "Bearer")})
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Successfully removed the project"),
        @ApiResponse(code = 404, message = "The task does not exist", response = ErrorDetailsResponse.class)
    })
    @RequestMapping(value = "/projects/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> doRemoveProjectById(@PathVariable("id") Long id) throws InstanceNotFoundException {
        projectService.removeById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    @ApiOperation(value = "Get project tasks by project id")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully retrieved the list of project tasks", 
                responseContainer="List", response = Task.class),
        @ApiResponse(code = 404, message = "The project does not exist", response = ErrorDetailsResponse.class)
    })    
    @RequestMapping(value = "/projects/{id}/tasks", method = RequestMethod.GET)
    public ResponseEntity<?> doGetProjectTasksById(@PathVariable("id") Long id) throws InstanceNotFoundException {
        return ResponseEntity.ok(tasksService.findByProjectId(id));
    }
    
}
