const axios = require("axios");
const generateAudio = require('../utils/generateAudio');

// 📅 Get Top 5 Events for a Specific Day
const getDayEvents = async (req, res) => {
    const { month, day } = req.params;

    // Input validation improvements
    const monthNum = parseInt(month);
    const dayNum = parseInt(day);
    
    if (!month || !day || isNaN(monthNum) || isNaN(dayNum) || 
        monthNum < 1 || monthNum > 12 || dayNum < 1 || dayNum > 31) {
        return res.status(400).json({ 
            success: false,
            error: "Invalid date. Please provide a valid month (1-12) and day (1-31)." 
        });
    }

    try {
        // Format month and day with leading zeros
        const formattedMonth = monthNum.toString().padStart(2, '0');
        const formattedDay = dayNum.toString().padStart(2, '0');
        
        const response = await axios.get(
            `https://en.wikipedia.org/api/rest_v1/feed/onthisday/all/${formattedMonth}/${formattedDay}`,
            {
                timeout: 10000, // 10 second timeout
                headers: {
                    'User-Agent': 'PastPortals/1.0 (your-email@example.com)' // Better to identify your app
                }
            }
        );
        
        const events = response.data.events?.slice(0, 5).map(event => ({
            title: event.text,
            year: event.year,
            pages: event.pages?.map(p => p.title) || []
        })) || [];

        res.status(200).json({ success: true, events });
    } catch (error) {
        console.error("Day Events Fetch Error:", error.message);
        res.status(500).json({ 
            success: false,
            error: "Failed to fetch daily events.", 
            details: process.env.NODE_ENV === 'development' ? error.message : 'Internal server error'
        });
    }
};

// 🔍 Get Event/Person Details for Prompt + Image
const getEventDetails = async (req, res) => {
    const { query } = req.params;

    if (!query || query.trim().length === 0) {
        return res.status(400).json({ 
            success: false,
            error: "Query parameter is required and cannot be empty." 
        });
    }

    try {
        const response = await axios.get(
            `https://en.wikipedia.org/api/rest_v1/page/summary/${encodeURIComponent(query)}`,
            {
                timeout: 10000,
                headers: {
                    'User-Agent': 'PastPortals/1.0 (your-email@example.com)'
                }
            }
        );
        
        const data = response.data;

        // Handle disambiguation pages or missing pages
        if (data.type === 'disambiguation') {
            return res.status(400).json({
                success: false,
                error: "Query returned a disambiguation page. Please be more specific.",
                suggestions: data.extract
            });
        }

        const eventData = {
            title: data.title,
            description: data.extract || "No description available.",
            pageUrl: data.content_urls?.desktop?.page || null,
            thumbnail: data.thumbnail?.source || null,
            image_prompt: data.extract ? 
                `Create an artistic representation of "${data.title}": ${data.extract.substring(0, 200)}...` :
                `Create an artistic representation of "${data.title}"`
        };

        res.status(200).json({ success: true, eventData });
    } catch (error) {
        console.error("Event Details Fetch Error:", error.message);
        
        if (error.response?.status === 404) {
            return res.status(404).json({
                success: false,
                error: "Event or person not found. Please check your query and try again."
            });
        }
        
        res.status(500).json({ 
            success: false,
            error: "Failed to fetch event/person details.", 
            details: process.env.NODE_ENV === 'development' ? error.message : 'Internal server error'
        });
    }
};

