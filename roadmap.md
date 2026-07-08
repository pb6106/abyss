# Abyss Roadmap

This document outlines the major versions of Abyss, past and planned. Only major (x.0.0) versions are listed here. Smaller updates and hotfixes use:

`release.smallupdate.hotfix`

For example, `3.1.0` is a small update on top of 3.0 (a few fixes or tweaks), while `4.2.1` is a hotfix on 4.2 (a crash or minor bug).

---

## Version overview

| Version | Name | Progress | Summary |
|---|---|---|---|
| 1.0 | Foundation | Released | The Abyss dimension: tall water column, mud seabed, no caves/ravines, depth fog, and ambient soundtrack. |
| 2.0 | Not Alone | Released | Seabases and submarine wreck structures, `/abyss` commands, seabase loot, and vanilla ocean structures in the biome. |
| 3.0 | Laying Foundations | In Progress | Custom building blocks for underwater bases, plus recipes and structure integration. |
| 4.0 | Deeper and Darker | Planned | A natural way into the Abyss (likely tied to the End void). Subject to change. |
| 5.0 | Building Pressure | Planned | Custom drowning/oxygen rules, Create contraption exceptions, and breathable air inside sealed hull spaces. |

---

## 1.0 -- Foundation (released)

- Single biome deep-ocean dimension (`abyss:abyss`)
- Sea level at Y=1000 with ~950 blocks of water above the seabed
- Mud floor terrain, no caves, ravines, aquifers, or ore veins
- Midnight lighting, depth fog, tropical fish, Subnautica-inspired music
- `/abyss teleport` for testing access

---

## 2.0 -- Not Alone (released)

- **Seabase** structure (rare) with chest loot and structure processors
- **Submarine wreck** structure (common) with five visual variants
- Random rotation on worldgen placement
- `/abyss vision` and `/abyss placestructure` for testing
- Vanilla shipwrecks, cold ocean ruins, and ocean monuments enabled in the Abyss biome

---

## 3.0 -- Laying Foundations (in progress)

The goal of 3.0 is to give players a block palette that actually feels like building in the Abyss -- industrial hull plating, interior trim, viewports, and lights -- instead of relying on vanilla blocks alone. Blocks should read well in dark water and match the rusted-copper tone already present in seabase loot.

### Blocks to add in 3.0

#### Structural (exterior hull)

| Block | Notes |
|---|---|
| **Abyssal Plating** | Main wall block. Dark blue-grey metal, full block. Crafted from iron + copper (or similar). |
| **Abyssal Plating Slab / Stair** | Same texture family as plating -- needed for corners and trim. |
| **Corroded Plating** | Weathered variant of abyssal plating (cracks, rust streaks). Found in wreck-themed builds; craftable from plating + water bucket or vinegar-style recipe. |
| **Reinforced Bulkhead** | Blast-resistant hull block (high hardness). Slower to mine underwater. Intended as the “serious” outer shell for bases. |

#### Interior & detail

| Block | Notes |
|---|---|
| **Bulkhead Panel** | Lighter interior wall block (ribbed metal look). Pairs with plating for inside/outside contrast. |
| **Bulkhead Panel Slab** | Ceiling trim, door headers, narrow corridors. |
| **Floor Grating** | Partial block (like iron bars footprint). Lets water flow through but players can walk on it -- good for sub bay floors. |
| **Pipe Casing** | Thin decorative block (1-block-tall or oriented like a log). Runs along walls/ceilings for industrial detail. |

#### Viewports & access

| Block | Notes |
|---|---|
| **Hatch** | Industrial door that comes in 2x2 and 3x3, works on both floor and walls |

#### Lighting

| Block | Notes |
|---|---|
| **Floodlight** | Directional light block (faces like a dispenser). Stronger than sea lanterns, short range -- meant for base exteriors and docking bays. |
| **Running Light** | Small wall-mounted indicator light (dim, colored). Cosmetic status lights for corridors and control rooms. |

#### Terrain tie-in

| Block | Notes |
|---|---|
| **Packed Seabed** | Crafted from mud. Same color family as the dimension floor but buildable -- for blending bases into the seabed or making foundations. |
| **Seabed Bricks** | Smoother, brick-pattern variant of packed seabed for paths and platforms. |

### Other 3.0 goals (non-block)

- [ ] Creative tab or item group for Abyss blocks
- [ ] Crafting recipes for all new blocks (stonecutter recipes for slabs/stairs where it makes sense)
- [ ] Add a few new blocks to **seabase chest loot** (small stacks of plating, glass, floodlights)
---

## 4.0 -- Deeper and Darker (planned)

- Natural entry into the Abyss without `/abyss teleport`
- Likely involves changing what happens when falling into the void in the End (exact design TBD)
- May add a one-way or risky transit method rather than a free portal
- Any supporting blocks/items (dive bell frame, depth beacon, etc.) would be added here, not in 3.0

---

## 5.0 -- Building Pressure (planned)

- Custom oxygen / drowning behavior below sea level in the Abyss
- **Exception:** players on a stable Create contraption do not drown (Create: Deep Seas synergy)
- Sealed interior spaces (up to ~25,000 blocks, enclosed by waterloggable hull blocks) replace water inside with breathable air
- **Hatch** and **Reinforced Bulkhead** from 3.0 gain real pressure/airtight behavior

---

## Ideas backlog (no version assigned)

- Depth gauge item
- Biome sub-zones (trenches, flats, ridges) -- would need terrain rework
- Config file for structure spacing and sea level
