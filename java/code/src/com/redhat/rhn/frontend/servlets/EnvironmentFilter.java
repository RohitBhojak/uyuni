/*
 * Copyright (c) 2024 SUSE LLC
 * Copyright (c) 2009--2013 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.frontend.servlets;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.frontend.struts.StrutsDelegate;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import java.io.IOException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * EnvironmentFilter
 */
public class EnvironmentFilter implements Filter {

    private static final Logger LOGGER = LogManager.getLogger(EnvironmentFilter.class);

    private static final List<String> NO_SSL_URL = List.of(
        "/rhn/kickstart/DownloadFile",
        "/rhn/common/DownloadFile",
        "/rhn/rpc/api",
        "/rhn/errors",
        "/rhn/ty/TinyUrl",
        "/rhn/websocket",
        "/rhn/metrics"
    );

    @Override
    public void init(FilterConfig arg0) {
        // Not needed in this filter
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
        throws IOException, ServletException {

        HttpServletRequest hreq = new
                             RhnHttpServletRequest((HttpServletRequest)request);
        HttpServletResponse hres = new RhnHttpServletResponse(
                                                (HttpServletResponse)response,
                                                hreq);

        // There are a list of pages that don't require SSL, that list should
        // be called out here.
        String path = hreq.getRequestURI();
        // Have to make this decision here, because once we pass the request
        // off to the next filter, that filter can do work that sends data to
        // the client, meaning that we can't redirect.
        if (ConfigDefaults.get().isSsl() && RhnHelper.pathNeedsSecurity(NO_SSL_URL, path) && !hreq.isSecure()) {
            LOGGER.debug("redirecting to secure: {}", path);
            redirectToSecure(hreq, hres);
            return;
        }

        // Set request attributes we may need later
        HttpServletRequest req = (HttpServletRequest) request;
        request.setAttribute(RequestContext.REQUESTED_URI, req.getRequestURI());

        LOGGER.debug("set REQUESTED_URI: {}", req.getRequestURI());

        // add messages that were put on the request path.
        addParameterizedMessages(req);

        // Done, go up chain
        chain.doFilter(hreq, hres);
    }

    private void addParameterizedMessages(HttpServletRequest req) {
        String messageKey = req.getParameter("message");
        if (messageKey != null) {
            ActionMessages msg = new ActionMessages();
            String param1 = req.getParameter("messagep1");
            String param2 = req.getParameter("messagep2");
            String param3 = req.getParameter("messagep3");

            Object[] args = new Object[3];
            args[0] = StringEscapeUtils.escapeHtml4(param1);
            args[1] = StringEscapeUtils.escapeHtml4(param2);
            args[2] = StringEscapeUtils.escapeHtml4(param3);

            msg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(messageKey, args));
            StrutsDelegate.getInstance().saveMessages(req, msg);
        }
    }

    @Override
    public void destroy() {
      // Nothing to do here
    }

    private void redirectToSecure(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        String originalUrl = request.getRequestURL().toString();
        String secureUrl = "https://" + originalUrl.substring(7);
        response.sendRedirect(secureUrl);
    }
}
