const API_BASE = '/openliberty-demo/api';

let currentExecutionId = null;
let pollingInterval = null;
let availableTests = {};

// Initialize the test runner when page loads
document.addEventListener('DOMContentLoaded', () => {
    loadAvailableTests();
    loadRecentExecutions();
    updateStatus('idle', 'Ready to run tests');
});

// Load available tests from the server
async function loadAvailableTests() {
    try {
        const response = await fetch(`${API_BASE}/tests/available`);
        const data = await response.json();
        availableTests = data.testClasses;

        populateTestClassSelect();
    } catch (error) {
        console.error('Error loading available tests:', error);
        showMessage('Failed to load available tests', 'error');
    }
}

// Populate the test class dropdown
function populateTestClassSelect() {
    const testClassSelect = document.getElementById('testClassSelect');
    testClassSelect.innerHTML = '<option value="">Select Test Class</option>';

    Object.keys(availableTests).forEach(testClass => {
        const option = document.createElement('option');
        option.value = testClass;
        option.textContent = testClass;
        testClassSelect.appendChild(option);
    });
}

// Update test methods dropdown when class is selected
function updateTestMethods() {
    const testClassSelect = document.getElementById('testClassSelect');
    const testMethodSelect = document.getElementById('testMethodSelect');
    const selectedClass = testClassSelect.value;

    testMethodSelect.innerHTML = '<option value="">Select Test Method</option>';

    if (selectedClass && availableTests[selectedClass]) {
        availableTests[selectedClass].forEach(method => {
            const option = document.createElement('option');
            option.value = method;
            option.textContent = method;
            testMethodSelect.appendChild(option);
        });
    }
}

// Run all tests
async function runAllTests() {
    if (currentExecutionId && pollingInterval) {
        showMessage('Tests are already running', 'warning');
        return;
    }

    try {
        updateStatus('running', 'Starting test execution...');
        clearResults();

        const response = await fetch(`${API_BASE}/tests/run`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const data = await response.json();
            currentExecutionId = data.executionId;

            showMessage('Test execution started successfully', 'success');
            startPolling();

            // Disable run buttons
            document.getElementById('runAllBtn').disabled = true;
            document.getElementById('runSpecificBtn').disabled = true;

        } else {
            throw new Error('Failed to start test execution');
        }
    } catch (error) {
        console.error('Error running tests:', error);
        showMessage('Failed to start test execution', 'error');
        updateStatus('failed', 'Failed to start tests');
    }
}

