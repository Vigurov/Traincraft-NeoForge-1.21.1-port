# Traincraft — unofficial NeoForge 1.21.1 port

An unofficial port of [Traincraft](https://github.com/Mrbrutal/Traincraft-5) (Traincraft 5 with Trains in Motion,
Minecraft 1.12.2) to **Minecraft 1.21.1 / NeoForge 21.1**, with reworked train physics and an overhead line for the
electric locomotives.

> **This is a derivative of the official Traincraft mod. It is not made, maintained or endorsed by the Traincraft
> team.** For the official mod see the links under [Credits](#credits).

## Credits

All of Traincraft's trains, models, textures, sounds and most of its code are the work of its authors:

- **Traincraft** — created by **Spitfire4466** and **Mrbrutal**
  ([Traincraft-5](https://github.com/Mrbrutal/Traincraft-5), [traincraft-mod.com](http://www.traincraft-mod.com),
  [Discord](https://discord.gg/SgpnCnK))
- **Trains in Motion (TiM)** and the Traincraft 5 rewrite — **EternalBlueFlame**
  ([EternalBlueFlame/Traincraft-5](https://github.com/EternalBlueFlame/Traincraft-5))
- with **NitroxydeX, canitzp, ComputerButter, DAYdiecast** and all Traincraft contributors
- the TMT model renderer (Turbo Model Thingy by GaryCXJk, Fexcraft's slim version)

The NeoForge 1.21.1 port and the changes listed below: **viguro**.

## License

The Traincraft parts are under the **Traincraft Mod License**, see [LICENSE.md](LICENSE.md)
([original](https://github.com/Mrbrutal/Traincraft-164/blob/master/LICENSE.md)). This port is published under the same
terms: credit to the authors, not for commercial use, and **no builds are distributed here** — the license does not
allow distributing modified builds. Build it yourself for your own use (below).

The Gradle/NeoForge template files come from the [NeoForged MDK](https://github.com/NeoForged/MDK) under the MIT
license, see [TEMPLATE_LICENSE.txt](TEMPLATE_LICENSE.txt).

## What changed from Traincraft 1.12.2

**Port**
- the whole mod on NeoForge 1.21.1 / Java 21: locomotives (steam, diesel, electric), rolling stock, the classic
  Traincraft tracks (straights, turns, 45° turns, parallel curves, switches, crossings, road crossings, slopes),
  assembly tables, distillery, hearth furnace, generators, signals, lanterns, uniforms
- works on dedicated servers; every item can be obtained in survival (recipes checked from the ores up)

**Train physics**
- consists on turns, switches in both positions, reversing through them, slopes with whole trains
- derailing when a train takes a turn too fast (scaled real-world limits); derailed vehicles uncouple and the rest
  of the train stops against them
- collisions between trains, being hit by a train hurts by its speed, standing and riding on the roofs
- tracks you can stand on (a low collision bed)

**Overhead line (new)**
- lattice catenary masts with glass insulators, cantilevers made to measure for the track in front (also on turns),
  contact and messenger wire strung mast to mast, a warning when a span strays off the track
- a 2×2×2 traction substation fed by generators (windmills, water wheels, diesel generator) through bare
  steel-aluminium power cables, feeders to the line; Forge Energy, with losses along the line
- electric locomotives only run under a live wire; their pantographs rise to it (per pantograph, also on slopes)
  and fold when it ends; wires can be cut anywhere along them

**Look**
- blocks, ores and items redrawn on the 1.21 textures; the classic tracks' icons drawn whole, as they lie once placed
  (`tools/gen_track_icons.py`); the machines, masts and substation show their 3D models as items
- copper is the vanilla one; TiM's own rail system is out of the creative tab and recipes (kept registered)

**Known gaps**: the IC4 locomotive has no recipe (as in the original); the tight 1×1 and 3×3 turns are retired.

## Building

Requires JDK 21.

```
./gradlew build
```

The mod is `build/libs/traincraft-5.0.0-neo.jar`; put it in the `mods` folder of a NeoForge 1.21.1 (21.1.265)
installation. `./gradlew runClient` starts a development client.

### Automated tests

`train.devtest.AutoTest` drives a development client from `run/tctest.properties` (place tracks, trains, masts and
wires, drive, log, take screenshots); `tools/run-autotest.sh LOG` runs it. The options are documented in the source.

---

### Українською

Неофіційний порт мода Traincraft (1.12.2) на Minecraft 1.21.1 / NeoForge, з переробленою фізикою потягів і
контактною мережею для електровозів. Усі потяги, моделі й більшість коду належать авторам Traincraft (див. Credits).
Готових збірок тут немає: ліцензія Traincraft не дозволяє поширювати змінені збірки, тому мод треба зібрати самому
(`./gradlew build`).
