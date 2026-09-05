# HexJSNeo

[![Curseforge](https://badges.moddingx.org/curseforge/versions/1679993) ![CurseForge](https://badges.moddingx.org/curseforge/downloads/1679993)](https://www.curseforge.com/minecraft/mc-mods/hexjsneo)  
[![Modrinth](https://badges.moddingx.org/modrinth/versions/hexjsneo) ![Modrinth](https://badges.moddingx.org/modrinth/downloads/hexjsneo)](https://modrinth.com/mod/hexjsneo)

Provides a way to create custom contents for Hex Casting via KubeJS.

[//]: # ([<img src="https://github.com/SamsTheNerd/HexGloop/blob/73ea39b3becd/externalassets/hexdoc-badgecozy.svg?raw=true" alt="A badge for hexdoc in the style of Devins Badges" width=180>]&#40;https://yukkuric.github.io/HexJSNeo&#41;)

[<img src="https://github.com/SamsTheNerd/HexGloop/blob/73ea39b3becd/externalassets/addon-badge-cozy.svg?raw=true" alt="A badge for addons.hexxy.media in the style of Devins Badges" width=160>](https://addons.hexxy.media)

## Features

### Core Concepts
- Minimal efforts: auto registered great pattern tags, omit all optional args, pipe everything, ...
- Weak typed: `ActionJS` returns, `ActionRegistryJS.of`, etc.
- Mutable is best: no registration event and phase limit, writing and reloading everywhere in startup scripts, we're writing JS \yay/
- Clean global scope: everything under a single `HexJS` global object, no leaks

### Game Content Registry
- `ActionJS`: basis for all executables
    - customizable `operate` & `operateInParens`, with high degree-of-freedom return value handling
    - additional helper methods for creating `operate` varieties
    - assigning `mediaCost` to auto-attach media-cost side effect on operate, with `preCheckMedia` methods to help check & mishap before logics
- `ActionRegistryJS`: registering certain patterns with actions
    - = Registry ID + Pattern prototype + inner `ActionJS` + Great-pattern marker (default false)
    - weak-typed constructor `ActionRegistryJS.of(...)`: args in any order among its constructor, missing ones auto-filled (id from pattern signature, fresh `ActionJS`, `false`); only the pattern is required
    - transparent proxy of inner `ActionJS`: operate setters (`setOperate` & variants), `mediaCost` and `preCheckMedia` are callable directly on the registry object
    - great spells are auto-tagged with per-world / enlightenment tags
- `SpecialHandlerJS`: taking an arbitrary pattern and retrieving its meanings
    - registers a JS `tryMatch(pattern, env)` factory consulted for every drawn pattern
    - `SpecialHandlerJS.create(action, name)` to help build a `SpecialHandler`, in which `action` could be another specially created `ActionJS`
    - chain setter `setTryMatch` for KJS-ish usage
- `IotaJS`: highly customizable iota types
    - NBT-backed custom iotas: `IotaJS.type(id)` creates the type, containing `CompoundTag` iota data
    - overriding common iota interfaces (display, color, size, etc.) with either callbacks or constants
    - optional executability via `setOperate`, controlling its execution behaviors outside and inside parens
- `ArgsJS`: basically a forwarder for various built-in Kotlin extension functions
    - used on a mutable iota list manually, or called from `ActionJS.setOperateArgsSplit`
    - typed getters (`double`, `vec3`, `entity`, ...) with auto mishaps on missing args or wrong types
- `CastingEnvironmentComponentJS`: vanilla but in JS, with more convenient Key creation
    - each component takes a key string as the first constructor arg, from which the `Key` is auto-created & cached (`setKey` to change it later)
    - usages and types same as vanilla

### QoL Features

- Hot-swap everything
    - everything, simple as `/kjs reload startup-scripts`
    - covers: actions (entry action & prototype replaced in-place), special handlers, iota types
    - not for those haven't created on startup, but it's possible to match later-created pattern actions using a special handler :3
- API & packages importer: `HexJS.API` & `HexJS.APIFlat`
    - exposes all Hex Casting classes scanned from the mod jar, either nested by package (`HexJS.API.<...package>.<Class>`) or flat (`HexJS.APIFlat`)

### ProbeJS Typing Support

- generates typings for the JS-facing API, including nested `HexJS.API` levels, fake-singleton classes flattened to static fields, and subclass members assigned onto their parents
