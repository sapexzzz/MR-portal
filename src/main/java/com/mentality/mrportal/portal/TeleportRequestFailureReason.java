package com.mentality.mrportal.portal;

/**
 * Internal failure categories for future teleport request orchestration.
 */
public enum TeleportRequestFailureReason {
	NONE,
	NOT_IMPLEMENTED,
	MISSING_WAYPOINT,
	ACTIVE_SESSION,
	PENDING_FAVORITE,
	SAME_DIMENSION_ONLY,
	MISSING_TELEPORT_ITEM,
	COOLDOWN_ACTIVE,
	NOT_ENOUGH_PEARLS,
	HANDLER_REJECTED,
	HANDLER_FAILED,
	INVALID_PLAYER,
	MISSING_DESTINATION_LEVEL
}
