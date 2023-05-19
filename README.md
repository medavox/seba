# Space Engineers Blueprint Analyser

a Kotlin/JS web app to produce useful stats about your Space Engineers blueprint.

## Building

You need to provide the project with some game files.

1. Put the following files in `game-files-processor/src/main/resources/`:


    Content/Data/Components.sbc
    Content/Data/CubeBlocks/ (the entire directory)
    Content/Data/Localization/MyTexts.resx

*You can provide a different `MyTexts.resx` file if you would prefer the block names in another language.*

Then, run `game-files-processor`/src/main/kotlin/Preprocessor.kt:main() to extract the needed info from the game files.