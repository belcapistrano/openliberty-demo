package com.example.demo.model;

import java.time.LocalDateTime;
import java.util.List;

public class TestResult {
    private String testClass;
    private String testMethod;
    private String status; // PASSED, FAILED, SKIPPED
    private String message;
    private long duration;
    private LocalDateTime timestamp;
    private String stackTrace;

    public TestResult() {
        this.timestamp = LocalDateTime.now();
    }

    public TestResult(String testClass, String testMethod, String status, String message, long duration) {
        this.testClass = testClass;
        this.testMethod = testMethod;
        this.status = status;
        this.message = message;
        this.duration = duration;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and setters
    public String getTestClass() { return testClass; }
    public void setTestClass(String testClass) { this.testClass = testClass; }

    public String getTestMethod() { return testMethod; }
    public void setTestMethod(String testMethod) { this.testMethod = testMethod; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getStackTrace() { return stackTrace; }
    public void setStackTrace(String stackTrace) { this.stackTrace = stackTrace; }
}

class TestSuite {
    private String name;
    private List<TestResult> testResults;
    private long totalDuration;
    private int totalTests;
    private int passedTests;
    private int failedTests;
    private int skippedTests;
    private LocalDateTime executionTime;

    public TestSuite() {
        this.executionTime = LocalDateTime.now();
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<TestResult> getTestResults() { return testResults; }
    public void setTestResults(List<TestResult> testResults) { this.testResults = testResults; }

    public long getTotalDuration() { return totalDuration; }
    public void setTotalDuration(long totalDuration) { this.totalDuration = totalDuration; }

    public int getTotalTests() { return totalTests; }
    public void setTotalTests(int totalTests) { this.totalTests = totalTests; }

    public int getPassedTests() { return passedTests; }
    public void setPassedTests(int passedTests) { this.passedTests = passedTests; }

    public int getFailedTests() { return failedTests; }
    public void setFailedTests(int failedTests) { this.failedTests = failedTests; }

    public int getSkippedTests() { return skippedTests; }
    public void setSkippedTests(int skippedTests) { this.skippedTests = skippedTests; }

    public LocalDateTime getExecutionTime() { return executionTime; }
    public void setExecutionTime(LocalDateTime executionTime) { this.executionTime = executionTime; }
}