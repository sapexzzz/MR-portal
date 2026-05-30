# PHASE_MRP_API_04_TELEPORT_REQUEST_SERVICE_DESIGN

## Goal

Design the internal `TeleportRequestService` before changing teleport routing.

## Service Ownership Decision

Future `TeleportRequestService` should own server-side request orchestration: waypoint/favorite lookup, activator classification, same-dimension validation, active session checks, payment/cooldown prechecks and commits, context creation, handler selection, and success/failure results.

## Payment/Cooldown Decision

Base MR-Portal keeps payment and cooldown ownership. The service should build a payment plan, call `handler.canStartTeleport(context)` before consuming resources, then commit scroll/pearls/cooldown immediately before synchronous accepted execution. Default handler routing should be introduced first because its session start can be synchronous.

## Quick Favorite Decision

Quick favorite must preserve current timing: initial validation and preview at keypress, then full dimension/item/payment/cooldown revalidation and payment at delayed activation. The delayed activation should eventually call back into `TeleportRequestService`.

## Preview Spark Decision

`usesDefaultPreviewSpark()` should control default preview creation/removal for screen open, screen close, successful GUI start, quick favorite queue, quick favorite abort, and quick favorite activation. The service/networking boundary must track whether a default spark was emitted.

## Default Handler Plan

Add a later paid-session start method in `PendingTeleportManager` that performs only default session execution. Keep item lookup, pearl/scroll consumption, and cooldown outside that method. Prefer an internal waypoint adapter over exposing mutable `WaypointData` publicly.

## Report

`Notes/REPORTS&AUDITS/PHASE_MRP_API_04_TELEPORT_REQUEST_SERVICE_DESIGN_REPORT.md`

## Next Phase

PHASE_MRP_API_05_REQUEST_SERVICE_SKELETON
