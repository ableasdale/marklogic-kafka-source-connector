package io.confluent.connect.marklogic;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URISyntaxException;

import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.ContentSourceFactory;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.exceptions.XccConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base Provider Class for a MarkLogic Content Source Factory - a singleton
 * provider which should only be instantiated once; each individual connection
 * to the XML Server should use this ContentSourceFactory
 * <p>
 * As this is a singleton, use getInstance() to access the class
 *
 * @author Alex Bleasdale
 */
public class MarkLogicXccContentSourceProvider {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    ContentSource contentSource;

    private MarkLogicXccContentSourceProvider() {
        LOG.debug("Creating the MarkLogic ContentSource...");
        try {
            // TODO - get the *configuration object* or use the defaults to remove these
            URI uri = new URI(generateXdbcConnectionUri(MarkLogicSourceConfig.CONNECTION_USER_DEFAULT, MarkLogicSourceConfig.CONNECTION_PASSWORD_DEFAULT, MarkLogicSourceConfig.CONNECTION_HOST_DEFAULT, MarkLogicSourceConfig.CONNECTION_PORT_DEFAULT));
            contentSource = ContentSourceFactory
                    .newContentSource(uri);
        } catch (URISyntaxException | XccConfigException e) {
            LOG.error("Exception encountered: ",e);
        }

    }

    private String generateXdbcConnectionUri(String user, String pass, String host, int port) {
        StringBuilder sb = new StringBuilder();
        sb.append("xcc://").append(user).append(":")
                .append(pass).append("@")
                .append(host).append(":")
                .append(port);
        LOG.debug(String.format("Conn: %s", sb));
        return sb.toString();
    }

    private static class MarkLogicContentSourceProviderHolder {
        private static final MarkLogicXccContentSourceProvider INSTANCE = new MarkLogicXccContentSourceProvider();
    }

    private static MarkLogicXccContentSourceProvider getInstance() {
        return MarkLogicContentSourceProviderHolder.INSTANCE;
    }

    private static ContentSource getContentSource() {
        return getInstance().contentSource;
    }

    public static Session getSession(String name) {
        return getContentSource().newSession(name);
    }
}