// 🎭 Search for Historical Events
const searchEvents = async (req, res) => {
    const { query, year } = req.query;

    if (!query || query.trim().length === 0) {
        return res.status(400).json({ 
            success: false,
            error: "Search query is required and cannot be empty." 
        });
    }

    try {
        console.log(`🔍 Searching for events: ${query}`);
        
        // Build search query
        let searchQuery = query.trim();
        if (year && !isNaN(year)) {
            searchQuery += ` ${year}`;
        }
        searchQuery += ' battle war event historical';

        // Search Wikipedia for events
        const searchResponse = await axios.get(`https://en.wikipedia.org/w/api.php`, {
            params: {
                action: 'query',
                format: 'json',
                list: 'search',
                srsearch: searchQuery,
                srlimit: 10,
                srprop: 'snippet|titlesnippet'
            },
            timeout: 10000,
            headers: {
                'User-Agent': 'PastPortals/1.0 (your-email@example.com)'
            }
        });

        const searchResults = searchResponse.data.query?.search || [];
        
        if (searchResults.length === 0) {
            return res.status(200).json({
                success: true,
                events: [],
                totalFound: 0,
                message: "No events found for your search query."
            });
        }

        // Get detailed info for top results with better error handling
        const eventPromises = searchResults.slice(0, 5).map(async (result) => {
            try {
                const detailResponse = await axios.get(
                    `https://en.wikipedia.org/api/rest_v1/page/summary/${encodeURIComponent(result.title)}`,
                    {
                        timeout: 5000,
                        headers: {
                            'User-Agent': 'PastPortals/1.0 (your-email@example.com)'
                        }
                    }
                );
                const detail = detailResponse.data;
                
                return {
                    title: detail.title,
                    description: detail.extract || result.snippet || "No description available.",
                    snippet: result.snippet,
                    pageUrl: detail.content_urls?.desktop?.page || 
                             `https://en.wikipedia.org/wiki/${encodeURIComponent(result.title)}`,
                    thumbnail: detail.thumbnail?.source || null,
                    type: 'event'
                };
            } catch (error) {
                console.warn(`Failed to get details for ${result.title}:`, error.message);
                return {
                    title: result.title,
                    description: result.snippet || "No description available.",
                    snippet: result.snippet,
                    pageUrl: `https://en.wikipedia.org/wiki/${encodeURIComponent(result.title)}`,
                    thumbnail: null,
                    type: 'event'
                };
            }
        });

        const events = await Promise.all(eventPromises);
        const validEvents = events.filter(event => event !== null);

        res.status(200).json({
            success: true,
            events: validEvents,
            totalFound: searchResults.length
        });

    } catch (error) {
        console.error("Event Search Error:", error.message);
        res.status(500).json({ 
            success: false,
            error: "Failed to search events.", 
            details: process.env.NODE_ENV === 'development' ? error.message : 'Internal server error'
        });
    }
};

// 👤 Search for Historical People
const searchPeople = async (req, res) => {
    const { query, era, occupation } = req.query;

    if (!query || query.trim().length === 0) {
        return res.status(400).json({ 
            success: false,
            error: "Search query is required and cannot be empty." 
        });
    }

    try {
        console.log(`🔍 Searching for people: ${query}`);
        
        // Build search query with additional context
        let searchQuery = query.trim();
        if (era && era.trim()) searchQuery += ` ${era.trim()}`;
        if (occupation && occupation.trim()) searchQuery += ` ${occupation.trim()}`;
        searchQuery += ' biography historical figure person';

        const searchResponse = await axios.get(`https://en.wikipedia.org/w/api.php`, {
            params: {
                action: 'query',
                format: 'json',
                list: 'search',
                srsearch: searchQuery,
                srlimit: 10,
                srprop: 'snippet|titlesnippet'
            },
            timeout: 10000,
            headers: {
                'User-Agent': 'PastPortals/1.0 (your-email@example.com)'
            }
        });

        const searchResults = searchResponse.data.query?.search || [];
        
        if (searchResults.length === 0) {
            return res.status(200).json({
                success: true,
                people: [],
                totalFound: 0,
                message: "No people found for your search query."
            });
        }

        // Get detailed info for top results
        const peoplePromises = searchResults.slice(0, 5).map(async (result) => {
            try {
                const detailResponse = await axios.get(
                    `https://en.wikipedia.org/api/rest_v1/page/summary/${encodeURIComponent(result.title)}`,
                    {
                        timeout: 5000,
                        headers: {
                            'User-Agent': 'PastPortals/1.0 (your-email@example.com)'
                        }
                    }
                );
                const detail = detailResponse.data;
                
                // Improved year extraction with better regex
                const yearMatches = detail.extract?.match(/\b(1[0-9]{3}|2[0-9]{3})\b/g) || [];
                const birthYear = yearMatches[0] || null;
                const deathYear = yearMatches.length > 1 ? yearMatches[1] : null;
                
                return {
                    title: detail.title,
                    description: detail.extract || result.snippet || "No description available.",
                    snippet: result.snippet,
                    pageUrl: detail.content_urls?.desktop?.page || 
                             `https://en.wikipedia.org/wiki/${encodeURIComponent(result.title)}`,
                    thumbnail: detail.thumbnail?.source || null,
                    birthYear: birthYear,
                    deathYear: deathYear,
                    type: 'person'
                };
            } catch (error) {
                console.warn(`Failed to get details for ${result.title}:`, error.message);
                return {
                    title: result.title,
                    description: result.snippet || "No description available.",
                    snippet: result.snippet,
                    pageUrl: `https://en.wikipedia.org/wiki/${encodeURIComponent(result.title)}`,
                    thumbnail: null,
                    birthYear: null,
                    deathYear: null,
                    type: 'person'
                };
            }
        });

        const people = await Promise.all(peoplePromises);
        const validPeople = people.filter(person => person !== null);

        res.status(200).json({
            success: true,
            people: validPeople,
            totalFound: searchResults.length
        });

    } catch (error) {
        console.error("People Search Error:", error.message);
        res.status(500).json({ 
            success: false,
            error: "Failed to search people.", 
            details: process.env.NODE_ENV === 'development' ? error.message : 'Internal server error'
        });
    }
};

