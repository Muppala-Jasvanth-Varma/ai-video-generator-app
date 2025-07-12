const mongoose = require("mongoose");

const browsedDataSchema = new mongoose.Schema({
    searchQuery: { type: String, required: true },
    searchResults: { type: Object, required: true },
    timestamp: { type: Date, default: Date.now }
});

module.exports = mongoose.model("BrowsedData", browsedDataSchema);
