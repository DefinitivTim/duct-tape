> Renamed on 2026-11-22: formerly `farbound_fixes` (1.3.9). New mod ID `definitivtim_duct_tape`, new package, own icon, version 1.4.0. Behaviour is unchanged: the jar was produced by renaming the compiled 1.3.9 jar, not by a new compile.
> Config files are now named after the new mod ID. Remove the old jar when installing this one.

# DefinitivTim's Duct Tape

Small workarounds for third party mods. Successor of `farbound_skyfix` (1.1.0), which is
now part of this mod. Mod id: `definitivtim_duct_tape`. Version 1.3.6.

## 1. Copycats+ material guard (new in 1.2.0)

Tested against Create: Copycats+ `3.0.9+mc.1.21.1-neoforge`.

### Problem
`IMultiStateCopycatBlockEntity.getMaterial()` calls `getMaterialItem(defaultProperty).material()` without a null
check. Crash seen when breaking several copycat blocks quickly with a wrench:

    java.lang.NullPointerException: Cannot invoke "MaterialItemStorage$MaterialItem.material()" because the
    return value of "MaterialItemStorage.getMaterialItem(String)" is null
      at IMultiStateCopycatBlockEntity.getMaterial(IMultiStateCopycatBlockEntity.java:67)
      at ICopycatBlock.getMaterial(ICopycatBlock.java:345)
      at ICopycatBlock.hidesNeighborFace(ICopycatBlock.java:626)
      at CopycatSlabBlock.hidesNeighborFace(CopycatSlabBlock.java:273)
      at Block.shouldRenderFace
      at Continuity CtmBakedModel.process
      at CopycatModelNeoForge.gatherModelData
      at Sodium BlockRenderer.renderModel

It happens on Sodium's chunk meshing thread, intermittently. The cause is probably a short moment where the block
entity storage has no entry for the property of the block that is currently at that position. This is a guess,
the crash reports do not prove it.

### What this mod does
Two mixins add a null-safe `getMaterial()` to the two block entity classes that use the unsafe default
(`MultiStateCopycatBlockEntity` and `CopycatCogWheelBlockEntity`). A missing entry returns air, exactly like
the existing `IMultiStateCopycatBlock.getMaterial(BlockGetter, BlockPos, String)` of Copycats+ does.
The first 5 cases are written to the log as `[Duct Tape] Copycats+: no material entry ...`.
A present entry is returned unchanged. Nothing else in Copycats+ is changed. `getMaterialItem` itself is not
touched, because Copycats+ relies on null there in other places.

Not guarded on purpose: `getConsumedItem()` and `isCTEnabled()` have the same pattern, but no crash was seen there.

## 2. Cosmonautics sky (from Sky Fix 1.1.0, client only)

Tested against `cosmonautics-26.08.307`.

`SkyHandler.ensureStarPlasmaTexture()` rebuilds a 64x64 animated plasma texture every tick on the render thread
(4096 noise evaluations), and `renderCosmicNebula` / `renderSpaceStars` are drawn every frame at night. Both run
even when `enableCustomSky` is `false`. The mixins cancel these calls, but only if `enableCustomSky` is `false` and
the player is not in deep space. If Cosmonautics changes and the hook cannot be resolved, the mod does nothing and
logs one line.

## Install
- Put `definitivtim_duct_tape-1.3.6.jar` into the `mods` folder of client and server.
- Delete `farbound_skyfix-1.1.0.jar`. Both contain the same sky mixins, so keep only one of them.
- On the server only the Copycats+ part is active, the sky part is client only and stays idle.

## Not tested in game
The null-safe logic was checked offline (logic test with stub classes: missing entry returns air, present entry is
returned unchanged, log limit). The descriptors of all referenced Copycats+ and Minecraft members were compared with
the bytecode of Copycats+ 3.0.9. The mixins have not been applied inside Minecraft by me.
If `[mixin]` lines mention `definitivtim_duct_tape` with an error, the merge into the target class failed. The mod is set to
`required: false`, so the game still starts and only the guard is missing.

## Build
No Gradle. Compile `src` and `stubs` together with `javac --release 21`, then pack only the classes under `de/`
(not the stub packages `org`, `net`, `com`) together with `res/neoforge.mods.toml` (into `META-INF/`) and
`res/definitivtim_duct_tape.mixins.json` (jar root). `stubs` only exists so the sources compile, it is not packaged.