// 🏛️ Get Historical People by Era/Century
const getPeopleByEra = async (req, res) => {
    const { era } = req.params;

    if (!era || era.trim().length === 0) {
        return res.status(400).json({ 
            success: false,
            error: "Era parameter is required and cannot be empty." 
        });
    }

    try {
        console.log(`🔍 Fetching people from era: ${era}`);

        // Search for people from specific era
        const searchResponse = await axios.get(`https://en.wikipedia.org/w/api.php`, {
            params: {
                action: 'query',
                format: 'json',
                list: 'search',
                srsearch: `"${era.trim()}" people historical figures biography`,
                srlimit: 15,
                srprop: 'snippet|titlesnippet'
            },
            timeout: 10000,
            headers: {
                'User-Agent': 'PastPortals/1.0 (your-email@example.com)'
            }
        });

        const searchResults = searchResponse.data.query?.search || [];
        
        // Filter and get detailed info with improved filtering
        const filteredResults = searchResults.filter(result => {
            const title = result.title.toLowerCase();
            const snippet = result.snippet.toLowerCase();
            
            // Exclude list pages, categories, and templates
            if (title.includes('list') || title.includes('category:') || 
                title.includes('template:') || title.includes('disambiguation')) {
                return false;
            }
            
            // Include if snippet contains biographical indicators
            return snippet.includes('biography') || snippet.includes('born') || 
                   snippet.includes('died') || snippet.includes('was a') || 
                   snippet.includes('historian') || snippet.includes('politician') ||
                   snippet.includes('writer') || snippet.includes('artist');
        });

        const peoplePromises = filteredResults
            .slice(0, 8)
            .map(async (result) => {
                try {
                    const detailResponse = await axios.get(
                        `https://en.wikipedia.org/api/rest_v1/page/summary/${encodeURIComponent(result.title)}`,
                        {
                            timeout: 5000,
                            headers: {
                                'User-Agent': 'PastPortals/1.0 (your-email@example.com)'
                            }
                        }
                    );
                    const detail = detailResponse.data;
                    
                    return {
                        title: detail.title,
                        description: detail.extract || result.snippet || "No description available.",
                        pageUrl: detail.content_urls?.desktop?.page || 
                                 `https://en.wikipedia.org/wiki/${encodeURIComponent(result.title)}`,
                        thumbnail: detail.thumbnail?.source || null,
                        era: era,
                        type: 'person'
                    };
                } catch (error) {
                    console.warn(`Failed to get details for ${result.title}:`, error.message);
                    return null;
                }
            });

        const people = (await Promise.all(peoplePromises)).filter(person => person !== null);

        res.status(200).json({
            success: true,
            era: era,
            people: people,
            totalFound: people.length
        });

    } catch (error) {
        console.error("Era People Fetch Error:", error.message);
        res.status(500).json({ 
            success: false,
            error: "Failed to fetch people by era.", 
            details: process.env.NODE_ENV === 'development' ? error.message : 'Internal server error'
        });
    }
};

