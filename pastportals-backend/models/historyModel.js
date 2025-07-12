const mongoose = require("mongoose");

const historySchema = new mongoose.Schema({
  year: { type: String, required: true },
  events: { type: Array, required: true }
});

module.exports = mongoose.model("History", historySchema);
