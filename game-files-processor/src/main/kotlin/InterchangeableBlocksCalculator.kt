//whether a block can be a drop-in replacement for another

//we can narrow down the possibilities :
//its dimensions - x,y,z ordering shouldn't matter, right?
//its attachment surfaces (mount points in the game code) - where it will stick down, and can be stuck onto
    //these seem more complicated than I thought (with 3d coordinates for when things can be attached),
    // but we can still do basic compatibility filtering with number of mountpoints
    // (and also the mountpoint side is listed)
//its conveyor port locations
// maybe as a first pass - number of conveyor ports
// (but I know that this varies between vanilla and DLC variants in some cases, eg refinery)
// also we only need >=1 replacement for each of the (currently) 373 DLC blocks

//we should mark functionally different (structural-only replacements),
// so that we can allow users to enable/disable structural-only fallback replacements

//also consider HP, mass and cost to build

// future improvement:
// allow users to pick which DLC they do/don't own, and don't replace those blocks
// (and potentially even use them as replacements for other DLC blocks which they don't own)


//and then if you take into account block function, then it's probably <5 blocks total

// maybe also do fuzzy matching per-word to programmatically highlight the direct vanilla equivalent

//make sure it's the same gridsize obviously, lol

//also we've got type, xsiType and subType

//if several of those match, it's probably good

//sci-fi interior wall can be replaced by a light armor block

//how many of the words in the human name of the DLC block exist in a candidate replacement?
// record the words that don't - we may be able to build a list of words like "Warfare" and "Sci-Fi" that we can safely ignore in future runs of comparing words
// how to encode bonus similarity points for the words in the DLC human name occurring in order in the candidate?