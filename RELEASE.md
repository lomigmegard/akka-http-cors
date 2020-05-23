# Release

Build the release with Java 8. For macOs:

```bash
brew tap adoptopenjdk/openjdk
brew cask install adoptopenjdk8
sbt -v --java-home /Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home
> + test
> project akka-http-cors
> + publishSigned
> sonatypeBundleRelease
```
