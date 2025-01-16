package io.dropwizard.request.logging;

import ch.qos.logback.access.jetty.JettyServerAdapter;
import ch.qos.logback.access.jetty.RequestLogImpl;
import ch.qos.logback.access.jetty.RequestWrapper;
import ch.qos.logback.access.jetty.ResponseWrapper;
import ch.qos.logback.access.common.spi.AccessEvent;
import ch.qos.logback.access.common.spi.IAccessEvent;
import ch.qos.logback.access.common.spi.ServerAdapter;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.spi.FilterReply;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;

import java.util.Iterator;

/**
 * The Dropwizard request log uses logback-access, but we override it to remove the requirement for logback-access.xml
 * based configuration.
 */
public class LogbackAccessRequestLog extends RequestLogImpl {
    @Override
    public void configure() {
        setName("LogbackAccessRequestLog");
    }

    @Override
    public void log(Request jettyRequest, Response jettyResponse) {
        HttpServletRequest httpServletRequest = new RequestWrapper(jettyRequest);
        HttpServletResponse httpServletResponse = new ResponseWrapper(jettyResponse);
        ServerAdapter adapter = new JettyServerAdapter(jettyRequest, jettyResponse);
        IAccessEvent accessEvent = new AccessEvent(this, httpServletRequest, httpServletResponse, adapter);
        if (getFilterChainDecision(accessEvent) == FilterReply.DENY) {
            return;
        }
        appendLoopOnAppenders(accessEvent);
    }

    private void appendLoopOnAppenders(IAccessEvent iAccessEvent) {
        Iterator<Appender<IAccessEvent>> appenderIterator = this.iteratorForAppenders();
        while (appenderIterator.hasNext()) {
            appenderIterator.next().doAppend(iAccessEvent);
        }
    }
}
