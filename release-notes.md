Version 1.1.1 (latest)
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

 
