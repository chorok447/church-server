FROM ubuntu:latest
LABEL authors="jdw"

ENTRYPOINT ["top", "-b"]