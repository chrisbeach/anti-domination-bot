FROM hseeberger/scala-sbt:11.0.1_2.12.7_1.2.6

COPY . /root

# Ensures SBT dependencies are present in image and not downloaded at runtime
RUN /usr/bin/sbt run || true

ENTRYPOINT ["/usr/bin/sbt"]
CMD ["run"]
