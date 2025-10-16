package com.example.demo.service;

import com.example.demo.model.TestResult;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.time.LocalDateTime;
import java.util.UUID;

@ApplicationScoped
public class TestRunnerService {

    private final ConcurrentMap<String, TestSuiteExecution> testExecutions = new ConcurrentHashMap<>();

    public static class TestSuiteExecution {
        private String id;
        private String status; // RUNNING, COMPLETED, FAILED
        private List<TestResult> results;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String output;

        public TestSuiteExecution(String id) {
            this.id = id;
            this.status = "RUNNING";
            this.results = new ArrayList<>();
            this.startTime = LocalDateTime.now();
            this.output = "";
        }

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public List<TestResult> getResults() { return results; }
        public void setResults(List<TestResult> results) { this.results = results; }

        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

        public String getOutput() { return output; }
        public void setOutput(String output) { this.output = output; }

        public void addOutput(String line) {
            this.output += line + "\n";
        }
    }

    public String runTests() {
        String executionId = UUID.randomUUID().toString();
        TestSuiteExecution execution = new TestSuiteExecution(executionId);
        testExecutions.put(executionId, execution);

        // Run tests asynchronously
        CompletableFuture.runAsync(() -> executeTests(execution));

        return executionId;
    }

    public TestSuiteExecution getTestExecution(String executionId) {
        return testExecutions.get(executionId);
    }

    public List<TestSuiteExecution> getAllExecutions() {
        return new ArrayList<>(testExecutions.values());
    }

    private void executeTests(TestSuiteExecution execution) {
        try {
            execution.addOutput("Starting test execution...");
            execution.addOutput("Running UserService tests...");

            // Simulate running UserService tests
            runUserServiceTests(execution);

            execution.addOutput("Running API integration tests...");

            // Simulate running integration tests
            runIntegrationTests(execution);

            execution.setStatus("COMPLETED");
            execution.setEndTime(LocalDateTime.now());
            execution.addOutput("All tests completed successfully!");

        } catch (Exception e) {
            execution.setStatus("FAILED");
            execution.setEndTime(LocalDateTime.now());
            execution.addOutput("Test execution failed: " + e.getMessage());
        }
    }

    private void runUserServiceTests(TestSuiteExecution execution) {
        try {
            Thread.sleep(1000); // Simulate test execution time

            // Simulate test results
            execution.getResults().add(new TestResult(
                "UserServiceTest", "testGetAllUsers", "PASSED",
                "Successfully retrieved all users", 45));

            execution.getResults().add(new TestResult(
                "UserServiceTest", "testCreateUser", "PASSED",
                "Successfully created new user", 32));

            execution.getResults().add(new TestResult(
                "UserServiceTest", "testGetUserById", "PASSED",
                "Successfully retrieved user by ID", 28));

            execution.getResults().add(new TestResult(
                "UserServiceTest", "testUpdateUser", "PASSED",
                "Successfully updated user", 41));

            execution.getResults().add(new TestResult(
                "UserServiceTest", "testDeleteUser", "PASSED",
                "Successfully deleted user", 35));

            execution.getResults().add(new TestResult(
                "UserServiceTest", "testFindByUsername", "PASSED",
                "Successfully found user by username", 29));

            execution.addOutput("UserService tests: 6 passed, 0 failed");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void runIntegrationTests(TestSuiteExecution execution) {
        try {
            Thread.sleep(2000); // Simulate test execution time

            // Simulate integration test results
            execution.getResults().add(new TestResult(
                "UserResourceIT", "testHealthEndpoint", "PASSED",
                "Health endpoint responding correctly", 156));

            execution.getResults().add(new TestResult(
                "UserResourceIT", "testGetAllUsers", "PASSED",
                "REST API returns all users", 203));

            execution.getResults().add(new TestResult(
                "UserResourceIT", "testCreateUser", "PASSED",
                "REST API creates user successfully", 189));

            execution.getResults().add(new TestResult(
                "UserResourceIT", "testGetUserById", "PASSED",
                "REST API retrieves user by ID", 145));

            execution.getResults().add(new TestResult(
                "UserResourceIT", "testDeleteUser", "PASSED",
                "REST API deletes user successfully", 167));

            execution.getResults().add(new TestResult(
                "UserResourceIT", "testSearchByUsername", "PASSED",
                "REST API searches by username", 134));

            execution.getResults().add(new TestResult(
                "UserResourceIT", "testSearchByUsernameNotFound", "PASSED",
                "REST API handles user not found", 98));

            execution.addOutput("Integration tests: 7 passed, 0 failed");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public String runSpecificTest(String testClass, String testMethod) {
        String executionId = UUID.randomUUID().toString();
        TestSuiteExecution execution = new TestSuiteExecution(executionId);
        testExecutions.put(executionId, execution);

        // Run specific test asynchronously
        CompletableFuture.runAsync(() -> {
            try {
                execution.addOutput("Running specific test: " + testClass + "." + testMethod);
                Thread.sleep(500); // Simulate test execution

                // Simulate specific test result
                execution.getResults().add(new TestResult(
                    testClass, testMethod, "PASSED",
                    "Test executed successfully", 87));

                execution.setStatus("COMPLETED");
                execution.setEndTime(LocalDateTime.now());
                execution.addOutput("Test completed successfully!");

            } catch (Exception e) {
                execution.setStatus("FAILED");
                execution.setEndTime(LocalDateTime.now());
                execution.addOutput("Test failed: " + e.getMessage());
            }
        });

        return executionId;
    }
}