// ✨ Get Summary of a Year with Major Events (ENHANCED)
const getYearEvents = async (req, res) => {
    const { year } = req.params;

    const yearNum = parseInt(year);
    if (!year || isNaN(yearNum) || yearNum < 1 || yearNum > new Date().getFullYear()) {
        return res.status(400).json({ 
            success: false,
            error: `Invalid year. Please provide a valid year between 1 and ${new Date().getFullYear()}.` 
        });
    }

    try {
        console.log(`🔍 Fetching events for year: ${year} (India focus)`);

        // ✅ Strategy 1: Try to get the Wikipedia page for the year in India
        let yearData = null;
        try {
            const yearResponse = await axios.get(
                `https://en.wikipedia.org/api/rest_v1/page/summary/${year}_in_India`,
                {
                    timeout: 5000,
                    headers: {
                        'User-Agent': 'PastPortals/1.0 (your-email@example.com)'
                    }
                }
            );
            yearData = yearResponse.data;
        } catch (yearError) {
            console.log(`⚠️ No direct Wikipedia page for ${year} in India, trying general year page`);
            // Fallback to general year page
            try {
                const generalYearResponse = await axios.get(
                    `https://en.wikipedia.org/api/rest_v1/page/summary/${year}`,
                    {
                        timeout: 5000,
                        headers: {
                            'User-Agent': 'PastPortals/1.0 (your-email@example.com)'
                        }
                    }
                );
                yearData = generalYearResponse.data;
            } catch (generalError) {
                console.log(`⚠️ No Wikipedia page found for year ${year}`);
            }
        }

        // ✅ Strategy 2: Search for India-specific events using Wikipedia search
        let indiaEvents = [];
        try {
            const indiaSearchResponse = await axios.get(`https://en.wikipedia.org/w/api.php`, {
                params: {
                    action: 'query',
                    format: 'json',
                    list: 'search',
                    srsearch: `India ${year} events history politics independence`,
                    srlimit: 10,
                    srprop: 'snippet'
                },
                timeout: 5000,
                headers: {
                    'User-Agent': 'PastPortals/1.0 (your-email@example.com)'
                }
            });
            
            indiaEvents = indiaSearchResponse.data.query?.search?.map(event => ({
                title: event.title,
                text: event.snippet.replace(/<[^>]*>/g, ''), // Remove HTML tags
                year: year
            })) || [];
        } catch (searchError) {
            console.log("Could not search for India-specific events:", searchError.message);
        }

        // ✅ Strategy 3: Collect events from significant dates in Indian history
        const significantIndianDates = [
            { month: 1, day: 26 },  // Republic Day
            { month: 8, day: 15 },  // Independence Day
            { month: 10, day: 2 },  // Gandhi Jayanti
            { month: 11, day: 14 }, // Nehru's Birthday (Children's Day)
            { month: 4, day: 13 },  // Baisakhi/Jallianwala Bagh remembrance
            { month: 3, day: 12 },  // Dandi March day
        ];

        const eventPromises = significantIndianDates.map(async (date) => {
            try {
                const formattedMonth = date.month.toString().padStart(2, '0');
                const formattedDay = date.day.toString().padStart(2, '0');
                
                const response = await axios.get(
                    `https://en.wikipedia.org/api/rest_v1/feed/onthisday/all/${formattedMonth}/${formattedDay}`,
                    {
                        timeout: 5000,
                        headers: {
                            'User-Agent': 'PastPortals/1.0 (your-email@example.com)'
                        }
                    }
                );
                
                // Filter events for the specific year and India-related content
                const yearEvents = response.data.events?.filter(event => {
                    return event.year == year && 
                           (event.text?.toLowerCase().includes('india') || 
                            event.text?.toLowerCase().includes('indian') ||
                            event.text?.toLowerCase().includes('delhi') ||
                            event.text?.toLowerCase().includes('mumbai') ||
                            event.text?.toLowerCase().includes('calcutta') ||
                            event.text?.toLowerCase().includes('kolkata') ||
                            event.text?.toLowerCase().includes('chennai') ||
                            event.text?.toLowerCase().includes('madras') ||
                            event.text?.toLowerCase().includes('bengal') ||
                            event.text?.toLowerCase().includes('punjab') ||
                            event.text?.toLowerCase().includes('gandhi') ||
                            event.text?.toLowerCase().includes('nehru') ||
                            event.text?.toLowerCase().includes('british raj') ||
                            event.text?.toLowerCase().includes('mughal') ||
                            event.text?.toLowerCase().includes('maratha'));
                }) || [];
                
                return {
                    date: `${date.month}/${date.day}`,
                    events: yearEvents
                };
            } catch (error) {
                console.warn(`Failed to fetch events for ${date.month}/${date.day}:`, error.message);
                return { date: `${date.month}/${date.day}`, events: [] };
            }
        });

        const allDateResults = await Promise.all(eventPromises);
        let yearEvents = [];
        
        allDateResults.forEach(dateResult => {
            yearEvents.push(...dateResult.events);
        });

        // Add events from India search to the main events array
        yearEvents.push(...indiaEvents.slice(0, 5)); // Limit to top 5 search results

        // ✅ Strategy 4: Search for notable Indian people who died/were born in this year
        let notablePeople = [];
        try {
            const peopleResponse = await axios.get(`https://en.wikipedia.org/w/api.php`, {
                params: {
                    action: 'query',
                    format: 'json',
                    list: 'search',
                    srsearch: `Indian born ${year} died ${year} biography politician freedom fighter`,
                    srlimit: 5,
                    srprop: 'snippet'
                },
                timeout: 5000,
                headers: {
                    'User-Agent': 'PastPortals/1.0 (your-email@example.com)'
                }
            });
            
            notablePeople = peopleResponse.data.query?.search?.map(person => ({
                name: person.title,
                snippet: person.snippet.replace(/<[^>]*>/g, '') // Remove HTML tags
            })) || [];
        } catch (peopleError) {
            console.log("Could not fetch notable Indian people for the year:", peopleError.message);
        }

        // ✅ Strategy 5: If no events found, create a generic Indian historical context
        if (yearEvents.length === 0) {
            console.log(`📝 No specific events found, creating generic Indian summary for ${year}`);
            
            const getIndianYearContext = (year) => {
                const yearNum = parseInt(year);
                if (yearNum >= 1857 && yearNum <= 1857) return "First War of Independence";
                if (yearNum >= 1858 && yearNum <= 1947) return "British Colonial period";
                if (yearNum >= 1947 && yearNum <= 1950) return "Independence and Partition era";
                if (yearNum >= 1950 && yearNum <= 1975) return "Early Republic period";
                if (yearNum >= 1975 && yearNum <= 1977) return "Emergency period";
                if (yearNum >= 1991 && yearNum <= 2000) return "Economic liberalization era";
                if (yearNum >= 2000 && yearNum <= 2010) return "IT boom and modernization period";
                if (yearNum >= 1526 && yearNum <= 1857) return "Mughal Empire period";
                if (yearNum >= 1200 && yearNum <= 1526) return "Delhi Sultanate period";
                if (yearNum >= 320 && yearNum <= 550) return "Gupta Empire period";
                if (yearNum >= 1 && yearNum <= 320) return "Ancient Indian kingdoms period";
                return "significant period in Indian history";
            };

            const context = getIndianYearContext(year);
            const genericSummary = `${year} was part of the ${context} in Indian history. This year saw various political, social, and cultural developments that contributed to shaping modern India.`;

            return res.status(200).json({
                success: true,
                yearSummary: {
                    year: year,
                    paragraph: yearData?.extract || genericSummary,
                    storytelling_summary: yearData?.extract || genericSummary,
                    timeline: [
                        {
                            title: `Historical developments in India during ${year}`,
                            date: year,
                            impact: genericSummary
                        }
                    ],
                    notablePeople: notablePeople,
                    image_prompts: [
                        `Historical scene from India in ${year} showing the ${context}`,
                        `Artistic representation of Indian life and society in ${year}`,
                        `Cultural and architectural aspects of India in ${year}`,
                        `Indian historical figures and events from ${year}`
                    ]
                }
            });
        }

        // ✅ Process found events
        const topEvents = yearEvents.slice(0, 8); // Increased to show more Indian events

        const timeline = topEvents.map(event => ({
            title: event.text ? event.text.split(".")[0] : (event.title || "Historical Event in India"),
            date: year,
            impact: event.text || event.title || "Significant impact on Indian history"
        }));

        const image_prompts = topEvents.slice(0, 4).map(event =>
            `An artistic scene depicting the Indian historical event in ${year}: ${event.text || event.title}`
        );



        let storytelling_summary = `In ${year}, India experienced significant historical developments. ${topEvents.map(e => e.text || e.title).slice(0, 3).join(" ")} These events played crucial roles in shaping the Indian subcontinent's political, social, and cultural landscape.`;

        // ✅ Use year page summary if available, with India focus
        if (yearData && yearData.extract) {
            storytelling_summary = yearData.extract;
        }
        
        console.log(`✅ Successfully found ${yearEvents.length} India-related events for year ${year}`);

        res.status(200).json({
            success: true,
            yearSummary: {
                year: year,
                paragraph: storytelling_summary,
                storytelling_summary: storytelling_summary,
                timeline: timeline,
                notablePeople: notablePeople,
                image_prompts: image_prompts
            }
        });

    } catch (error) {
        console.error("❌ Year Events Fetch Error:", error.message);
        console.error("❌ Full error:", error);
        res.status(500).json({ 
            success: false,
            error: "Failed to fetch Indian year-based events.", 
            details: process.env.NODE_ENV === 'development' ? error.message : 'Internal server error'
        });
    }
};

module.exports = {
    getDayEvents,
    getEventDetails,
    getYearEvents,
    searchEvents,
    searchPeople,
    getPeopleByEra
};