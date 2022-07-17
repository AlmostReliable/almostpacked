# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog], and this project adheres to [Semantic Versioning].

## [Unreleased]
- /

## [1.1.0] - 2022-07-18

### Added
- new config option `failOnChange`
  - lets the push hook fail if changes were made to the sync file
  - this allows you to push the sync file to the same commit as the original
- two new instance overwrites to ensure the launcher doesn't load the instance from backup
  - `lastPreviousMatchUpdate`
  - `lastRefreshAttempt`

### Changed
- enabling/disabling mods is now properly synced
- improved some logging

## [1.0.0] - 2022-07-16
- initial release

<!-- Links -->
[keep a changelog]: https://keepachangelog.com/en/1.0.0/
[semantic versioning]: https://semver.org/spec/v2.0.0.html

<!-- Versions -->
[unreleased]: https://github.com/AlmostReliable/almostpacked/compare/v1.0.0...HEAD
[1.1.0]: https://github.com/AlmostReliable/almostpacked/releases/tag/v1.0.0..v1.1.0
[1.0.0]: https://github.com/AlmostReliable/almostpacked/releases/tag/v1.0.0
