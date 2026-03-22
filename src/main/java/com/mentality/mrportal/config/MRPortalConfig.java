package com.mentality.mrportal.config;

public class MRPortalConfig {
	public int cooldownSeconds = 60;
	public int maxWaypoints = 10;
	public int portalOpenDelay = 40;
	public int blindnessDuration = 40;

	public int portalLifetimeTicks() {
		return this.portalOpenDelay + 200;
	}
}