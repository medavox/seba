# Space Engineers Blueprint Analyser

A Kotlin/JS web app to produce useful stats about your Space Engineers blueprint.

Is your blueprint too heavy? Does it have too high a PCU? 

This small web app can help you find the greatest contributors.

It extracts data directly from the game files before compiling to javascript, 
so it's easy to keep up to date AND runs pretty fast without need for a backend. 

## Building

You need to provide the project with some game files before compilation.

1. Put the following files in `game-files-processor/src/main/resources/`:

```
    Content/Data/Components.sbc
    Content/Data/CubeBlocks/ (the entire directory)
    Content/Data/Localization/MyTexts.resx
```

*You can provide a different `MyTexts.resx` file if you want block names in another language.*

2. Run `game-files-processor/src/main/kotlin/Preprocessor.kt:main()` to extract the needed info from the game files.

3. Compile & run the web app with `./gradlew run`, or update the live web app with `./gradlew browserProductionWebpack`. 
