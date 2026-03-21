# Project cahiers-notes: Cahiers de notes

Application pour enregistrer des notes sur divers sujets. Les cahiers sont des répertoires sur le disque alors que les notes sont des fichiers en format texte (éventuellement en format MarkDown).

## Installation

Installer le fichier .jar en format uberjar à l'endroit désiré.

## Lancer l'application

    $ java -jar cahiers-notes-0.1.0-standalone.jar [dossier-principal]


## Sauvegarde automatique

Dès qu'un document est mis en mode "édit", l'appli sauvegardera le texte lors d'un des événements suivants:
- on enlève la coche "Modifier" pour mettre le document en mode affichage seulement
- une autre page est sélectionné ou un autre livre est selectinné; ceci arrivera automatiquement lorsque qu'on ajoutera un cahier ou une page
- lorsque que l'application terminera de façon normale

ATTENTION: Si l'application plante ou est terminée de force par l'utilisateur, les changements seront perdus

## ISSUES
- page listener is broken or not configured with regards to saving docs an resetting edit state
-- page change does not reset the edit state
-- open a page with text
-- then select a page with no text: the new page seems to take on the text from the first page

## TODO
- titre "Input" de showInputDialog => créer le mien?
- add menu and key binding for edit like CTRL+E
- add About that explains command line and auto save
- fix issue with .txt extension for pages: title has no .txt, path does by default
- add app icon

## Testing Functionality
- Three panes remain synchorize
- docs can be editing or read-only as expected by the circumstances
- content is saved and loaded properly
- edit shows text
- read-only shows md

### Add cahier
- cahier list updated
- new cahier selected
- pages list is blank (no page in new book)
- doc state is handled
- - in edit mode: save the content
- - without doc open: no change

### Rename cahier
- list selection is not affected but all paths are udpated in the database OR list of pages is reloaded from newly moved folder
- if doc is loaded, it's not affected

### Select cahier
- with doc open
- page list is load with no selection
- - doc is cleared if displayed
- - doc is saved if editing
- without doc open

### Add page
- with doc open
- without doc open

### Rename page
- with doc open
- without doc open

### Select page
- with doc open
- without doc open


## License

Copyright © 2026 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
