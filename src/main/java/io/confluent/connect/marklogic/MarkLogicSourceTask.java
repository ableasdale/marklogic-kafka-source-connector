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
import java.util.List;
import java.util.Map;

public class MarkLogicSourceTask extends SourceTask {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    protected DatabaseClient client;
    private int timeout;
    private Map<String, String> config;
    private int maxRetires;
    private int remainingRetries;
    private int batchSize;
    //private BufferedRecords bufferedRecords;
    private DataMovementManager dataMovementManager;

    @Override
    public void start(final Map<String, String> config) {
        LOG.info("***********************************************");
        LOG.info("***  MarkLogicSourceTask - start() called   ***");
        LOG.info("***********************************************");

        // Second simple probe - Can an XDBC Session be created and the MarkLogic Timestamp be returned?
        try {
            ContentSource cs = ContentSourceFactory.newContentSource(URI.create("xcc://admin:admin@marklogic:8000/Meters"));
            Session s = cs.newSession();
            LOG.info("*** MarkLogicSourceTask: current MarkLogic Timestamp: " + s.getCurrentServerPointInTime());
        } catch (RequestException | XccConfigException e) {
            LOG.info("MarkLogicSourceTask: Exception Caught: ", e);
        }

        // Third simple probe - Can the Java Client API be used to connect to MarkLogic?
        client = DatabaseClientFactory.newClient("marklogic", 8000, "Meters",
                new DatabaseClientFactory.DigestAuthContext("admin", "admin"));

        LOG.info("*** MARKLOGIC SOURCE CONNECTOR :: Client created: " + client.getDatabase());

        LOG.info("*** MarkLogicSourceTask :: Does this still die now or do we see this message get logged? ***");

        // host.docker.internal

        // Test: Generate a full and-query (to get every URI) and log each
        StructuredQueryDefinition sqd = new StructuredQueryBuilder().and();

        DataMovementManager dmm = client.newDataMovementManager();
        QueryBatcher batcher = dmm.newQueryBatcher(sqd);
        batcher.onUrisReady(batch -> {
                    for (String uri : batch.getItems()) {
                        LOG.info("URI: " + uri);
                    }
                }
        )
                .onQueryFailure(exception -> exception.printStackTrace());
        // *** Step 4: Submit the DMSDK job ***
        dmm.startJob(batcher);
        // Wait for the job to complete, and then stop it.
        batcher.awaitCompletion();
        dmm.stopJob(batcher);

    }

    @Override
    public List<SourceRecord> poll() throws InterruptedException {
        LOG.info("*** MarkLogicSourceTask - calling poll ***");
        // fixme - just keeping this artifical sleep here to stop the log being filled with poll log messages
        Thread.sleep(10000);
        return null;
    }

    @Override
    public void stop() {
        LOG.info("*** MarkLogicSourceTask - stop called ***");
    }

    public String version() {
        return MarkLogicSource.MARKLOGIC_CONNECTOR_VERSION;
    }
}
