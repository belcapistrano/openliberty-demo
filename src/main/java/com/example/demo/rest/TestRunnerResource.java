package com.example.demo.rest;

import com.example.demo.service.TestRunnerService;
import com.example.demo.service.TestRunnerService.TestSuiteExecution;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/tests")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TestRunnerResource {

    @Inject
    private TestRunnerService testRunnerService;

    @POST
    @Path("/run")
    public Response runAllTests() {
        try {
            String executionId = testRunnerService.runTests();
            Map<String, String> response = new HashMap<>();
            response.put("executionId", executionId);
            response.put("status", "STARTED");
            response.put("message", "Test execution started");

            return Response.accepted(response).build();
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to start test execution");
            error.put("message", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @POST
    @Path("/run/{testClass}/{testMethod}")
    public Response runSpecificTest(@PathParam("testClass") String testClass,
                                   @PathParam("testMethod") String testMethod) {
        try {
            String executionId = testRunnerService.runSpecificTest(testClass, testMethod);
            Map<String, String> response = new HashMap<>();
            response.put("executionId", executionId);
            response.put("status", "STARTED");
            response.put("message", "Test execution started for " + testClass + "." + testMethod);

            return Response.accepted(response).build();
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to start test execution");
            error.put("message", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @GET
    @Path("/execution/{executionId}")
    public Response getTestExecution(@PathParam("executionId") String executionId) {
        TestSuiteExecution execution = testRunnerService.getTestExecution(executionId);
        if (execution == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Test execution not found");
            error.put("executionId", executionId);
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }

        return Response.ok(execution).build();
    }

    @GET
    @Path("/executions")
    public Response getAllExecutions() {
        List<TestSuiteExecution> executions = testRunnerService.getAllExecutions();
        return Response.ok(executions).build();
    }

    @GET
    @Path("/status/{executionId}")
    public Response getExecutionStatus(@PathParam("executionId") String executionId) {
        TestSuiteExecution execution = testRunnerService.getTestExecution(executionId);
        if (execution == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Test execution not found");
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }

        Map<String, Object> status = new HashMap<>();
        status.put("executionId", executionId);
        status.put("status", execution.getStatus());
        status.put("startTime", execution.getStartTime());
        status.put("endTime", execution.getEndTime());

        if (execution.getResults() != null) {
            long passed = execution.getResults().stream()
                .filter(r -> "PASSED".equals(r.getStatus()))
                .count();
            long failed = execution.getResults().stream()
                .filter(r -> "FAILED".equals(r.getStatus()))
                .count();
            long skipped = execution.getResults().stream()
                .filter(r -> "SKIPPED".equals(r.getStatus()))
                .count();

            status.put("totalTests", execution.getResults().size());
            status.put("passed", passed);
            status.put("failed", failed);
            status.put("skipped", skipped);
        }

        return Response.ok(status).build();
    }

    @GET
    @Path("/available")
    public Response getAvailableTests() {
        Map<String, Object> availableTests = new HashMap<>();

        // List of available test classes and methods
        Map<String, String[]> tests = new HashMap<>();
        tests.put("UserServiceTest", new String[]{
            "testGetAllUsers", "testCreateUser", "testGetUserById",
            "testUpdateUser", "testDeleteUser", "testFindByUsername"
        });
        tests.put("UserResourceIT", new String[]{
            "testHealthEndpoint", "testGetAllUsers", "testCreateUser",
            "testGetUserById", "testDeleteUser", "testSearchByUsername",
            "testSearchByUsernameNotFound"
        });

        availableTests.put("testClasses", tests);
        availableTests.put("totalClasses", tests.size());

        int totalMethods = tests.values().stream()
            .mapToInt(methods -> methods.length)
            .sum();
        availableTests.put("totalMethods", totalMethods);

        return Response.ok(availableTests).build();
    }
}