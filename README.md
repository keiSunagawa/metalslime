WIP

## metalslime
metalslime is search for dependency file from diff file via [metals Language Server](https://github.com/scalameta/metals)  
this useful to extract for test target.  

## Usage
- add your project/plugins.sbt
```
addSbtPlugin("ch.epfl.scala" % "sbt-bloop" % "1.4.0-RC1")
```

```shell
$ cd <your git project>
# create diff file.
$ git diff --color=never -U0 --relative="<sbt project dir>" master <your branch> > diff.txt
$ cd <sbt project root>
$ sbt bloopInstall
$ cd <metalslime root>
$ sbt "metalslime/run <sbt project root> <diff file path>"
```
