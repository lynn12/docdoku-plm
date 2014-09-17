/*global define*/
define(function () {
    return {
        WEBRTC_INVITE: 'WEBRTC_INVITE',
        WEBRTC_ACCEPT: 'WEBRTC_ACCEPT',
        WEBRTC_REJECT: 'WEBRTC_REJECT',
        WEBRTC_HANGUP: 'WEBRTC_HANGUP',
        WEBRTC_ROOM_JOIN_EVENT: 'WEBRTC_ROOM_JOIN_EVENT',
        WEBRTC_ROOM_REJECT_EVENT: 'WEBRTC_ROOM_REJECT_EVENT',
        WEBRTC_OFFER: 'offer',
        WEBRTC_ANSWER: 'answer',
        WEBRTC_CANDIDATE: 'candidate',
        WEBRTC_BYE: 'bye',
        COLLABORATIVE_CREATE: 'COLLABORATIVE_CREATE',
        COLLABORATIVE_INVITE: 'COLLABORATIVE_INVITE',
        COLLABORATIVE_JOIN: 'COLLABORATIVE_JOIN',
        COLLABORATIVE_CONTEXT: 'COLLABORATIVE_CONTEXT',
        COLLABORATIVE_COMMANDS: 'COLLABORATIVE_COMMANDS',
        COLLABORATIVE_EXIT: 'COLLABORATIVE_EXIT',
        COLLABORATIVE_KILL: 'COLLABORATIVE_KILL',
        COLLABORATIVE_GIVE_HAND: 'COLLABORATIVE_GIVE_HAND',
        COLLABORATIVE_KICK_USER: 'COLLABORATIVE_KICK_USER',
        COLLABORATIVE_KICK_NOT_INVITED: 'COLLABORATIVE_KICK_NOT_INVITED',
        COLLABORATIVE_WITHDRAW_INVITATION: 'COLLABORATIVE_WITHDRAW_INVITATION',
        CHAT_MESSAGE: 'CHAT_MESSAGE',
        CHAT_MESSAGE_ACK: 'CHAT_MESSAGE_ACK',
        USER_STATUS: 'USER_STATUS'
    };
})