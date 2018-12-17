# Release

```bash
docker build . -t akka-http-cors:latest
docker run -it --rm -v ~/.ivy2:/root/.ivy2 -v ~/.sbt:/root/.sbt -v ~/.gnupg:/root/.gnupg akka-http-cors:latest
> + test
> project akka-http-cors
> + publishSigned
```
