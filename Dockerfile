# Docker image to have a stable building context

FROM openjdk:8

ENV SBT_VERSION 1.2.8

RUN \
  curl -L -o sbt-$SBT_VERSION.deb https://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
  dpkg -i sbt-$SBT_VERSION.deb && \
  rm sbt-$SBT_VERSION.deb && \
  apt-get update && \
  apt-get install sbt

COPY . /opt

WORKDIR /opt

ENTRYPOINT sbt
