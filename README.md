## MarkLogic Kafka Source Connector

_Please note this is in early stages of development_

At the top level is a slightly modified **docker-compose.yml** file; this file maps a local machine directory `~/connectors` into the container:

```
    volumes:
      - ~/connectors:/data/connectors
```

This directory should have a structure like this:

```
.
└── marklogic-source-connector
    └── marklogic-kafka-source-connector-0.0.1c.jar
```

The **docker-compose.yml** file takes the standard Confluent Platform file and also instantiates a single MarkLogic instance for testing.

You may need to log-in to Docker Hub and checkout the MarkLogic Docker image on first use:

https://hub.docker.com/_/marklogic

And you may need to pull the latest MarkLogic image before you can use docker-compose:

```
docker pull store/marklogicdb/marklogic-server:10.0-6.2-dev-centos
```

## To Build the Connector 

If you run the gradle task: `buildConnectorJar`

This will create a jar in `build/libs`; copy the jar into the `connectors` directory.

Finally, start up the docker containers; from the root directory of this project, run:

```
docker compose up -d
```

Ensure MarkLogic is completely initialised and that you can log in (admin/admin): http://localhost:8001
Open the Confluent Control Center: http://localhost:9021

Sample (Minimal) connector configuration settings:

```
{
  "name": "MarkLogicSourceConnector_0",
  "connector.class": "io.confluent.connect.marklogic.MarkLogicSource",
  "ml.connection.host": "localhost",
  "ml.connection.port": "8000",
  "ml.connection.user": "admin",
  "ml.connection.password": "admin"
}
```

## Docker Cleanup

```
docker compose down
docker system prune
```