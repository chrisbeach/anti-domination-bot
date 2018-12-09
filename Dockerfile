FROM hseeberger/scala-sbt:11.0.1_2.12.7_1.2.6

COPY . /root

# Simplify bind mount by placing application.conf at root
COPY /src/main/resources/application.conf /application.conf
RUN rm /root/src/main/resources/application.conf && ln -snf /application.conf /root/src/main/resources/application.conf

# Ensures SBT dependencies are present in image and not downloaded at runtime
RUN /usr/bin/sbt run || true

ENTRYPOINT ["/usr/bin/sbt"]
CMD ["run"]
