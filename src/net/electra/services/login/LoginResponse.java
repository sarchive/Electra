package net.electra.services.login;

public enum LoginResponse
{
    LOGIN (0),
    WAIT_TWO_SECONDS (1),
    FINALIZE_LOGIN (2),
    WRONG_PASSWORD (3),
    BANNED (4),
    ALREADY_LOGGED_IN (5),
    CLIENT_UPDATED (6),
    WORLD_FULL (7),
    LOGIN_SERVER_OFFLINE (8),
    LOGIN_LIMIT_EXCEEDED (9),
    BAD_SESSION_ID (10),
    REJECTED_SESSION (11),
    MEMBERS_ONLY (12),
    INCOMPLETE_LOGIN (13),
    UPDATE_IN_PROGRESS (14),
    PICKED_UP_SESSION (15),
    LOGIN_ATTEMPTS_EXCEEDED (16),
    STANDING_IN_MEMBERS (17),
    CONNECTION_BLOCKED (18),
    WORLD_SERVER_OFFLINE (19),
    INVALID_LOGIN_SERVER (20),
    JUST_LEFT_WORLD (21);
    
    private final byte responseCode;
    
    private LoginResponse(int responseCode)
    {
    	this.responseCode = (byte)responseCode;
    }
    
    public byte code()
    {
    	return responseCode;
    }
}
