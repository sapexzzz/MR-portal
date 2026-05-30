# MR-Portal API Design

Placeholder for the future MR-Portal public API design.

This file belongs in `MR-portal/Notes/OBSIDIAN_BOARD/` because the API is implemented inside the base MR-Portal jar.

Current intended direction:

- The API will live inside `mr-portal.jar`.
- Addons will register teleport handlers through `MRPortalApi`.
- The base mod must not check specific addon mod ids.
- The base mod must not depend on addon jars.
- The base mod keeps ownership of waypoint, item, cost, and cooldown rules.
- Addons control teleport execution style and visuals.
- Base MR-Portal API notes live under `MR-portal/Notes/`.
- Chrono addon design, addon architecture, and addon reports live under `Add-ons/Chrono/Notes/`.
- From the `MR-portal/` root, Chrono notes are at `../Add-ons/Chrono/Notes/`.
- Do not store addon design/report files in `MR-portal/Notes/`.
- Root-level `Notes/` is not used.
- No API is implemented in `PHASE_MRP_WORKSPACE_02_FIX_NOTES_LAYOUT`.

Audit status:

- `PHASE_MRP_API_01_AUDIT_TELEPORT_FLOW` mapped the current teleport flow without Java source changes.
- Current payment/cooldown ownership is in base MR-Portal, mainly `PendingTeleportManager`.
- Current default execution is `PortalSession` based and includes owner teleport, other entity teleport, portal visuals, preview sparks, and close lifecycle.
- The next implementation phase should add API contracts only: `PHASE_MRP_API_02_PUBLIC_HANDLER_API`.

Public API contracts added in `PHASE_MRP_API_02_PUBLIC_HANDLER_API`:

- Package path: `src/main/java/com/mentality/mrportal/api/`
- `MRPortalApi`: public registry entry point, API version, handler registration, active handler selection, handler listing.
- `MRPortalTeleportHandler`: addon teleport execution contract.
- `MRPortalTeleportContext`: base-validated request context for future routing phases.
- `MRPortalWaypointView`: read-only public waypoint view.
- `MRPortalTeleportActivator`: activation path enum for regular staff, infinite staff, teleport scroll, and creative.

Handler registry rules:

- Higher priority wins.
- Duplicate handler ids are replaced by the latest registration and logged as a warning.
- Same-priority ties are deterministic by handler id string, with a warning.
- If no handler is registered, `MRPortalApi.getActiveTeleportHandler()` returns `null` until the later default-handler phase.

Ownership rules:

- Base MR-Portal keeps waypoint lookup, item validation, same-dimension rules, pearl payment, scroll consumption, staff cooldown, creative/infinite bypass, and quick favorite lookup/delay.
- Addon handlers control execution style and visuals after base validation/payment in later phases.
- Gameplay routing is not implemented yet; `TELEPORT_REQUEST_C2S` and `QUICK_TELEPORT_BY_KEYBIND_C2S` still use the existing flow.

Default handler registration added in `PHASE_MRP_API_03_DEFAULT_HANDLER_REGISTRATION`:

- Default handler class: `src/main/java/com/mentality/mrportal/portal/DefaultPortalTeleportHandler.java`
- Handler id: `mr_portal:default_portal`
- Handler priority: `0`
- Registration point: `MRPortal.onInitialize()`, after config load and item registration, before server networking and tick registration.
- `usesDefaultPreviewSpark()` returns `true`.
- `startTeleport(context)` is intentionally non-executing in this phase and returns `false`; gameplay routing is not implemented yet.
- Next required design: a request service that keeps payment/cooldown ownership in base MR-Portal while routing execution to the selected handler.

Teleport request service design from `PHASE_MRP_API_04_TELEPORT_REQUEST_SERVICE_DESIGN`:

