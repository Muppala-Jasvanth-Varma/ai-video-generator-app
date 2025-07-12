const express = require("express");
const router = express.Router();

// Import Wikipedia Controller
const {
    getDayEvents,
    getEventDetails,
    getYearEvents,
    searchEvents,
    searchPeople,
    getPeopleByEra
} = require("../controllers/wikipediaController");

// 📅 EXISTING ROUTES (Day Events)
router.get("/day/:month/:day", getDayEvents);

// 🔍 EXISTING ROUTES (Event/Person Details)
router.get("/details/:query", getEventDetails);

// ✨ EXISTING ROUTES (Year Events)
router.get("/year/:year", getYearEvents);

// 🎭 NEW ROUTES (Event Search)
router.get("/events/search", searchEvents);

// 👤 NEW ROUTES (People Search)
router.get("/people/search", searchPeople);

// 🏛️ NEW ROUTES (People by Era)
router.get("/people/era/:era", getPeopleByEra);

module.exports = router;