// Run specific test
async function runSpecificTest() {
    if (currentExecutionId && pollingInterval) {
        showMessage('Tests are already running', 'warning');
        return;
    }

    const testClass = document.getElementById('testClassSelect').value;
    const testMethod = document.getElementById('testMethodSelect').value;

    if (!testClass || !testMethod) {
        showMessage('Please select both test class and method', 'warning');
        return;
    }

    try {
        updateStatus('running', `Running ${testClass}.${testMethod}...`);
        clearResults();

        const response = await fetch(`${API_BASE}/tests/run/${testClass}/${testMethod}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const data = await response.json();
            currentExecutionId = data.executionId;

            showMessage(`Test execution started: ${testClass}.${testMethod}`, 'success');
            startPolling();

            // Disable run buttons
            document.getElementById('runAllBtn').disabled = true;
            document.getElementById('runSpecificBtn').disabled = true;

        } else {
            throw new Error('Failed to start test execution');
        }
    } catch (error) {
        console.error('Error running specific test:', error);
        showMessage('Failed to start test execution', 'error');
        updateStatus('failed', 'Failed to start test');
    }
}

// Start polling for test results
function startPolling() {
    if (pollingInterval) {
        clearInterval(pollingInterval);
    }

    // Show progress bar
    document.getElementById('progressBar').style.display = 'block';

    pollingInterval = setInterval(async () => {
        await checkTestStatus();
    }, 1000); // Poll every second
}

// Check test execution status
async function checkTestStatus() {
    if (!currentExecutionId) return;

    try {
        const response = await fetch(`${API_BASE}/tests/execution/${currentExecutionId}`);

        if (response.ok) {
            const execution = await response.json();
            updateTestDisplay(execution);

            if (execution.status === 'COMPLETED' || execution.status === 'FAILED') {
                stopPolling();
                enableRunButtons();
                loadRecentExecutions();

                if (execution.status === 'COMPLETED') {
                    showMessage('Test execution completed successfully', 'success');
                } else {
                    showMessage('Test execution failed', 'error');
                }
            }
        }
    } catch (error) {
        console.error('Error checking test status:', error);
    }
}

// Update test display with execution results
function updateTestDisplay(execution) {
    updateStatus(execution.status.toLowerCase(), getStatusMessage(execution));
    updateTestResults(execution.results || []);
    updateTestOutput(execution.output || '');
    updateTestSummary(execution.results || []);
}

// Get status message based on execution state
function getStatusMessage(execution) {
    switch (execution.status) {
        case 'RUNNING':
            return 'Running tests...';
        case 'COMPLETED':
            const total = execution.results ? execution.results.length : 0;
            const passed = execution.results ? execution.results.filter(r => r.status === 'PASSED').length : 0;
            return `Completed: ${passed}/${total} tests passed`;
        case 'FAILED':
            return 'Test execution failed';
        default:
            return 'Unknown status';
    }
}

// Update status indicator and text
function updateStatus(status, message) {
    const indicator = document.getElementById('statusIndicator');
    const text = document.getElementById('statusText');

    // Reset classes
    indicator.className = 'status-indicator';

    // Add appropriate class
    switch (status) {
        case 'running':
            indicator.classList.add('status-running');
            break;
        case 'completed':
            indicator.classList.add('status-completed');
            break;
        case 'failed':
            indicator.classList.add('status-failed');
            break;
        default:
            indicator.classList.add('status-idle');
    }

    text.textContent = message;
}

// Update test results display
function updateTestResults(results) {
    const resultsContainer = document.getElementById('testResults');

    if (results.length === 0) {
        resultsContainer.innerHTML = `
            <p style="text-align: center; color: #6b7280; margin: 20px 0;">
                No test results yet. Run tests to see results here.
            </p>
        `;
        return;
    }

    resultsContainer.innerHTML = results.map(result => `
        <div class="test-result-item test-result-${result.status.toLowerCase()}">
            <div class="test-info">
                <div class="test-name">${result.testClass}</div>
                <div class="test-method">${result.testMethod}</div>
                ${result.message ? `<div style="font-size: 12px; color: #6b7280; margin-top: 4px;">${result.message}</div>` : ''}
            </div>
            <div class="test-duration">${result.duration}ms</div>
        </div>
    `).join('');
}

// Update test output display
function updateTestOutput(output) {
    const outputContainer = document.getElementById('testOutput');
    outputContainer.textContent = output || 'No output available';

    // Auto-scroll to bottom
    outputContainer.scrollTop = outputContainer.scrollHeight;
}

// Update test summary
function updateTestSummary(results) {
    const summaryContainer = document.getElementById('testSummary');

    if (results.length === 0) {
        summaryContainer.style.display = 'none';
        return;
    }

    summaryContainer.style.display = 'grid';

    const total = results.length;
    const passed = results.filter(r => r.status === 'PASSED').length;
    const failed = results.filter(r => r.status === 'FAILED').length;
    const skipped = results.filter(r => r.status === 'SKIPPED').length;

    document.getElementById('totalTests').textContent = total;
    document.getElementById('passedTests').textContent = passed;
    document.getElementById('failedTests').textContent = failed;
    document.getElementById('skippedTests').textContent = skipped;
}

// Load recent test executions
async function loadRecentExecutions() {
    try {
        const response = await fetch(`${API_BASE}/tests/executions`);
        const executions = await response.json();

        displayRecentExecutions(executions.slice(0, 5)); // Show last 5 executions
    } catch (error) {
        console.error('Error loading recent executions:', error);
    }
}

// Display recent executions
function displayRecentExecutions(executions) {
    const container = document.getElementById('recentExecutions');

    if (executions.length === 0) {
        container.innerHTML = '<p style="text-align: center; color: #6b7280;">No recent executions</p>';
        return;
    }

    container.innerHTML = executions.map(execution => {
        const total = execution.results ? execution.results.length : 0;
        const passed = execution.results ? execution.results.filter(r => r.status === 'PASSED').length : 0;
        const failed = execution.results ? execution.results.filter(r => r.status === 'FAILED').length : 0;

        return `
            <div class="test-result-item" style="margin-bottom: 10px; cursor: pointer;"
                 onclick="loadExecution('${execution.id}')">
                <div class="test-info">
                    <div class="test-name">Execution ${execution.id.substring(0, 8)}</div>
                    <div class="test-method">
                        Status: ${execution.status} |
                        Tests: ${total} |
                        Passed: ${passed} |
                        Failed: ${failed}
                    </div>
                    <div style="font-size: 12px; color: #6b7280;">
                        Started: ${new Date(execution.startTime).toLocaleString()}
                    </div>
                </div>
                <div class="status-indicator status-${execution.status.toLowerCase()}"></div>
            </div>
        `;
    }).join('');
}

// Load specific execution
async function loadExecution(executionId) {
    try {
        const response = await fetch(`${API_BASE}/tests/execution/${executionId}`);
        const execution = await response.json();

        updateTestDisplay(execution);
        currentExecutionId = executionId;

        showMessage(`Loaded execution ${executionId.substring(0, 8)}`, 'success');
    } catch (error) {
        console.error('Error loading execution:', error);
        showMessage('Failed to load execution', 'error');
    }
}

// Stop polling
function stopPolling() {
    if (pollingInterval) {
        clearInterval(pollingInterval);
        pollingInterval = null;
    }

    // Hide progress bar
    document.getElementById('progressBar').style.display = 'none';

    currentExecutionId = null;
}

// Enable run buttons
function enableRunButtons() {
    document.getElementById('runAllBtn').disabled = false;
    document.getElementById('runSpecificBtn').disabled = false;
}

// Clear results
function clearResults() {
    updateTestResults([]);
    updateTestOutput('Starting test execution...');
    document.getElementById('testSummary').style.display = 'none';
}

// Refresh results
function refreshResults() {
    if (currentExecutionId) {
        checkTestStatus();
    }
    loadRecentExecutions();
    showMessage('Results refreshed', 'success');
}

// Show message to user
function showMessage(message, type) {
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${type}`;
    messageDiv.textContent = message;

    const container = document.querySelector('.test-controls');
    container.insertBefore(messageDiv, container.firstChild);

    setTimeout(() => {
        messageDiv.remove();
    }, 3000);
}