- Future internal class: `src/main/java/com/mentality/mrportal/portal/TeleportRequestService.java`
- The service owns server-side request orchestration: waypoint/favorite lookup, activator classification, same-dimension validation, active session checks, payment/cooldown planning, context creation, handler selection, handler acceptance, and success/failure results.
- Base MR-Portal continues to own pearl payment, scroll consumption, cooldown, item detection, quick favorite delay timing, and same-dimension rules.
- `PendingTeleportManager` should remain the default portal session executor: session storage, portal tick lifecycle, portal create/close packets, owner/entity teleport, and geometry calculations.
- Default handler execution should become safe by adding a paid-session start path in `PendingTeleportManager` that does not perform payment, item lookup, cooldown, or scroll consumption.
- Prefer an internal `WaypointData` adapter/bridge for the default handler rather than exposing mutable internal waypoint state through the public API.
- Normal GUI routing should precheck payment/cooldown, call `handler.canStartTeleport(context)`, commit payment/cooldown immediately before accepted synchronous execution, call `handler.startTeleport(context)`, then remove preview and close the screen only on success.
- Quick favorite routing must preserve current behavior: queue and preview at keypress, but revalidate item/dimension/payment/cooldown and consume resources only at delayed activation.
- `usesDefaultPreviewSpark()` should control default spark creation/removal for GUI and quick favorite paths, and the code must track whether a default spark was emitted before removing it.
- Recommended next phases: `PHASE_MRP_API_05_REQUEST_SERVICE_SKELETON`, `PHASE_MRP_API_06_EXTRACT_PAID_SESSION_START`, `PHASE_MRP_API_07_ROUTE_GUI_TELEPORT_THROUGH_SERVICE`, `PHASE_MRP_API_08_ROUTE_QUICK_FAVORITE_THROUGH_SERVICE`, `PHASE_MRP_API_09_PREVIEW_SPARK_HANDLER_HOOK`, `PHASE_MRP_API_10_NO_ADDON_REGRESSION_VALIDATION`.

Request service skeleton added in `PHASE_MRP_API_05_REQUEST_SERVICE_SKELETON`:

- `TeleportRequestService`: public internal service class for future routing; currently has unrouted stubs for GUI teleport and quick favorite requests.
- `TeleportRequestResult`: public result record using `successful`, `failureReason`, and `messageSent`.
- `TeleportRequestFailureReason`: public enum covering `NONE`, `NOT_IMPLEMENTED`, waypoint/session/item/payment/handler failures, invalid player, and missing destination level.
- `TeleportPaymentPlan`: package-private internal record for base-owned payment/cooldown planning. It stores mutable `ItemStack` references only internally and does not consume resources.
- `TeleportActivationInfo`: package-private internal record for classified request state before public context creation.
- `WaypointDataView`: package-private read-only adapter from `WaypointData` to `MRPortalWaypointView`.
- The service is not wired yet. `MRPortalNetworking` still routes `TELEPORT_REQUEST_C2S` and `QUICK_TELEPORT_BY_KEYBIND_C2S` through the existing direct flow.
- Next extraction target: `PHASE_MRP_API_06_EXTRACT_PAID_SESSION_START`, adding a session-start path that assumes validation and payment already happened.

Paid session start extracted in `PHASE_MRP_API_06_EXTRACT_PAID_SESSION_START`:

- `PendingTeleportManager.startPaidTeleportSession(ServerPlayer, WaypointData, boolean)` starts the default portal session after future validation/payment.
- `PendingTeleportManager.startPaidTeleportSession(ServerPlayer, WaypointData, boolean, Vec3, float)` supports preserved source portal position/yaw for delayed quick favorite activation.
- Both methods are session-only wrappers around the existing private `startSession(...)` path.
- The paid-session methods do not perform item lookup, pearl consumption, scroll consumption, cooldown checks, cooldown application, or same-dimension payment rules.
- Existing `beginTeleport`, `beginScrollTeleport`, and quick favorite activation still perform their current payment/cooldown/scroll behavior before calling the session-only path.
- `DefaultPortalTeleportHandler.startTeleport(context)` is now executable for internal `WaypointDataView` contexts and calls `startPaidTeleportSession`.
- `WaypointDataView` exposes package-private `waypointData()` for base internals only. No public `internalWaypoint()` was added to `MRPortalTeleportContext`.
- The default handler does not consume pearls, consume scrolls, apply cooldown, or inspect teleport items.
- Next routing phase: `PHASE_MRP_API_07_ROUTE_GUI_TELEPORT_THROUGH_SERVICE`.

GUI routing implemented in `PHASE_MRP_API_07_ROUTE_GUI_TELEPORT_THROUGH_SERVICE`:

