Based on your requirements, here's a step-by-step solution:

**Step 1: Create a login route**

In the back-end, create a new route for handling user login. This route should accept username and password as input and return an authentication token or a success message upon successful login.

Example (Node.js/Express):
```javascript
const express = require('express');
const router = express.Router();
const authController = require('./authController');

router.post('/login', authController.login);
```
**Step 2: Create a login page**

In the front-end, create a new page for displaying the login form. This page should include fields for username and password.

Example (HTML):
```html
<!DOCTYPE html>
<html>
<head>
    <title>Login</title>
</head>
<body>
    <form action="/login" method="post">
        <label>Username:</label>
        <input type="text" name="username"><br><br>
        <label>Password:</label>
        <input type="password" name="password"><br><br>
        <button type="submit">Login</button>
    </form>
</body>
</html>
```
**Step 3: Add login link to admin menu**

In the front-end, add a new link to the admin menu that points to the login page.

Example (HTML):
```html
<nav>
    <ul>
        <!-- existing links -->
        <li><a href="/admin/login">Login</a></li>
    </ul>
</nav>
```
**Step 4: Trigger authentication upon login**

In the front-end, add JavaScript code to trigger authentication when the user clicks on the "Login" link.

Example (JavaScript):
```javascript
const loginLink = document.querySelector('a[href="/admin/login"]');
loginLink.addEventListener('click', () => {
    const usernameInput = document.querySelector('input[name="username"]');
    const passwordInput = document.querySelector('input[name="password"]');

    // Send a POST request to the login route with the form data
    fetch('/login', {
        method: 'POST',
        body: JSON.stringify({
            username: usernameInput.value,
            password: passwordInput.value
        }),
        headers: { 'Content-Type': 'application/json' }
    })
    .then(response => response.json())
    .then(data => {
        // Handle successful login
        console.log('Login successful!');
    })
    .catch(error => {
        // Handle login failure
        console.error('Error logging in:', error);
    });
});
```
Note that this implementation does not handle encoded passwords, as per your request. This will be addressed in the next iteration.

This solution meets the requirements specified:

* The back-end is able to handle the login process.
* When accessing the admin section in the front view, authentication is triggered with a login page.
* The implementation does not handle encoded passwords (this will be done in the next iteration).