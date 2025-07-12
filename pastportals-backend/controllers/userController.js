const User = require("../models/userModel");
const Login = require("../models/loginModel");  // Import login model


const bcrypt = require("bcrypt");
const signup = async (req, res) => {
    try {
        console.log("Signup Request Body:", req.body); // Debug: Print received data

        const { username, name, email, mobileNumber, password, confirmPassword } = req.body;

        if (!username || !name || !email || !mobileNumber || !password || !confirmPassword) {
            console.log("Error: Missing fields");
            return res.status(400).json({ message: "All fields are required" });
        }

        if (password !== confirmPassword) {
            console.log("Error: Passwords do not match");
            return res.status(400).json({ message: "Passwords do not match" });
        }

        const existingUser = await User.findOne({ email });
        if (existingUser) {
            console.log("Error: User already exists");
            return res.status(400).json({ message: "User already exists" });
        }

        const hashedPassword = await bcrypt.hash(password, 10);

        const newUser = new User({
            username,
            name,
            email,
            mobileNumber,
            password: hashedPassword
        });

        await newUser.save();
        res.status(201).json({ message: "User registered successfully" });

    } catch (error) {
        console.log("Signup Error:", error.message); // Print the error in terminal
        res.status(500).json({ message: "Error during signup", error: error.message });
    }
};

const login = async (req, res) => {
    try {
        console.log("Login Request Received:", req.body); // ✅ Debugging Log

        const { email, password } = req.body;
        if (!email || !password) {
            console.log("Error: Missing email or password");
            return res.status(400).json({ message: "Email and password are required" });
        }

        const user = await User.findOne({ email });
        if (!user) {
            console.log("Error: User not found");
            return res.status(400).json({ message: "User not found" });
        }

        const isMatch = await bcrypt.compare(password, user.password);
        if (!isMatch) {
            console.log("Error: Invalid credentials");
            return res.status(400).json({ message: "Invalid credentials" });
        }

        console.log("Login successful for user:", user.username);

        res.status(200).json({ message: "Login successful" });

    } catch (error) {
        console.log("Login Error:", error.message);
        res.status(500).json({ message: "Error during login", error: error.message });
    }
};

module.exports = { signup, login };  // Ensure functions are exported