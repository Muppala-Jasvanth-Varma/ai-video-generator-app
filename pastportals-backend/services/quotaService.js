const UserRequest = require('../models/userRequestModel');

exports.checkQuota = async (userId) => {
  try {
    let userRequest = await UserRequest.findOne({ userId });
    
    // Reset monthly quota
    if (userRequest && new Date() - userRequest.lastReset > 30*24*60*60*1000) {
      userRequest.count = 0;
      userRequest.lastReset = new Date();
      await userRequest.save();
    }

    if (!userRequest) {
      userRequest = await UserRequest.create({ userId });
    }

    if (userRequest.count >= 1000) {
      throw new Error('Monthly quota exceeded');
    }

    return userRequest;
  } catch (error) {
    throw new Error(`Quota Service Error: ${error.message}`);
  }
};

exports.updateQuota = async (userId) => {
  await UserRequest.updateOne(
    { userId },
    { $inc: { count: 1 } }
  );
};