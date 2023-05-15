# Space Engineers Blueprint Analyser

a Kotlin/JS web app to produce useful stats about your Space Engineers blueprint.

## Building

you need to provide the project with some game files:

```
Content/Data/Components.sbc
Content/Data/CubeBlocks/
Content/Data/Localization/MyTexts.resx
```

You can provide a different `MyTexts.resx` file if you would prefer the block names in another language.


Next, you need to run the `game-files-processor` module to extract from the game files only the information the web app needs.