- `MRPortalNetworking` now routes only `TELEPORT_REQUEST_C2S` through `TeleportRequestService.handleGuiTeleportRequest(...)`.
- `QUICK_TELEPORT_BY_KEYBIND_C2S` remains on the old direct favorite flow.
- `TeleportRequestService` now owns GUI waypoint lookup, activator classification, same-dimension validation, active-session rejection, payment precheck, context creation, handler selection, payment commit, and handler start.
- Regular staff GUI path consumes pearls once and applies cooldown once immediately before handler execution.
- Scroll GUI path shrinks one scroll once immediately before handler execution.
- Infinite/creative GUI path bypasses pearl cost, scroll consumption, and cooldown.
- `DefaultPortalTeleportHandler` remains payment-free and only starts the already-paid default session.
- `PendingTeleportManager` exposes same-package helper methods for request service reuse while preserving the existing old quick favorite flow.
- Manual validation is pending; next recommended phase is `PHASE_MRP_API_07B_GUI_TELEPORT_MANUAL_VALIDATION`.

GUI staff payment regression fixed in `PHASE_MRP_API_07C_FIX_GUI_TELEPORT_PAYMENT_REGRESSION`:

- Runtime validation found regular Portal Staff GUI teleports could start sessions without consuming pearls or applying cooldown.
- Root cause: `TeleportRequestService` used the broad GUI `creativeView` flag as the activation classifier. That flag represents waypoint visibility for creative/infinite access, not necessarily the paid activator for the current request.
- Fix: GUI staff classification now uses the actual staff stack found by `PendingTeleportManager.findPortalStaff(player)`.
- Creative bypass is limited to `player.getAbilities().instabuild`; infinite bypass is limited to an actual infinite staff stack.
- Regular staff payment plans keep the same staff stack through precheck and commit, so pearl payment and cooldown target the same item.
- `DefaultPortalTeleportHandler` remains payment-free and now treats sessions as infinite only for `INFINITE_STAFF` or `CREATIVE` activators.
- Quick favorite remains unchanged and unrouted through `TeleportRequestService`.
- Manual gameplay validation of the fix is still pending; next recommended phase is `PHASE_MRP_API_07D_GUI_TELEPORT_PAYMENT_FIX_MANUAL_VALIDATION`.

GUI payment resource sync fixed in `PHASE_MRP_API_07D_FIX_GUI_PAYMENT_COMMIT_RESOURCE_CONSUMPTION`:

- Runtime validation after 07C showed staff cooldown applying while ender pearls and scroll stacks did not visibly decrease after GUI teleport.
- Audit confirmed the service uses live `ItemStack` references and no payment-stack `copy()` path exists.
- `TeleportRequestService.commitPayment(...)` now synchronizes inventory/menu state after successful scroll shrink and after successful pearl consumption.
- Payment remains base-owned and still happens before `handler.startTeleport(context)`.
- `DefaultPortalTeleportHandler` remains payment-free.
- Quick favorite remains unchanged and is still not routed through `TeleportRequestService`.
- Manual gameplay validation is pending; next recommended phase is `PHASE_MRP_API_07E_GUI_PAYMENT_COMMIT_MANUAL_VALIDATION`.

GUI staff cooldown order fixed in `PHASE_MRP_API_07E_FIX_GUI_STAFF_COOLDOWN_AFTER_PAYMENT`:

- Runtime validation after 07D showed staff pearls and scroll resources were fixed, but regular staff cooldown did not appear or persist.
- Audit found cooldown code and nonzero cooldown config were present, but the GUI service transaction order differed from the old manager path after the 07D inventory sync change.
- Regular staff commit order now matches the old path more closely: consume pearls, apply cooldown, synchronize inventory/menu state, then start the handler.
- Cooldown target remains the actual regular staff item from `paymentPlan.staffStack().getItem()`.
- Scroll remains unchanged and still has no cooldown.
- Quick favorite remains unchanged and unrouted through `TeleportRequestService`.
- Manual gameplay validation is pending; next recommended phase is `PHASE_MRP_API_07F_GUI_STAFF_COOLDOWN_MANUAL_VALIDATION`.

GUI payment runtime diagnostics added in `PHASE_MRP_API_07F_DIAGNOSE_GUI_PAYMENT_COMMIT_RUNTIME_PATH`:

- Runtime after 07E still reports GUI staff and scroll sessions start without visible resource/cooldown effects.
- No further blind payment fix was applied.
- Temporary `[MRP07F]` logs were added to `MRPortalNetworking` and `TeleportRequestService`.
- Logs cover GUI packet entry, service entry, waypoint lookup, activator classification, payment plan, handler acceptance, commit branch, pearl counts before/after consume, cooldown state before/after `addCooldown`, scroll count before/after `shrink(1)`, inventory sync, and handler start result.
- Quick favorite remains unchanged and is not routed through the service.
- Next step is runtime capture of `[MRP07F]` logs for one regular staff GUI teleport and one scroll GUI teleport.

