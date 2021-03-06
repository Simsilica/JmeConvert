Version 1.2.1 (unreleased)
--------------


Version 1.2.0 (latest)
--------------
* Fixed AssedReader to use canonical path instead of absolute path
    because sometimes paths weren't fully resolved and it confused
    the 'localized path' detection.
* Modified Convert to keep a list of ModelScripts instead of just
    script names.  Makes it easier to manage scripts in embedded
    applications.
* Modified ModelScript to be able to take the loaded script String
    on the constructor.
* Modified ModelScript so that it can return its script name.
* Added support for extracting subgraphs into separate j3o files
    linked into the main model with an AssetLinkNode.
    see: ModelInfo.extractSubmodel()
* Fixed an issue where the custom extras loader was not being used
    for GLB files.
* JmecNode for attaching an auto-updating JmeConverted asset to your
    scene that live-updates when the source model changes.


Version 1.1.1
--------------
* Modified to default -sourceRoot to the current directory if not
    specified.
* Fixed an issue saving J3o files that had GLTF imported user-data
    that was not strings.  During import, these are now specifically
    converted to Double or are treated as String if they are nested
    in such a way that JME does not support.


Version 1.1.0
--------------
* Added support for GLTF extensions as user-added data on the
    Spatial.  Includes support for all JSON primitives as well
    as Lists and Maps.
* Added a 'u' option to the -probe to show the user-added data
    of a converted model.


Version 1.0.0
--------------
* Initial release, includes:
    * asset copying and rehoming
    * model script processors for groovy, javascript, etc.
    * model probing options
    * optional material generation


