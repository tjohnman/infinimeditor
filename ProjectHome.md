This is a basic editor for editing the server saved maps in Zachtronics' game _Infiniminer_. It is programmed by **Sallen** and **Taron** in Java.

It currently supports the official Infiniminer branch (v1.5), although its development will be halted until the code is ported to _standard_ Java (so that we can prescind from using Processing). Bug reports and suggestions are and will always be welcome though.

**Ideas for an hypothetic future:**

> Some basic drawing tools such as lines, ovals, rectangles, etc...

> An acceptably close-to-the-game's random map generation algorithm for, uh... making random maps.

> The ability to make and store prefabs for repeated use.

_Don't hesitate to send me your suggestions! Also, if you know how to program in Java and want to collaborate don't even think about it and join the project!_

If you are interested in contributing just send us an e-mail. You can also post bugs and feature requests in the Issues tab.

Go on and download the [latest version](http://code.google.com/p/infinimeditor/downloads/list).


---


Change log:

**Version 0.40 - May 18, 2009**

> Changeable brushsize with 'pgup' and 'pgdown'.

> Undo/Redo function. press 'u' for undo, 'i' for redo (beta).

> Now if you press 'b' the level boundary is shown.

> Added a new function in the menu: Convert 1.3 maps to 1.5 maps.

> Added coordinates.

> Added two minimaps of the missing views, really cool and useful.

> Because of the minimaps rearranged the block choosing.

> Fixed the floodfill bug ([Issue 5](https://code.google.com/p/infinimeditor/issues/detail?id=5)).

> Rightclick puts always empty blocks.

> Some minor visual modifications.

**Version 0.36** - May 13, 2009
> Now the load command prompts for confirmation before closing the current working map.

> Disabled "Save copy..." command when no map is loaded.

> Some minor UI changes.

> Now the ground level is displayed also for axis Y.

**Version 0.35b** - May 13, 2009
> Now Infinimeditor works with 1.4 maps (thanks to Taron!)

**Version 0.35** - May 09, 2009
> Added "Create flat map" to the menu.

> Added menu command "Save copy..."

> Added a ground level indicator.

> Removed spawn points and teleporters from the brush list.

> Changed block type codes to display the new map format correctly.

> Added a flood fill (bucket) command.

**Version 0.31** - May 08, 2009
> Changed the mousewheel code to ignore OS settings and (hopefully) scroll one brush per mousewheel step.

**Version 0.3** - May 07, 2009
> Fixed display of forcefields depending on team.

> Added menu with new map creation options.

> Now '=' and '`_`' keys also move the slicing plane.

> Improved text rendering quality.

> Added mouse wheel support for brush selection.

**Version 0.21** - May 06, 2009
> Fixed crash when trying to load a file that does not exist and changed the open map prompt into a select file dialog.

**Version 0.2** - May 06, 2009
> Added editing capabilities and team and brush selection.

> Fixed the X axis.

> Fixed a bug that changed the map name on save.

**Version 0.1** - May 06, 2009
> Just a visualizer (it cannot edit), and may be buggy as hell.