GUI scroll consumption mode tracking fixed in `PHASE_MRP_API_07G_FIX_SCROLL_CONSUMPTION_AND_AUDIT_COOLDOWN_REJOIN_PARITY`:

- GUI Teleport Scroll consumption root cause was server/client mode ownership, not the scroll shrink helper itself.
- The server now records whether the open waypoint screen is a scroll screen or a staff/infinite screen.
- `TeleportScrollItem` opens the screen with scroll mode enabled; `PortalStaffItem` and staff/infinite keybind opens use scroll mode disabled.
- Waypoint screen refreshes preserve the active mode, and successful GUI teleport or screen close clears it.
- `TELEPORT_REQUEST_C2S` still routes only GUI teleport through `TeleportRequestService`, but now passes the server-owned scroll mode into the service.
- Quick favorite remains unchanged and continues to use the old `queueFavoriteTeleport` path.
- Portal Staff cooldown disappearing after logout/rejoin is classified as expected legacy behavior because backup code used vanilla `ItemCooldowns` only and had no persistent cooldown restore path.
- Temporary `[MRP07F]` diagnostics remain in source and should be removed after runtime validation confirms the scroll fix.

Validation follow-up in `PHASE_MRP_API_07H_VALIDATE_SCROLL_FIX_AND_DIAGNOSTIC_CLEANUP`:

- Static validation passed for the 07G scroll mode fix.
- `compileJava` and `compileClientJava` pass with Java 21.
- Quick favorite remains on the old `queueFavoriteTeleport` path.
- No Chrono checks or addon dependencies were added.
- Manual runtime validation was not completed in this pass, so `[MRP07F]` diagnostics remain in source.
- Next step is manual validation continuation, then diagnostic cleanup if scroll and staff critical paths pass.

Manual pass recorded and diagnostics removed in `PHASE_MRP_API_07I_RECORD_MANUAL_PASS_AND_REMOVE_DIAGNOSTICS`:

- User confirmed the GUI runtime paths are OK after the 07G scroll mode fix.
- Teleport Scroll GUI consumption, regular staff GUI behavior, and staff pearl/cooldown behavior are recorded as passing.
- Quick favorite remains unchanged and still uses the old direct manager path.
- Portal Staff cooldown disappearing after rejoin remains `EXPECTED_LEGACY_BEHAVIOR`.
- Temporary `[MRP07F]` diagnostics were removed from `MRPortalNetworking` and `TeleportRequestService`.
- Next recommended phase is `PHASE_MRP_API_08_ROUTE_QUICK_FAVORITE_THROUGH_SERVICE`.

Quick favorite routing implemented in `PHASE_MRP_API_08_ROUTE_QUICK_FAVORITE_THROUGH_SERVICE`:

- `QUICK_TELEPORT_BY_KEYBIND_C2S` now calls `TeleportRequestService.queueQuickFavoriteRequest(...)`.
- The queue stage performs favorite lookup, active-session/pending-session checks, preferred activator selection, and same-dimension validation.
- Queue stage preserves old behavior by not consuming pearls, not consuming scrolls, and not applying cooldown.
- `PendingTeleportManager` continues to own pending quick favorite delay storage and preview timing.
- Delayed activation now calls `TeleportRequestService.activateQueuedQuickFavorite(...)`.
- Activation-time service logic revalidates item/cooldown/resources, commits payment, builds `MRPortalTeleportContext`, and calls the active handler.
- `MRPortalTeleportContext` now carries source position/yaw so the default handler can preserve the queued quick favorite source portal origin.
- `DefaultPortalTeleportHandler` remains payment-free and starts paid sessions with the context source position/yaw.
- GUI teleport routing remains unchanged.
- Manual validation is pending in `PHASE_MRP_API_08B_QUICK_FAVORITE_MANUAL_VALIDATION`.

Quick favorite manual validation status from `PHASE_MRP_API_08B_QUICK_FAVORITE_MANUAL_VALIDATION`:

- Static validation passed: compile targets pass, service routing is present, no `[MRP07F]`, no Chrono checks, and the default handler remains payment-free.
- Manual runtime validation was not run in this environment.
- Required runtime checks remain pending for regular staff, not-enough-pearls, cooldown block, scroll, infinite/creative, same-dimension restriction, and GUI regression smoke.
- Next recommended phase is `PHASE_MRP_API_08B_QUICK_FAVORITE_MANUAL_VALIDATION_CONTINUATION`.

