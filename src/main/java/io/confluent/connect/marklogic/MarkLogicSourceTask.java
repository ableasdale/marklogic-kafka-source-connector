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

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.datamovement.DataMovementManager;
import com.marklogic.client.datamovement.QueryBatcher;
import com.marklogic.client.document.DocumentManager;
import com.marklogic.client.query.StructuredQueryBuilder;
import com.marklogic.client.query.StructuredQueryDefinition;
import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.ContentSourceFactory;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.exceptions.RequestException;
import com.marklogic.xcc.exceptions.XccConfigException;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

public class MarkLogicSourceTask extends SourceTask {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    protected DatabaseClient client;
    protected ContentSource cs;

    @Override
    public void start(final Map<String, String> config) {
        LOG.debug("***********************************************");
        LOG.debug("***  MarkLogicSourceTask - start() called   ***");
        LOG.debug(String.format("*** Source Task - Properties Configured: %d", config.size()));
        config.forEach((key, value) -> LOG.debug(MessageFormat.format("*** {0} : {1} ", key, value)));
        LOG.debug(String.format("MarkLogic Hostname: %s | MarkLogic Port: %s | MarkLogic Username: %s | MarkLogic Password: %s", config.get(MarkLogicSourceConfig.CONNECTION_HOST), config.get(MarkLogicSourceConfig.CONNECTION_PORT), config.get(MarkLogicSourceConfig.CONNECTION_USER), config.get(MarkLogicSourceConfig.CONNECTION_PASSWORD)));
        LOG.debug("***********************************************");

        // Second simple probe - Can an XDBC Session be created and the MarkLogic Timestamp be returned?
        try {
            Session s = MarkLogicXccContentSourceProvider.getSession("Documents");
            LOG.info("*** MarkLogicSourceTask: current MarkLogic Timestamp: " + s.getCurrentServerPointInTime());
        } catch (RequestException e) {
            LOG.info("MarkLogicSourceTask: Exception Caught: ", e);
        }

        // Third simple probe - Can the Java Client API be used to connect to MarkLogic?
        client = DatabaseClientFactory.newClient(config.get(MarkLogicSourceConfig.CONNECTION_HOST), Integer.parseInt(config.get(MarkLogicSourceConfig.CONNECTION_PORT)), "Meters",
                new DatabaseClientFactory.DigestAuthContext(config.get(MarkLogicSourceConfig.CONNECTION_USER), config.get(MarkLogicSourceConfig.CONNECTION_PASSWORD)));

        LOG.info("*** MARKLOGIC SOURCE CONNECTOR :: Client created: " + client.getDatabase());

        // host.docker.internal

        // Test: Generate a full and-query (to get every URI) and log each
        StructuredQueryDefinition sqd = new StructuredQueryBuilder().and();

        DataMovementManager dmm = client.newDataMovementManager();
        QueryBatcher batcher = dmm.newQueryBatcher(sqd);
        batcher.onUrisReady(batch -> {
                    for (String uri : batch.getItems()) {
                        LOG.debug("URI: " + uri);
                    }
                }
        ).onQueryFailure(e -> LOG.error("Failure processing batch: ", e));
        // *** Step 4: Submit the DMSDK job ***
        dmm.startJob(batcher);
        // Wait for the job to complete, and then stop it.
        batcher.awaitCompletion();
        dmm.stopJob(batcher);
    }

    @Override
    public List<SourceRecord> poll() throws InterruptedException {
        LOG.info("*** MarkLogicSourceTask - calling poll ***");
        Session s = MarkLogicXccContentSourceProvider.getSession("Documents");
        try {
            LOG.info("*** MarkLogicSourceTask :: poll() - current MarkLogic Timestamp: " + s.getCurrentServerPointInTime());
        } catch (RequestException e) {
            LOG.error("Exception occurred:", e);
        }
        // TODO - this will be a range query (prop:last-modified) eventually
        StructuredQueryDefinition sqd = new StructuredQueryBuilder().and();
        /*
  <cts:properties-fragment-query xmlns:cts="http://marklogic.com/cts">
    <cts:element-range-query operator="&gt;">
      <cts:element xmlns:prop="http://marklogic.com/xdmp/property">prop:last-modified</cts:element>
      <cts:value xsi:type="xs:dateTime" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">2021-05-21T13:25:24.315485Z</cts:value>
    </cts:element-range-query>
  </cts:properties-fragment-query>

         */

        DataMovementManager dmm = client.newDataMovementManager();
        QueryBatcher batcher = dmm.newQueryBatcher(sqd);

        batcher.withThreadCount(1).onUrisReady(batch -> {
                    for (String uri : batch.getItems()) {
                        LOG.debug("URI: " + uri);
                    }
                }
        ).onQueryFailure(e -> LOG.error("Failure processing batch: ", e));
        // *** Step 4: Submit the DMSDK job ***
        dmm.startJob(batcher);
        // Wait for the job to complete, and then stop it.
        batcher.awaitCompletion();
        dmm.stopJob(batcher);
        // fixme - just keeping this artificial sleep here to stop the log being filled with poll log messages
        Thread.sleep(10000);
        return null;
    }

    @Override
    public void stop() {
        LOG.info("MarkLogicSourceTask: stop() called");
    }

    public String version() {
        return MarkLogicSource.MARKLOGIC_CONNECTOR_VERSION;
    }
}
