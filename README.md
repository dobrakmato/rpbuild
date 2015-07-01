# rpbuild ![](https://travis-ci.org/dobrakmato/rpbuild.svg)
Build system for Minecraft resource packs.

![not fancy rpbuild's logo](http://i.imgur.com/Oh0khp2.png)

This tool simplifies process of assembling/compressing and distribution of Minecraft resource packs by automatization of some tasks (minification of jsons, generation of  sounds.json, rezising images for different RP resoultions).

**Latest build:** <http://ci.alpha.mtkn.eu/job/rpbuild/lastBuild/>

- [How to get started?](#how-to-get-started)
- [Running rpbuild](#running-rpbuild)
- [Build configuration](#build-configuration)
  - [Build steps (tasks)](#build-steps-tasks)
  - [Xml configuration](#xml-configuration)
- [Using as Maven plugin](#maven-plugin)

![How is rpbuild used?](http://i.imgur.com/hS5itcg.png)

![](http://i.imgur.com/vSLchIu.png?1)

## How to get started

### Obtain rpbuild.jar

The first step is to obtain rpbuild.jar. You can download it from build server or from maven repo.

`http://ci.matejkormuth.eu/job/rpbuild/lastStableBuild/eu.matejkormuth.starving$rpbuild/`

There is also option of executing the following command to automatically update your rpbuild.jar:

`wget -O - https://raw.githubusercontent.com/dobrakmato/rpbuild/master/rpbuild_update.sh | bash`

### Create configuration

To create default configuration run: 

`./rpbuild.jar create`

`java -jar rpbuild.jar create`

This will create new `rpbuild.xml` file in current directory. You can edit it then to meet your needs.

### Set up git (optional)

Set up git repository in directory you want rpbuild to use as source folder. Don't forget to enable git pull
in configuration. 

<b>Warning! When you are using `git pull` rpbuild automatically destories all local changes using `git stash drop`!</b>

> I suggest you creating git repository (github, bitbucket) for resource pack. That way
> changes to your reseouce pack are tracked and you can always revert to older version 
> of your resource pack. You can also set up hooks, so everytime, when you push changes
> your resource pack gets updated automatically. (This is probably best option for big
> networks / servers or projects where many people work on one resource pack) 

### Set up hooks (optional)

If you want autobuild when you push changes to git you have to enable web hooks if you have them in your git provider.

> To be written.

### Run build or push changes

Now you just have to run rpbuild or push changes to you git repo.

## Running rpbuild

To build your resource pack, just run the jar in folder where configuration file exists:

`./rpbuild.jar`

`java -jar rpbuild.jar`

You can also specify configuration file explicitly.

`./rpbuild.jar [CONFIGURATION FILE]`

`java -jar rpbuild.jar [CONFIGURATION FILE]` 

You can generate default rpbuild.xml config using:

`./rpbuild.jar create`

`java -jar rpbuild.jar create`

## Build configuration

Configuraion consists of build steps and project information. Refer to examples below for help.

### Build steps (tasks)

There are some **build tasks available out of box**, but you are free to extends classes **FileGenerator** and **FileCompiler**. 

**FileGenerator** class generates files that needs to be regenerated each time the resource pack is built (for example sounds.json generator).
These generators are available out of box:

- eu.matejkormuth.rpbuild.generators.PackMcMetaGenerator *(generates pack.mcmeta)*
- eu.matejkormuth.rpbuild.generators.sounds.FileTreeSoundsJsonGenerator *(generates sounds.json from files in sounds directory)*


**FileCompiler** class behaves like compiler of files. It opens generated and supplied files from git and processes them (for example minifies jsons or optimizes png images).
These compilers are available out of box:

- eu.matejkormuth.rpbuild.compilers.JsonCompressor *(minifies jsons)*
- eu.matejkormuth.rpbuild.compilers.ImageResizer *(resizes images to specified size)*
- eu.matejkormuth.rpbuild.compilers.JsonCommenter *(may not work, not recommended for use)*

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
    <!-- Source of resource pack. Leave dot for current directory. -->
    <src>.</src>
    <!-- Target zip file. -->
    <target>/mertex/web/zombie/2/rp/latest.zip</target>
    <!-- Zip archive compression level. -->
    <compressionLevel></compressionLevel>
    <!-- Build steps. -->
    <build>
    	<!-- Generate sounds.json using available sound files using convention. -->
        <generate class="eu.matejkormuth.rpbuild.generators.sounds.FileTreeSoundsJsonGenerator"/>
        <!-- Specifies to run Generator (PackMcMetaGenerator - this one creates pack.mcmeta file) in build. -->
        <generate class="eu.matejkormuth.rpbuild.generators.PackMcMetaGenerator"/>
        <!-- Specifies to run Compiler (JsonCompressor - this one compresess jsons) in build. Files attribute specifies type of files, which this compiler compiles. -->
        <compile class="eu.matejkormuth.rpbuild.compilers.JsonCompressor" files=".json"/>
        <!-- Specifies to run Image Resize in build on all .png files. -->
        <compile class="eu.matejkormuth.rpbuild.compilers.ImageResizer" files=".png">
        	<settings>
        		<!-- Interpolation setting. Can be nearest, bilinear or bicubic. -->
            	<setting key="interpolation" value="nearest" />
            	<!-- Max image resolution in pixels. Put 64 if you want 64x64 resource pack. -->
            	<setting key="maxResolution" value="64" />
            </settings>
        </compile>
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

This type of rpbuild configuration is no longer supported. Support was dropped in `1.0.3`. You are strongly
recommended to migrate to new xml configuration type!

## Maven plugin

You can also use *rpbuild* as part of Maven build. Best way to do this is to use *rpbuild-maven-plugin*.

You can do this by adding build phase to your `pom.xml`.

```xml
<plugin>
  <groupId>eu.matejkormuth.starving</groupId>
  <artifactId>rpbuild-maven-plugin</artifactId>
  <!-- Version should be same as desired rpbuild version. -->
  <version>1.0.3</version>
  <executions>
    <execution>
      <phase>generate-sources</phase>
        <goals>
          <goal>rpbuild</goal>
        </goals>
      </execution>
  </executions>
  <configuration>
    <configurationFile>src/rpbuild.xml</configurationFile> <!-- This is path to your rpbuild.xml project file. -->
  </configuration>
</plugin>
```

Don't forget to add plugin repository because rpbuild-maven-plugin is currently not in maven central repo.

```xml
<pluginRepositories>
  <pluginRepository>
    <id>matejkormuth-repo</id>
    <url>http://repo.matejkormuth.eu</url>
  </pluginRepository>
</pluginRepositories>
```
