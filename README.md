# MarkLogic Kafka Source Connector

_Please note this is in early stages of development_

## Prerequisites

The source MarkLogic database requires:
- A range index on the **prop:last-modified** properties element to query for changes since the connector last ran
- The database-level **maintain last modified** property to be set to **true**

Both of these can be configured by running the following XQuery (note this uses the **Documents** database as an example):

```xquery
import module namespace admin = "http://marklogic.com/xdmp/admin" at "/MarkLogic/admin.xqy";

declare variable $CONFIG := admin:get-configuration();
declare variable $DATABASE-ID := xdmp:database("Documents");

let $CONFIG := admin:database-set-maintain-last-modified($CONFIG, $DATABASE-ID, fn:true())
let $CONFIG := admin:database-add-range-element-index($CONFIG, $DATABASE-ID, admin:database-range-element-index("dateTime", "http://marklogic.com/xdmp/property", "last-modified", (), fn:false() ))
return
admin:save-configuration($CONFIG)
```

At the top level of this project is a slightly modified **docker-compose.yml** file; this file maps a local machine directory `~/connectors` into the container for Connect to use:

```
    volumes:
      - ~/connectors:/data/connectors
```

The **connectors** directory (in your home directory) should have a structure like this:

```
.
└── marklogic-source-connector
    └── marklogic-kafka-source-connector-0.0.1c.jar
```

The **docker-compose.yml** file takes the standard Confluent Platform file and also instantiates a single MarkLogic instance for testing.

You may need to log-in to Docker Hub and checkout the MarkLogic Docker image on first use:

https://hub.docker.com/_/marklogic

You may need to pull the latest MarkLogic Docker image before you can use `docker-compose`:

```
docker pull store/marklogicdb/marklogic-server:10.0-6.2-dev-centos
```

## To Build the Connector 

If you run the gradle task: `buildConnectorJar`, this will create a jar in `build/libs`; copy the jar into the `connectors` directory.

If you run the gradle task: `copyConnectorJar`, this will clean, build the classes, assemble the project and copy the jar into the target directory.

Finally, start up the docker containers; from the root directory of this project, run:

```
docker compose up -d
```

Ensure MarkLogic is completely initialised and that you can log in (admin/admin) to: 

 - http://localhost:8001 (Admin GUI)
 - http://localhost:8000 (Query Console / Default XDBC Source)

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