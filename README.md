# rpbuild
Build system for Minecraft resource packs.

This tool simplifies process of assembling/compressing and distribution of Minecraft resource pack by automatization of some tasks.


![](http://i.imgur.com/vSLchIu.png?1)

### Build descriptor (Build configuration)

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
