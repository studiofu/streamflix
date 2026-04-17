const jwt = require('jsonwebtoken');

const token = jwt.sign(
  { userId: "secure_user_999" }, // The data we want to secure
  "super-secret-streamflix-key", // The secret key
  { expiresIn: '1h' }            // Token lifespan
);

console.log("Your Test JWT Token:");
console.log("Bearer " + token);
