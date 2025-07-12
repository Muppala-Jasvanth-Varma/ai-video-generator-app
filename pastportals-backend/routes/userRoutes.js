const express = require("express");
const { signup, login } = require("../controllers/userController");

const router = express.Router();

router.post("/signup", signup);
router.post("/login", login);  // ✅ Add this line

module.exports = router;