# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project adheres to [this versioning scheme](https://gist.github.com/cech12/69319028e88c50349a6b044000a6607b).

## [26.1-4.0.1.1] - 2026-05-23
### Fixed
- fixed startup warning about missing mixin refmap

## [26.1-4.0.1.0] - 2026-05-14
### Added
- the possibility to fuse two damaged ceramic shears via crafting for repairing (all loaders) (thanks to bixqa for the idea) https://github.com/cech12/WoodenShears/issues/38

## [26.1-4.0.0.0] - 2026-03-27
### Changed
- updated to Minecraft 26.1 (Fabric 0.144.3+26.1, NeoForge 26.1.0.6-beta, Forge 62.0.3)

## [1.21.11-3.7.0.0] - 2025-12-13
### Changed
- updated to Minecraft 1.21.11 (Fabric 0.139.5+1.21.11, NeoForge 21.11.6-beta, Forge 61.0.2)
- updated Cloth Config support (21.11.151) (Fabric/Quilt)
- updated ModMenu support (17.0.0-alpha.1) (Fabric/Quilt)

## [1.21.9-3.6.0.1] - 2025-10-23
### Fixed
- leash knot on fences could not be sheared (Fabric/Quilt) (for Forge & NeoForge a PR needs to be merged)
- poppy of copper golem could not be sheared (Fabric/Quilt) (for Forge a PR needs to be merged)

## [1.21.9-3.6.0.0] - 2025-10-12
- updated to Minecraft 1.21.9 (Fabric 0.134.0+1.21.9, NeoForge 21.9.15-beta, Forge 59.0.5)
- updated Cloth Config support (20.0.148) (Fabric/Quilt)
- updated ModMenu support (16.0.0-rc.1) (Fabric/Quilt)

## [1.21.6-3.5.0.0] - 2025-06-19
### Changed
- Updated to Minecraft 1.21.6 (Fabric 0.127.1+1.21.6, NeoForge 21.6.6-beta, Forge 56.0.3)
- Updated Cloth Config support (19.0.147) (Fabric/Quilt)
- Updated ModMenu support (15.0.0-beta.2) (Fabric/Quilt)

## [1.21.4-3.4.0.1] - 2025-03-10
### Fixed
- Ceramic Shears recipe used a wrong category (thanks to Merucyr for the report) https://github.com/cech12/WoodenShears/issues/27

## [1.21.4-3.4.0.0] - 2025-02-11
### Changed
- updated to Minecraft 1.21.4 (Fabric 0.116.0+1.21.4, Neoforge 21.4.88-beta, Forge 54.0.26)
- updated Cloth Config support (17.0.144) (Fabric/Quilt)
- updated ModMenu support (13.0.1) (Fabric/Quilt)

## [1.21.3-3.3.0.0] - 2024-11-04
### Changed
- updated to Minecraft 1.21.3 (Fabric 0.107.0+1.21.3, NeoForge 21.3.10-beta, Forge 53.0.7)
- updated Cloth Config support (16.0.141) (Fabric/Quilt)
- updated ModMenu support (12.0.0-beta.1) (Fabric/Quilt)
- use the new common tag "c:tools/shear" for shears (Forge)

## [1.21-3.2.1.0] - 2024-08-30
### Changed
- durability config option is now a text field instead of a slider (Fabric)

## [1.21-3.2.0.2] - 2024-08-10
### Fixed
- fixed shears damaging in Forge

## [1.21-3.2.0.1] - 2024-07-15
### Fixed
- error at world creation & world loading with a data pack that contains shears tag as match tool condition (thanks to ayrus02 for the report) #28

## [1.21-3.2.0.0] - 2024-07-14
### Changed
- updated NeoForge to 21.0.94-beta
- the `config` directory is used for the default configuration (NeoForge)

### Fixed
- crashed on startup with Neoforge (casued by a breaking change in 21.0.82-beta)

## [1.21-3.1.2.0] - 2024-07-07
### Changed
- updated Russian language support (thanks to Alexander317 for the contribution) #27

## [1.21-3.1.1.0] - 2024-07-02
### Added
- added Ceramic Shears to the `c:tools/shear` item tag (Fabric/Quilt & NeoForge) (thanks to Phoupraw for the suggestion) https://github.com/cech12/WoodenShears/issues/17

## [1.21-3.1.0.0] - 2024-07-01
### Changed
- updated Neoforge to 21.0.45-beta

### Fixed
- fixed known issue: wolf armor could not be sheared off (Neoforge, Forge)
- fixed known issue: Bogged could not be sheared (Forge) (was already been fixed in the 1.21-3.0.0.0 update)

## [1.21-3.0.0.0] - 2024-06-23
### Changed
- Updated to Minecraft 1.21 (Fabric 0.100.3+1.21, Neoforge 21.0.29-beta, Forge 51.0.17)
- Updated Cloth Config support (15.0.127) (Fabric/Quilt)
- Updated ModMenu support (11.0.1) (Fabric/Quilt)