## 3. Copycats+ #419: unskinned copycats invisible (1.3.4, client only)
Upstream issue: https://github.com/copycats-plus/copycats/issues/419 (open, seen with Copycats+ 3.0.4 and 3.0.9).
Symptom: a copycat without a material (shown as `create:copycat_base`) is invisible after a contraption moved/placed it,
until a block update or chunk reload. Cause (from the issue, matches the 3.0.9 bytecode): the chunk is meshed before the
block entity data arrived; when it arrives Copycats+ redraws only if the material changed, and an unskinned copycat reads
back the same default material. Fix: redraw after every client data packet read, for all Copycats+ block entity types
(basic, multistate, cogwheel, pipes, sliding door, shaft). The first 3 are logged as `[Duct Tape] Copycats+ #419 fix`.
Remove this part when Copycats+ fixes #419.

## 4. Diagnosis for invisible copycat blocks (1.3.4, client only, changes nothing)
Log lines start with `[Duct Tape Diag]` (max 60 per session). A line `model data has NO material at X Y Z` means the
mesher asked for that copycat while the model data was empty, with the state of the block entity at that moment
(`no block entity yet`, or its storage). A later line `data arrived ... ms after` means the data came afterwards.
If a block is invisible and no such line exists for its position, the cause is not missing material data.

Note 1.3.4: all log lines now go through the game logger (logger name `DuctTape`). Before, they used System.out and did not reach latest.log.
Note 1.3.4: also redraws when a copycat block entity is added to a client level (chunk load, contraption placement), because in that case the data is read while the block entity has no level yet and the redraw after read is skipped. The first reads and loads are logged.

Note 1.3.5: sky diagnosis. A spark profile of 1.3.4 in the Overworld with `enableCustomSky = false` still showed
`ensureStarPlasmaTexture`, `renderSpaceStars` and `renderCosmicNebula` running in full (about 7.6 % of the render thread),
so the skip did not take effect. 1.3.5 changes no behaviour, it only logs (`[Duct Tape Diag] Sky: ...`, max 12 lines per session):
whether the hook into Cosmonautics was resolved, and the first decision of each handler with the values it read
(`enableCustomSky`, `deepSpace`, plasma texture state) or the exception it swallowed. If no such line appears while the sky
still runs, the mixin was not applied to `SkyHandler`.

Note 1.3.6: a spark profile of 1.3.5 (Overworld, `enableCustomSky = false`) still showed the sky work, now 14.6 % of the render
thread, and not one `[Duct Tape Diag] Sky` line in latest.log. The handlers call `SkyFixState`, which logs on its first call, so
the mixin handlers never ran: the mixin is not applied to `SkyHandler`. 1.3.6 adds `SkyProbe`, which 90 seconds after start
(client only) logs whether Mixin knows the `definitivtim_duct_tape` config, whether it lists `SkyHandler` as a target, and whether the
`SkyHandler` class contains the merged `dt$` methods. Behaviour is unchanged.

Note 1.3.9: root cause of the sky fix not working since 1.2.0. The compile stubs declared `@Inject` and `@At` with
`RetentionPolicy.CLASS`. Real Mixin declares them `RUNTIME` and only looks for injectors in the visible annotations, so
every `@Inject` handler of this mod was merged into its target as a plain, never called method (seen in
`.mixin.out`: `SkyHandler` contains `dt$skip...`, but `ensureStarPlasmaTexture`, `renderSpaceStars` and
`renderCosmicNebula` have no call to them). `farbound_skyfix` 1.0.0/1.1.0 was built with `RUNTIME` stubs and worked.
1.3.9 is the 1.3.8 jar with the three `@Inject` annotations of `SkyHandlerMixin` made runtime visible, nothing else
changed. The stubs in this folder are corrected. Still inactive on purpose in 1.3.9: the `@Inject` handlers of
`CopycatClientReadMixin` (#419 redraw) and `CopycatModelDiagMixin` have the same defect and have never run in game.
The two material guard mixins add a method without `@Inject` and are not affected.
Also: the sources in `src` are behind the shipped jar (1.3.7/1.3.8 changes were not all written back) and do not
compile against `stubs` alone (`ClientHooks`, `CopycatDiag` need Minecraft/NeoForge classes).

## License
MIT, see `LICENSE`. Free to use in modpacks and in other projects.
Mods this one patches at runtime keep their own licenses. This mod contains none of their code.

## AI notice
The code of DefinitivTim's Duct Tape was written by Claude (Anthropic), an AI. The idea, the requirements, the design decisions and
the testing come from DefinitivTim: he decided what the mod does and tested it in game in his modpack.