Quick favorite activation payment fix in `PHASE_MRP_API_08C_FIX_QUICK_FAVORITE_ACTIVATION_PAYMENT`:

- Runtime validation found regular staff quick favorite did not consume pearls at activation.
- Root cause was queue-stage activator mode storage: scroll mode could be queued whenever a scroll existed, even if regular staff was the preferred activator.
- The queue stage now stores scroll mode only when scroll is the actual selected activator after the old preference order.
- Keypress remains payment-free.
- Delayed activation remains responsible for pearl consumption, scroll consumption, and cooldown application.
- GUI routing remains unchanged.
- Manual validation of the quick favorite payment fix is pending.

Quick favorite manual pass recorded in `PHASE_MRP_API_08D_RECORD_QUICK_FAVORITE_MANUAL_PASS`:

- User confirmed "да всё ок" after the 08C quick favorite activation payment fix.
- Quick favorite regular staff, activation-time pearl payment, keypress payment-free behavior, quick favorite scroll, and GUI smoke are recorded as manually passing.
- Static validation confirms the quick favorite route remains service-routed, `activateQueuedQuickFavorite` remains present, old `activatePendingFavoriteTeleport` remains absent, GUI routing remains present, no `[MRP07F]` diagnostics remain, no Chrono checks were added, and `DefaultPortalTeleportHandler` remains payment-free.
- Preview spark handler opt-out remains future work.
- Next recommended phase is `PHASE_MRP_API_09_PREVIEW_SPARK_HANDLER_HOOK`.

Preview spark handler hook implemented in `PHASE_MRP_API_09_PREVIEW_SPARK_HANDLER_HOOK`:

- Base preview spark creation now consults the active `MRPortalTeleportHandler.usesDefaultPreviewSpark()` hook.
- Missing active handler falls back to `true` to preserve base behavior.
- GUI open sends a trailing `useDefaultPreviewSpark` boolean in `OPEN_SCREEN_S2C`, and the client `WaypointScreen` only shows/hides local default preview when enabled.
- Server-side GUI preview broadcast is skipped when the active handler opts out, and removal remains gated by recorded preview state.
- Quick favorite pending activations now store whether the default quick preview was emitted, and removal on abort/activation is gated by that flag.
- `DefaultPortalTeleportHandler` remains explicitly opted in.
- GUI and quick favorite payment/routing behavior was not changed.
- Manual validation is pending; next recommended phase is `PHASE_MRP_API_09B_PREVIEW_SPARK_HOOK_MANUAL_VALIDATION`.

Preview hook manual validation status from `PHASE_MRP_API_09B_PREVIEW_SPARK_HOOK_MANUAL_VALIDATION`:

- Static validation passed: compile targets pass, preview hook greps are present, GUI and quick favorite routes remain service-routed, no `[MRP07F]` diagnostics remain, no Chrono checks were added, and `DefaultPortalTeleportHandler` remains payment-free.
- Runtime validation was not completed in this phase.
- Required runtime checks remain pending for GUI staff open/close, GUI staff teleport, GUI scroll teleport, quick favorite staff preview, quick favorite scroll preview, screen refresh/edit preview stability, and log review.
- Next recommended phase is `PHASE_MRP_API_09B_PREVIEW_SPARK_HOOK_MANUAL_VALIDATION_CONTINUATION`.

Preview hook manual pass recorded in `PHASE_MRP_API_09C_RECORD_PREVIEW_SPARK_HOOK_MANUAL_PASS`:

- User confirmed "да всё ок" after runtime testing.
- No crash was reported.
- GUI opens successfully.
- Preview spark / target point appears during waypoint selection.
- Preview cleanup is accepted as OK, and no stuck preview was reported.
- GUI and quick favorite behavior were not reported broken.
- Static validation confirms `usesDefaultPreviewSpark`, `OPEN_SCREEN_S2C`, `defaultPreviewCreated`, `defaultPreviewShown`, GUI service routing, and quick favorite service routing remain present.
- No `[MRP07F]` diagnostics, Chrono hardcoded checks, or addon dependencies were added.
- Future Chrono work moves to `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/Add-ons/Chrono/`, with the base MR-Portal reference at `../../MR-portal/`.
- Next recommended phase is `PHASE_MRP_CHRONO_01_ADDON_SCAFFOLD_AUDIT_OR_CREATE`.
