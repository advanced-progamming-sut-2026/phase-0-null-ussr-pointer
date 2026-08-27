package com.ussr.pvz.shared.network;

public enum RequestType {
    PING,

    LOGIN,
    REGISTER,
    COMPLETE_REGISTRATION,

    FORGOT_PASSWORD,
    ANSWER_SECURITY_QUESTION,
    RESET_PASSWORD,

    AUTH_TOKEN,
    LOGOUT,

    GET_PROFILE,
    CHANGE_USERNAME,
    CHANGE_NICKNAME,
    CHANGE_EMAIL,
    CHANGE_PASSWORD,
    SYNC_ACCOUNT,

    GET_LEADERBOARD,

    FIND_RANDOM_MATCH,
    CANCEL_RANDOM_MATCH,
    GET_ONLINE_PLAYERS,      // client polls for the online-player list
    SEND_INVITE,             // client → server: invite a specific username
    CANCEL_INVITE,           // client → server: withdraw a sent invite
    RESPOND_INVITE,          // client → server: accept or reject an incoming invite
    CHECK_INVITE,            // client polls for any pending invite aimed at this client
    JOIN_RANDOM_QUEUE,       // client → server: join the matchmaking queue
    LEAVE_RANDOM_QUEUE,      // client → server: leave the matchmaking queue
    CHECK_RANDOM_MATCH,
    CHECK_INVITE_RESULT,
    CHALLENGE_PLAYER,
    ACCEPT_CHALLENGE,
    REJECT_CHALLENGE,

    GAME_ACTION,
    GAME_STATE,

    SEND_REACTION,
}