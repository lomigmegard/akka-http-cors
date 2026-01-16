# Release

Build the release with Java 8. 
See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html.

```bash
brew install openjdk@8
export SONATYPE_USERNAME=xxx
export SONATYPE_PASSWORD=yyy
sbt -v --java-home /usr/local/opt/openjdk@8/libexec/openjdk.jdk/Contents/Home
> + test
> project akka-http-cors
> + publishSigned
> sonaRelease
```
