const Search = require('../models/userSearch');

exports.saveSearch = async (req, res) => {
    try {
        const { userId, query } = req.body;
        if (!userId || !query) {
            return res.status(400).json({ message: 'User ID and search query are required' });
        }

        const search = new Search({ userId, query });
        await search.save();
        res.status(201).json({ message: 'Search saved successfully!', search });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
};

exports.getUserSearchHistory = async (req, res) => {
    try {
        const { userId } = req.params;
        const searches = await Search.find({ userId }).sort({ timestamp: -1 });

        res.status(200).json(searches);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
};
