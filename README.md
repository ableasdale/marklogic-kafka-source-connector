## MarkLogic Kafka Source Connector

_Please note this is in early stages of development_

At the top level is a slightly modified **docker-compose.yml** file.

This file maps a local machine directory `~/connectors` into the container:

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

## To Build the Connector 

If you run the gradle task: `buildConnectorJar`

This will create a jar in `build/libs`; copy the jar into the `connectors` directory.

Finally, start up the docker containers; from the root directory of this project, run:

```
docker compose up -d
```

Open the Confluent Control Center: http://localhost:9021

