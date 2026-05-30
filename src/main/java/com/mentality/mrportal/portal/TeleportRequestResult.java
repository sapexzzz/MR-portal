package com.mentality.mrportal.portal;

import java.util.Objects;

/**
 * Internal result object for future teleport request service calls.
 */
public record TeleportRequestResult(
	boolean successful,
	TeleportRequestFailureReason failureReason,
	boolean messageSent
) {
	public TeleportRequestResult {
		Objects.requireNonNull(failureReason, "failureReason");
		if (successful && failureReason != TeleportRequestFailureReason.NONE) {
			throw new IllegalArgumentException("successful teleport results must use NONE");
		}
		if (!successful && failureReason == TeleportRequestFailureReason.NONE) {
			throw new IllegalArgumentException("failed teleport results must include a failure reason");
		}
	}

	public static TeleportRequestResult success() {
		return new TeleportRequestResult(true, TeleportRequestFailureReason.NONE, false);
	}

	public static TeleportRequestResult failed(TeleportRequestFailureReason reason) {
		return failed(reason, false);
	}

	public static TeleportRequestResult failed(TeleportRequestFailureReason reason, boolean messageSent) {
		return new TeleportRequestResult(false, reason, messageSent);
	}
}
