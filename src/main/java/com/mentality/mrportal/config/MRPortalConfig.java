package com.mentality.mrportal.config;

public class MRPortalConfig {
	public String language = "auto";
	public int cooldownSeconds = 60;
	public int maxWaypoints = 10;
	public int portalOpenDelay = 60;
	public int portalActiveTicks = 60;
	public int portalCloseDelayTicks = 60;
	public int portalPearlCost = 3;
	public boolean applyBlindnessOnTeleport = true;
	public int blindnessDuration = 40;
	public double portalScale = 1.2D;
	public double portalSpawnDistance = 2.0D;

	public void sanitize() {
		if (this.language == null || (!this.language.equals("auto") && !this.language.equals("en") && !this.language.equals("ru"))) {
			this.language = "auto";
		}
		this.cooldownSeconds = Math.max(0, this.cooldownSeconds);  
		this.maxWaypoints = Math.max(1, this.maxWaypoints);
		this.portalOpenDelay = Math.max(1, this.portalOpenDelay);
		this.portalActiveTicks = Math.max(1, this.portalActiveTicks);
		this.portalCloseDelayTicks = Math.max(1, this.portalCloseDelayTicks);
		this.portalPearlCost = Math.max(0, this.portalPearlCost);
		this.blindnessDuration = Math.max(0, this.blindnessDuration);
		this.portalScale = Math.max(0.5D, this.portalScale);
		this.portalSpawnDistance = Math.max(0.0D, this.portalSpawnDistance);
	}

	public int portalLifetimeTicks() {
		return this.portalOpenDelay + this.portalActiveTicks + this.portalCloseDelayTicks;
	}
}