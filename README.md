# rpbuild ![](https://travis-ci.org/dobrakmato/rpbuild.svg)
Build system for Minecraft resource packs.

This tool simplifies process of assembling/compressing and distribution of Minecraft resource pack by automatization of some tasks (minification of jsons, generation of  sounds.json).

**Latest build:** <http://ci.alpha.mtkn.eu/job/rpbuild/lastBuild/>

- [Build configuration](#build-configuration)
  - [Xml configuration](#xml-configuration)
  - [Legacy (deprecated) configuration](#legacy-configuration-deprecated)


![](http://i.imgur.com/vSLchIu.png?1)

## Build configuration

To build your resource pack, just run the jar in folder, configuration file exists:

`./rpbuild.jar`

`java -jar rpbuild.jar`

You can also specify configuration file explicitly.

`./rpbuild.jar [CONFIGURATION FILE]`

`java -jar rpbuild.jar [CONFIGURATION FILE]` 

### Xml configuration

The best way to make configuration file for your build is to use XML configuration. Create file with name `rpbuild.xml` in folder with resource pack or use `rpbuild.jar create` command to generate one for you. The file is self-explaining. Look at the example below. 

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<project>
    <!-- Name of resource pack. -->
    <name>ResourcePack Name</name>
    <!-- Encoding / charset used in build. -->
    <encoding>UTF-8</encoding>
    <!-- Whether to run git pull before building resource pack. -->
    <gitPull>false</gitPull>
    <!-- Whether to exclude .git folders in target zip archive. -->
    <ignoreGitFolders>true</ignoreGitFolders>
    <!-- Source of resource pack. -->
    <src>.</src>
    <!-- Target zip file. -->
    <target>/mertex/web/zombie/2/rp/latest.zip</target>
    <!-- Build steps. -->
    <build>
        <!-- Specifies to run Generator (PackMcMetaGenerator - this one creates pack.mcmeta file) in build. -->
        <generate class="eu.matejkormuth.rpbuild.generators.sounds.PackMcMetaGenerator"/>
        <!-- Specifies to run Compile (JsonCommenter - this one adds comment to all jsons) in build on all .json files. -->
        <compile class="eu.matejkormuth.rpbuild.compilers.JsonCommenter" files=".json">
        	<settings>
            	<setting key="comment" value="My cool comment!" />
            </settings>
        </compile>
        <!-- Specifies to run Compiler (JsonCompressor - this one compresess jsons) in build. Files attribute specifies type of files, which this compiler compiles. -->
        <compile class="eu.matejkormuth.rpbuild.compilers.JsonCompressor" files=".json"/>
    </build>
    <!-- Filters - endings of files which will be excluded in target zip file. -->
    <filters>
        <!-- Matches rpbuild.xml -->
        <filter>rpbuild.xml</filter>
        <!-- Matches everyting that ends with .jar -->
        <filter>.jar</filter>
        <filter>.zip</filter>
        <filter>.bat</filter>
        <filter>.php</filter>
        <filter>.sh</filter>
        <filter>.md</filter>
        <filter>.log</filter>
    </filters>
</project>
```

### Legacy configuration (deprecated)

Build descriptors are just java properties files. Take a look at example file:

```properties
# Whether to optimize files or not (currently JSON compression). (default true)
optimizeFiles=true
# Name of resource pack. (default ResourcePack)
projectName=My Cool Resource Pack
# Target ZIP file. 
zipName=/var/www/path/to/resourcepack/latest.zip
# Whether to ignore any .git directories. (default true)
ignoreGit=true
# Specifies file types separated by semicolon, that will be excluded from final zip archive.
filters=.jar;.zip;.php;.rpbuild;.md;.bat;.sh;.log;.bak;
# Whether to run git pull automatically before build. (default false)
gitPull=true
# Description (or name) of resource pack. (default generated description contains project name and build date and time)
resourcePackDescription=MyResource pack.
# Encoding which will be used for build (default is UTF-8)
encoding=UTF-8
```
Not all of these values are required. If no value is specified, default one will be used.
