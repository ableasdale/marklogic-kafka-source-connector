/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.confluent.connect.marklogic;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.confluent.connect.marklogic.MarkLogicSourceConfig.CONFIG_DEF;
import static java.util.Collections.singletonList;

public class MarkLogicSource extends SourceConnector {

    public static final String MARKLOGIC_CONNECTOR_VERSION = "0.0.1-PRE";
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    // fixme - hard coded for now in order to make the connector flow work
    private int batchSize;
    private int numTasks;
    private String topic;
    private Map<String, String> settings;
    private Map<String, String> config;
    private Map<String, String> properties;

    @Override
    public ConfigDef config() {
        return CONFIG_DEF;
    }

    @Override
    public void start(final Map<String, String> props) {
        LOG.info("***********************************************");
        LOG.info("***         MarkLogicSource: START          ***");
        LOG.info("***********************************************");
        LOG.info(String.format("*** Connector version: %s", MARKLOGIC_CONNECTOR_VERSION));
        LOG.info(String.format("*** Properties Configured: %d", props.size()));
        props.forEach((key, value) -> LOG.info(MessageFormat.format("*** {0} : {1} ", key, value)));
        LOG.info("***********************************************");
        properties = props;
        topic = "marklogic";
        batchSize = 100;
        numTasks = 1;

        // Simplest possible test: an unauthenticated HTTP connection to the MarkLogic healthcheck probe to confirm MarkLogic is fully initialized and responding
        try {
            URL url = new URL("http://marklogic:7997");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            LOG.info("***********************************************");
            LOG.info("***      MarkLogicSource: Healthcheck       ***");
            LOG.info("***********************************************");
            LOG.info(MessageFormat.format("*** Healthcheck HTTP response message: {0}", con.getResponseMessage()));
            LOG.info(MessageFormat.format("*** Healthcheck HTTP response code: {0}", con.getResponseCode()));
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String responseBody = br.lines().collect(Collectors.joining());
            LOG.info(MessageFormat.format("*** Healthcheck HTTP response body: {0}", responseBody));
            LOG.info("***********************************************");
        } catch (ProtocolException | MalformedURLException e) {
            LOG.error("MarkLogic HealthCheck Probe failed: ", e);
        } catch (IOException e) {
            LOG.error("MarkLogic HealthCheck Probe failed with an IOException: ", e);
        }
    }


    @Override
    public void stop() {
        LOG.info("***********************************************");
        LOG.info("***         MarkLogicSource: STOP           ***");
        LOG.info("***********************************************");
    }

    @Override
    public Class<? extends Task> taskClass() {
        return MarkLogicSourceTask.class;
    }

    @Override
    public List<Map<String, String>> taskConfigs(final int numTasks) {
        return singletonList(properties);
    }

    @Override
    public String version() {
        return MARKLOGIC_CONNECTOR_VERSION;
    }
}
