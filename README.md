# JmeConvert
A command line utility for converting models to J3O and copying their dependencies to a new target structure.
```
Current command line help:
        _   __  __   ___    ___
     _ | | |  \/  | | __|  / __|
    | || | | |\/| | | _|  | (__
     \__/  |_|  |_| |___|  \___|

    JME3 Asset Converter v1.0.0

Usage: jmec [options] [models]

Where [models] are a list of model files.

Where [options] are:
 -sourceRoot <dir> : specifies the asset root for the models.
       Model paths will be evaluated relative to this root.

 -targetRoot <dir> : specifies the asset target root for writing
       converted assets and their dependencies.

 -targetPath <path> : a path string specifying where to place
       the copied/converted assets within the targetRoot.  Any
       internal asset keys will be 'rehomed' to this path.

Examples:

>jmec -sourceRoot C:\Downloads\CoolModel -targetRoot assets -targetPath Models/CoolModel C:\Downloads\CoolModel\AwesomeThing.gltf

 Converts C:\Downloads\CoolModel\AwesomeThing.gltf to a j3o and writes it
 to ./assets/Models/CoolModel/AwesomeThing.gltf.j3o

 Any dependent textures, etc. relative to C:\Downloads\CoolModel will
 be copied until the appropriate ./assets/Models/CoolModel/* subdirectory.

 For example:
    C:\Downloads\CoolModel\textures\awesome-sauce.jpg
 Gets copied to:
    ./assets/Models/CoolModel/textures/awesome-sauce.jpg
```
