const jwt = require('jsonwebtoken');

const token = jwt.sign(
  { userId: "secure_user_999", typ: "access" },
  "super-secret-streamflix-key",
  { expiresIn: "15m" }
);

console.log("Your Test JWT Token:");
console.log("Bearer " + token);
