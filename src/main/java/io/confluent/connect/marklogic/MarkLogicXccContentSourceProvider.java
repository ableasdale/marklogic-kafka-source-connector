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
            URI uri = new URI(generateXdbcConnectionUri("admin", "admin", "localhost", 8000));
            contentSource = ContentSourceFactory
                    .newContentSource(uri);
        } catch (URISyntaxException e) {
            LOG.error("Exception",e);
        } catch (XccConfigException e) {
            LOG.error("Exception",e);
        }

    }

    private String generateXdbcConnectionUri(String user, String pass, String host, int port) {
        StringBuilder sb = new StringBuilder();
        sb.append("xdbc://").append(user).append(":")
                .append(pass).append("@")
                .append(host).append(":")
                .append(port);
        LOG.info("Conn: " + sb.toString());
        return sb.toString();
    }

    private static class MarkLogicContentSourceProviderHolder {
        private static final MarkLogicXccContentSourceProvider INSTANCE = new MarkLogicXccContentSourceProvider();
    }

    private static MarkLogicXccContentSourceProvider getInstance() {
        return MarkLogicContentSourceProviderHolder.INSTANCE;
    }

    public static ContentSource getContentSource() {
        return MarkLogicContentSourceProviderHolder.INSTANCE.contentSource;
    }

    public static Session getSession(String name) {
        return MarkLogicContentSourceProviderHolder.INSTANCE.contentSource.newSession(name);
    }
}