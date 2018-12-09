### Anti-Domination Bot

Polls latest topics on a Discourse forum and posts a warning message if the user of the most recent topic has authored
a high proportion of recent topics.
 
### Configuration

Customise [src/main/resources/application.conf.sample](src/main/resources/application.conf.sample) and save as
`src/main/resources/application.conf` 

See comments in file for instructions

### Running with Docker

    docker run -t \
        -v /path/to/application.conf:/application.conf \
        chrisbeach/anti-domination-bot

NOTE: `/path/to/application.conf` is the location of your customised application.conf file and must be specified as an 
absolute path.

### Running (Development)

NOTE: Requires [SBT](https://www.scala-sbt.org/)

    sbt run