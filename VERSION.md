## Touch Typist's Completion Caddy

[TOC]: # " Version History"

### Version History
- [Touch Typist's Completion Caddy](#touch-typists-completion-caddy)
    - [Version 1.5 - Enhancement Release](#version-15---enhancement-release)
    - [Version 1.4 - Enhancement Release](#version-14---enhancement-release)
    - [Version 1.3 - Bug Fix Release](#version-13---bug-fix-release)
    - [Version 1.2 - Bug Fix Release](#version-12---bug-fix-release)
    - [Version 1.1 - Bug Fix Release](#version-11---bug-fix-release)
    - [Version 1.0 - Initial Release](#version-10---initial-release)


### Version 1.5 - Enhancement Release

* Add: customization of characters on which to disable auto-pop completions in addition to space
* Fix: in 2019.x completion in language text generated exception
* Fix: detection of plain text completion to handle completion on space before the options are
  displayed during fast typing.

### Version 1.4 - Enhancement Release

* Add: `For plain text completions in text boxes (VCS commit message, etc.)` option to stop
  completions on space in text boxes, such as VCS commit message.

### Version 1.3 - Bug Fix Release

* Add: `resources/search/searchableOptions.xml` for full text search across all configuration
  settings.

### Version 1.2 - Bug Fix Release

* Fix: NPE when editor has no virtual file

### Version 1.1 - Bug Fix Release

* Add: tracking of last typed character to prevent backspace followed by space from causing
  completion to be applied. Now will only complete on space if up/down is used to change
  selection in auto-popup completion.

### Version 1.0 - Initial Release

* Add: basic auto-popup completion control on space

