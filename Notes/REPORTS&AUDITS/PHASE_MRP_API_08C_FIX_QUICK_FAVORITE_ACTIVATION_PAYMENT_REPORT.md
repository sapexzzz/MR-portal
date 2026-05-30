# PHASE_MRP_API_08C_FIX_QUICK_FAVORITE_ACTIVATION_PAYMENT - FIX_QUICK_FAVORITE_ACTIVATION_PAYMENT

## Status
STATUS: PASS_BUILD_ONLY_MANUAL_VALIDATION_PENDING

## Runtime bug report from user
- Quick favorite regular staff activation does not consume ender pearls.
- Payment must not happen at keypress.
- Payment must happen at delayed activation.

## Root cause
`TeleportRequestService.queueQuickFavoriteRequest(...)` queued scroll mode based on whether a scroll existed in the preferred-item result, instead of whether scroll was the selected activator after applying old preference order.

For players with a regular Portal Staff preferred over a Teleport Scroll, the queue could still store `useScroll=true` if a scroll was present. Delayed activation then followed the scroll payment branch, so regular staff pearl payment was skipped.

## Files inspected
- `src/main/java/com/mentality/mrportal/portal/TeleportRequestService.java`
- `src/main/java/com/mentality/mrportal/portal/PendingTeleportManager.java`
- `src/main/java/com/mentality/mrportal/portal/TeleportPaymentPlan.java`
- `src/main/java/com/mentality/mrportal/portal/TeleportActivationInfo.java`
- `src/main/java/com/mentality/mrportal/api/MRPortalTeleportContext.java`
- `src/main/java/com/mentality/mrportal/portal/DefaultPortalTeleportHandler.java`
- `src/main/java/com/mentality/mrportal/network/MRPortalNetworking.java`

## Files changed
- `src/main/java/com/mentality/mrportal/portal/TeleportRequestService.java`
- `Notes/REPORTS&AUDITS/PHASE_MRP_API_08C_FIX_QUICK_FAVORITE_ACTIVATION_PAYMENT_REPORT.md`
- `Notes/OBSIDIAN_BOARD/PHASES/PHASE_MRP_API_08C_FIX_QUICK_FAVORITE_ACTIVATION_PAYMENT.md`
- `Notes/OBSIDIAN_BOARD/API_DESIGN.md`
- `Notes/OBSIDIAN_BOARD/mrportalboard.canvas`

## Fix summary
- Keypress still performs no payment.
- Queue now stores scroll mode only when scroll is actually selected after preference order:
  - not infinite/creative
  - not regular staff
  - scroll selected
- Regular staff quick favorite activation now reaches the regular staff payment plan.
- Cooldown behavior is preserved at delayed activation.
- Scroll quick favorite remains activation-time scroll consumption.
- GUI route was not changed.

## Payment timing
- Keypress: favorite lookup, active/pending check, preferred activator selection, same-dimension check, queue/preview only.
- Delayed activation: revalidate item/session/dimension, call handler `canStartTeleport`, commit payment/cooldown, then call handler `startTeleport`.
- Failure behavior: not enough pearls/cooldown/item failures abort without partial payment or portal start.

## Validation
- `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew compileJava`: PASS
- `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew compileClientJava`: PASS
- `[MRP07F]` grep: no matches
- `queueQuickFavoriteRequest` grep: networking and service present
- `activateQueuedQuickFavorite` grep: manager and service present
- `activatePendingFavoriteTeleport` grep: no matches
- `handleGuiTeleportRequest` grep: GUI service route present
- payment grep: service commit path contains scroll shrink, pearl consume, and cooldown application
- `mr_portal_chrono` grep: no matches
- `chrono_portal` grep: no matches
- `FabricLoader.*isModLoaded` grep: no matches
- handler payment-free check: no matches in `DefaultPortalTeleportHandler`

Manual validation was not run by the agent.

Pending manual checks:
- Quick favorite regular staff: keypress no payment, activation consumes pearls once and applies cooldown.
- Quick favorite not enough pearls: no portal, no cooldown, no partial consumption.
- Quick favorite cooldown block: immediate repeat blocked, no extra pearls.
- Quick favorite scroll: scroll consumed at activation only.
- GUI regression smoke: GUI staff and scroll still work.

## Compatibility notes
- GUI unchanged.
- Handler remains payment-free.
- No Chrono checks.
- No addon dependency.
- No `[MRP07F]`.
- No persistent cooldown added.

## Next recommended phase
PHASE_MRP_API_08D_QUICK_FAVORITE_PAYMENT_MANUAL_VALIDATION
