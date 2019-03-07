# Personal budget manager

**Status: In development**

## how to run:

`./run.sh`

```
gradle build

java -jar build/libs/personal-budget-manager.jar  
```

## Overviews

### Month overview

#### Detailed

#### Data input

#### Summary

#### Diagram

### Wallets

Wallets overview allows to manage wallet setup:

- wallets:
    - list of wallets: wallet name (with display glyph), wallet tags (as list, with display glyph), notes / description,
status (active / archived), action (delete / restore)
    - wallet create/edit form: glyph, name, tags selector, notes/ description, initial amount, initial month. Initial
amount and month are used for creation of initial transaction for this wallet.
    - list filtering: tags, status
- wallet tags:
     - list of tags: tag value (with display glyph), tag aspect, status (active / archived), action (delete / restore)
     - tag create/edit form: glyph, tag value, tag aspect
     - list filtering: by status

### Tags

Tag overview allows to manage a list of tags, that is used for transaction grouping:

- list of tags: tag value (with display glyph), tag aspect, status (active / archived), action (delete / restore)
- tag create/edit form: glyph, tag value, tag aspect
- list filtering: by status

### Parser Builder

## Features

### Implemented

- tag management
- wallet management
- parser management
- transaction detailed overview
- transaction single input

### ToDo

- building and running automation
- internationalisation
- account switching
- integration with GDrive

## minor ToDo's

- tag overview: full window table
- tag overview: create and use a component for the glyph selection
- tag overview: add filtering by the aspect(s)
- wallet tags: remove aspects