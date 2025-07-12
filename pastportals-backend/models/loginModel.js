const mongoose = require("mongoose");

const loginSchema = new mongoose.Schema({
    username: { type: String, required: true },
    email: { type: String, required: true },
    loggedAt: { type: Date, default: Date.now }
});

const Login = mongoose.model("Login", loginSchema);
module.exports = Login;
