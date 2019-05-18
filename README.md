# JmeConvert
A command line utility for converting models to J3O and copying their dependencies to a new target structure.
```
Current command line help:
        _   __  __   ___    ___
     _ | | |  \/  | | __|  / __|
    | || | | |\/| | | _|  | (__
     \__/  |_|  |_| |___|  \___|

    JME3 Asset Converter v1.0.0 build:2019-05-17T03:46:59-0400

03:47:00,290 INFO  [Convert] Max memory:3641.00 mb
Usage: jmec [options] [models]

Where [models] are a list of JME-compatible model files.

Where [options] are:
 -sourceRoot <dir> : specifies the asset root for the models.
       Model dependency paths will be evaluated relative to this root.

 -targetRoot <dir> : specifies the asset target root for writing
       converted assets and their dependencies.

 -targetPath <path> : a path string specifying where to place
       the copied/converted assets within the targetRoot.  Any
       internal asset keys will be 'rehomed' to this path.

 -script <path> : a script file that will be run against the model
       before writing out.  Any number of script files can be specified
       and they will be run in the order specified.
       Groovy and Javascript are supported 'out of the box' but any
       JSR 223 compatible scripting engine should work if on the classpath.

 -probe [probe options string] : configures the information that the probe
       will output.
       [probe options]:
       A : all options turned on, same as: btrscpdu
       b : show bounding volumes
       t : show translations
       r : show rotation
       s : show scale
       c : show the list of controls
       p : show material parameters
       u : show user-added data
       d : list asset dependencies

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
