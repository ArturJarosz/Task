FROM ubuntu:latest
LABEL authors="artur"

ENTRYPOINT ["top", "-b"]
