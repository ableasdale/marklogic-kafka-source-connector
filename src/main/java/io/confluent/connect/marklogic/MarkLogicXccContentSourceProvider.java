package io.confluent.connect.marklogic;

import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.ContentSourceFactory;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.exceptions.XccConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.net.URI;

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
    private static String uri;

    private MarkLogicXccContentSourceProvider() {
        try {
            if (uri == null) {
                LOG.info("Creating the MarkLogic ContentSource using container-specific defaults (running in test mode)");
                generateXdbcConnectionUri(MarkLogicSourceConfig.CONNECTION_USER_DEFAULT, MarkLogicSourceConfig.CONNECTION_PASSWORD_DEFAULT, MarkLogicSourceConfig.CONNECTION_HOST_DEFAULT, MarkLogicSourceConfig.CONNECTION_PORT_DEFAULT);
            }
            contentSource = ContentSourceFactory.newContentSource(URI.create(uri));
        } catch (XccConfigException e) {
            LOG.error("Exception encountered: ", e);
        }

    }

    protected static void generateXdbcConnectionUri(String user, String pass, String host, String port) {
        LOG.info("uri is currently set to: "+uri);
        StringBuilder sb = new StringBuilder();
        sb.append("xcc://").append(user).append(":")
                .append(pass).append("@")
                .append(host).append(":")
                .append(port);
        LOG.debug(String.format("Conn: %s", sb));
        LOG.info("uri will now be set to: "+sb);
        uri = sb.toString();
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