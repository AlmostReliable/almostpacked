<div align="center">
<h1>Almost Packed</h1>

An automation tool to allow collaboration on [Minecraft] modpacks.

[![Version][version_badge]][version_link]
[![Workflow Status][workflow_status_badge]][workflow_status_link]
[![License][license_badge]][license]

[Discord] | [Releases] | [Wiki]

</div>

## **📑 Overview**
This small console application allows you to work on [Minecraft] modpacks together easily.<br>
It's automated with the help of two [Git hooks] and thus runs in the background with no user interaction required.

It was mainly built to work with the [CurseForge] launcher.<br>
Other launchers have not been tested since they likely use another data format.


## **📖 Wiki**
If you want to get started, see the features, the workflow or the installation instructions,
check out the [wiki].


## **💚 Credits**
The tool is inspired by [InstanceSync] from [Vazkii].<br>
Credits for the download logic have been noted in the relevant sections.

Notable differences can be found on the [wiki][notable changes].


## **⏰ Changelog**
Everything related to versions and their release notes can be found in the [changelog].


## **🎓 License**
This project is licensed under the [LGPL-3.0][license].

As a general note, since the tool is required to be on your remote repository,
you are of course allowed to redistribute the compiled version of the tool and
the required setup scripts and configs.

It would be much appreciated if credits could be given to this tool on your
modpack page.


<!-- Badges -->
[version_badge]: https://img.shields.io/github/v/release/AlmostReliable/almostpacked?include_prereleases&style=flat-square
[version_link]: https://github.com/AlmostReliable/almostpacked/releases/latest
[workflow_status_badge]: https://img.shields.io/github/workflow/status/AlmostReliable/almostpacked/Build?style=flat-square
[workflow_status_link]: https://github.com/AlmostReliable/almostpacked/actions
[license_badge]: https://img.shields.io/github/license/AlmostReliable/almostpacked?style=flat-square

<!-- Links -->
[minecraft]: https://www.minecraft.net/
[discord]: https://discord.com/invite/ThFnwZCyYY
[releases]: https://github.com/AlmostReliable/almostpacked/releases
[wiki]: https://github.com/AlmostReliable/almostpacked/wiki
[git hooks]: https://git-scm.com/book/en/v2/Customizing-Git-Git-Hooks
[curseforge]: https://download.curseforge.com/
[instancesync]: https://github.com/Vazkii/InstanceSync
[vazkii]: https://github.com/Vazkii
[notable changes]: https://github.com/AlmostReliable/almostpacked/wiki/Explanation#instancesync-vs-almostpacked
[changelog]: CHANGELOG.md
[license]: LICENSE
