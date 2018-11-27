FROM hseeberger/scala-sbt

COPY . /root

# For convenience - docker run command can bind-mount application.conf:/application.conf
COPY /src/main/resources/application.conf /application.conf
RUN rm /root/src/main/resources/application.conf && ln -snf /application.conf /root/src/main/resources/application.conf

# Ensures SBT dependencies are present in image and not downloaded at runtime
RUN /usr/bin/sbt run || true

ENTRYPOINT ["/usr/bin/sbt"]
CMD ["run"]
