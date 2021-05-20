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

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;

import static io.confluent.connect.marklogic.MarkLogicSourceConfig.CONFIG_DEF;
import static java.util.Collections.singletonList;

public class MarkLogicSource extends SourceConnector {

    public static final String MARKLOGIC_CONNECTOR_VERSION = "0.0.1PRE-B";
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    // fixme - hard coded for now in order to make the connector flow work
    private int batchSize;
    private int numTasks;
    private String filename;
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
        LOG.info("*************** MarkLogic Source Connector 1 is STARTING *************");
        LOG.info("***********************************************");
        LOG.info("*** MarkLogicSourceConnector: start called  ***");
        LOG.info("MarkLogicSourceConnector - Properties File Size: "+props.size());
        StringBuilder sb = new StringBuilder();
        sb.append("MarkLogic Source Connector Properties:\n");
        for (Map.Entry<?, ?> entry : props.entrySet()) {
            sb.append(entry.getKey()).append(" : ").append(entry.getValue()).append("\n");
        }
        LOG.info(""+sb.toString());
        LOG.info("***********************************************");
        properties = props;
        topic = "marklogic";
        batchSize = 100;
        numTasks =1;
    }

    @Override
    public void stop() {
        LOG.info("***********************************************");
        LOG.info("*** MarkLogicSourceConnector: stop called   ***");
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
