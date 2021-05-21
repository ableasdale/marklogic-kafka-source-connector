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

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;

import java.util.Map;

public class MarkLogicSourceConfig extends AbstractConfig {

	public static final String CONNECTION_HOST = "ml.connection.host";
	private static final String CONNECTION_HOST_DOC = "MarkLogic hostname (use marklogic if you're using Docker)";
	
	public static final String CONNECTION_PORT = "ml.connection.port";
    private static final String CONNECTION_PORT_DOC = "MarkLogic Application Server port";
	
	public static final String CONNECTION_USER = "ml.connection.user";
	private static final String CONNECTION_USER_DOC = "MarkLogic connection user.";

	public static final String CONNECTION_PASSWORD = "ml.connection.password";
	private static final String CONNECTION_PASSWORD_DOC = "MarkLogic connection password";

	public static final String BATCH_SIZE = "ml.batch.size";
	private static final int BATCH_SIZE_DEFAULT = 1000;
	private static final String BATCH_SIZE_DOC = "MarkLogic batch size";

	public static final String MAX_RETRIES = "max.retries";
    private static final int MAX_RETRIES_DEFAULT = 100;
    private static final String MAX_RETRIES_DOC =  "The maximum number of times to retry on errors/exception before failing the task.";
		
	public static final String RETRY_BACKOFF_MS = "retry.backoff.ms";
    private static final int RETRY_BACKOFF_MS_DEFAULT = 10000;
	private static final String RETRY_BACKOFF_MS_DOC = "The time in milliseconds to wait following an error/exception before a retry attempt is made.";

	public static final String TOPIC_CONFIG = "topic";
	private static final String TOPIC_CONFIG_DEFAULT = "marklogic";
	private static final String TOPIC_CONFIG_DOC = "Kafka topic to publish source MarkLogic data to";

	public static ConfigDef CONFIG_DEF = new ConfigDef()
			// Essential configuration
			.define(CONNECTION_HOST, Type.STRING, Importance.HIGH, CONNECTION_HOST_DOC)
			.define(CONNECTION_PORT, Type.INT, Importance.HIGH, CONNECTION_PORT_DOC)
			.define(CONNECTION_USER, Type.STRING, Importance.HIGH, CONNECTION_USER_DOC)
			.define(CONNECTION_PASSWORD, Type.STRING, Importance.HIGH, CONNECTION_PASSWORD_DOC)
			// Has defaults
			.define(BATCH_SIZE, Type.INT, BATCH_SIZE_DEFAULT, Importance.MEDIUM, BATCH_SIZE_DOC)
			.define(MAX_RETRIES, Type.INT, MAX_RETRIES_DEFAULT, Importance.MEDIUM, MAX_RETRIES_DOC)
			.define(RETRY_BACKOFF_MS, Type.INT, RETRY_BACKOFF_MS_DEFAULT, Importance.MEDIUM, RETRY_BACKOFF_MS_DOC)
			.define(TOPIC_CONFIG, Type.LIST, TOPIC_CONFIG_DEFAULT, Importance.MEDIUM, TOPIC_CONFIG_DOC);

	public MarkLogicSourceConfig(final Map<?, ?> defaults) {
		super(CONFIG_DEF, defaults, true);
	}

}