## [1.20.6-2.3.0.1] - 2024-06-08
### Fixed
- ceramic shears could not be enchanted (all loaders)

## [1.20.6-2.3.0.0] - 2024-06-08
### Changed
- Updated to Minecraft 1.20.6 (Fabric 0.98.0+1.20.6, Neoforge 20.6.112-beta, Forge 50.1.0)
- Updated Cloth Config support (14.0.126) (Fabric/Quilt)
- Updated ModMenu support (10.0.0-beta.1) (Fabric/Quilt)

### Removed
- dependency WSMLMB is not needed any longer (Fabric/Quilt)
- removed unused shears tags (Neoforge, Fabric/Quilt)

### Known Issues
- wolf armor cannot be sheared off (Neoforge, Forge)
- Bogged cannot be sheared (Forge)

## [1.20.4-2.2.0.0] - 2024-04-13
### Added
- add Fabric (>=0.96.11+1.20.4) support (Fabric, Quilt)

## [1.20.4-2.1.0.2] - 2024-01-19
### Fixed
- fixed startup crash in Forge mod

## [1.20.4-2.1.0.1] - 2024-01-03
### Fixed
- Fixed that version didn't contain the minecraft version

## [1.20.4-2.1.0.0] - 2023-12-20
### Changed
- Update to Minecraft 1.20.4 (Forge 49.0.10, Neoforge 20.4.46-beta)

## [1.20.2-2.0.0.0] - 2023-12-20
### Changed
- Move to Multiloader mod template to support Forge and Neoforge
- Update to Minecraft 1.20.2 (Forge 48.0.49, Neoforge 20.2.86)

## [1.20.1-1.9.0.0] - 2023-08-09
### Changed
- Changed Forge to NeoForge 1.20.1-47.1.54 (compatible with Forge 47.1.0)

## [1.20-1.8.0.0] - 2023-06-08
### Changed
- Update mod to Forge 1.20-46.0.1 #20

## [1.19.3-1.7.1.2] - 2023-02-05
### Fixed
- Added pt_br and pt_pt translations #18 (thanks to sanduicheirainox)

## [1.19.3-1.7.1.1] - 2022-12-30
### Fixed
- Fixed recipes and advancements

## [1.19.3-1.7.1.0] - 2022-12-30
### Changed
- Update mod to Forge 1.19.3-44.0.41

## [1.19-1.7.0.0] - 2022-06-22
### Changed
- Update mod to Forge 1.19-41.0.45

## [1.18.2-1.6.1.1] - 2022-05-04
### Fixed
- Fixed that ceramic shears could be stacked

## [1.18.2-1.6.1.0] - 2022-05-03
### Changed
- Update mod to Forge 1.18.2-40.1.12 (fix breaking sound bug)

## [1.18.2-1.6.0.0] - 2022-05-03
### Changed
- Update mod to Forge 1.18.2-40.1.2 (fix interaction with entities with both hands)

### Added
- server config option to define the durability of ceramic shears

## [1.18.1-1.5.1.0] - 2022-01-10
### Changed
- Update mod to Forge 1.18.1-39.0.16
- to fix Log4J security issue
- and to fix glow lichen interaction #11

## [1.18-1.5.0.1] - 2021-12-01
### Changed
- Update mod to Forge 1.18-38.0.4

## [1.17.1-1.5.0.1] - 2021-08-31
### Changed
- Update mod to Forge 1.17.1-37.0.46

### Fixed
- Fixed interaction with Beehives, Pumpkins and Tripwire Hooks

## [1.17.1-1.5.0.0] - 2021-08-20
### Changed
- Update mod to 1.17.1-37.0.41

### Known Bugs
- Interaction with Beehives, Pumpkins and Tripwire Hooks do not work for now.

## [1.16.5-1.5.0.0] - 2021-08-20
### Added
- Russian and Ukrainian translation (thanks to vstannumdum aka DMHYT) #10

### Changed
- Update mod to Forge 1.16.5-36.1.15
- changed versioning to fit [Forge Recommended Versioning](https://mcforge.readthedocs.io/en/latest/conventions/versioning/)
- add some automated tests

## [1.4.0_1.16] - 2021-02-21
### Changed
- update to 1.16.4-35.1.23 for code cleanup and bugfixes (thanks to Bernasss12 for bug report)

## [1.3.2_1.16] - 2020-09-10
### Added
- Russian language added (thanks to kazmurenko)

## [1.3.1_1.16] - 2020-08-25
### Changed
- Mod supports Minecraft 1.16.2 now

## [1.3.0_1.15] - 2020-04-16
### Changed
- Release for Minecraft 1.16.1
- internal change: removed global loot modifiers because of added shears tag to Minecraft Forge
