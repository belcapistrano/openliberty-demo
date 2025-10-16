const API_BASE = '/openliberty-demo/api';

// Check health status on page load
document.addEventListener('DOMContentLoaded', () => {
    checkHealth();
    loadUsers();
});

async function checkHealth() {
    try {
        const response = await fetch(`${API_BASE}/health`);
        const data = await response.json();
        const statusElement = document.getElementById('healthStatus');

        if (data.status === 'UP') {
            statusElement.textContent = 'Service is UP';
            statusElement.className = 'status-badge up';
        } else {
            statusElement.textContent = 'Service is DOWN';
            statusElement.className = 'status-badge down';
        }
    } catch (error) {
        const statusElement = document.getElementById('healthStatus');
        statusElement.textContent = 'Service is DOWN';
        statusElement.className = 'status-badge down';
    }
}

async function loadUsers() {
    try {
        const response = await fetch(`${API_BASE}/users`);
        const users = await response.json();
        displayUsers(users);
    } catch (error) {
        console.error('Error loading users:', error);
        showMessage('Failed to load users', 'error');
    }
}

function displayUsers(users) {
    const usersList = document.getElementById('usersList');

    if (users.length === 0) {
        usersList.innerHTML = '<p style="text-align: center; color: #6b7280;">No users found</p>';
        return;
    }

    usersList.innerHTML = users.map(user => `
        <div class="user-card">
            <div class="user-info">
                <h4>${user.fullName || 'No name'}</h4>
                <p>Username: ${user.username} | Email: ${user.email}</p>
                <p style="font-size: 12px; color: #9ca3af;">ID: ${user.id}</p>
            </div>
            <button onclick="deleteUser(${user.id})" class="btn btn-danger">Delete</button>
        </div>
    `).join('');
}

function toggleAddForm() {
    const form = document.getElementById('addUserForm');
    form.classList.toggle('hidden');

    // Clear form if hiding
    if (form.classList.contains('hidden')) {
        document.getElementById('username').value = '';
        document.getElementById('email').value = '';
        document.getElementById('fullName').value = '';
    }
}

async function addUser(event) {
    event.preventDefault();

    const userData = {
        username: document.getElementById('username').value,
        email: document.getElementById('email').value,
        fullName: document.getElementById('fullName').value
    };

    try {
        const response = await fetch(`${API_BASE}/users`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(userData)
        });

        if (response.ok) {
            showMessage('User created successfully!', 'success');
            toggleAddForm();
            loadUsers();
        } else {
            throw new Error('Failed to create user');
        }
    } catch (error) {
        console.error('Error creating user:', error);
        showMessage('Failed to create user', 'error');
    }
}

async function deleteUser(id) {
    if (!confirm('Are you sure you want to delete this user?')) {
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/users/${id}`, {
            method: 'DELETE'
        });

        if (response.ok) {
            showMessage('User deleted successfully!', 'success');
            loadUsers();
        } else {
            throw new Error('Failed to delete user');
        }
    } catch (error) {
        console.error('Error deleting user:', error);
        showMessage('Failed to delete user', 'error');
    }
}

function showMessage(message, type) {
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${type}`;
    messageDiv.textContent = message;

    const container = document.querySelector('.api-section');
    container.insertBefore(messageDiv, container.firstChild);

    setTimeout(() => {
        messageDiv.remove();
    }, 3000);
}