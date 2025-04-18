/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.frontend.configuration.tags;

import com.redhat.rhn.frontend.taglibs.IconTag;

import org.apache.commons.text.StringEscapeUtils;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * ConfigChannelTag
 * <p>
 * {@literal <cfg:channel id="" value="" type="" size="large|small" nolink="">}
 *
 */
public class ConfigChannelTag extends TagSupport {

    private String name;
    private String type;
    private boolean nolink;

    public static final String CHANNEL_URL =
                                        "/rhn/configuration/ChannelOverview.do";

    /**
     *
     * Comment for {@literal <code>serialVersionUID</code>}
     */
    private static final long serialVersionUID = -8093937572650589326L;

    /**
     * {@inheritDoc}
     */
    @Override
    public int doEndTag() throws JspException {
        StringBuilder result = new StringBuilder();
        if (nolink || id == null) {
            result.append(writeIcon());
            result.append(name);
        }
        else {
            result.append("<a href=\"" +
                        ConfigChannelTag.makeConfigChannelUrl(id) + "\">");
            result.append(writeIcon());
            result.append(StringEscapeUtils.escapeXml10(name) + "</a>");
        }
        JspWriter writer = pageContext.getOut();
        try {
            writer.write(result.toString());
        }
        catch (IOException e) {
            throw new JspException(e);
        }
        return Tag.SKIP_BODY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void release() {
        id = null;
        name = null;
        type = null;
        nolink = false;
        super.release();
    }


    /**
     * @param val the id to set
     */
    @Override
    public void setId(String val) {
        this.id = val;
    }


    /**
     * @param isNoLink the nolink to set
     */
    public void setNolink(String isNoLink) {
        this.nolink = Boolean.TRUE.toString().equalsIgnoreCase(isNoLink);
    }


    /**
     * @param tp the type to set
     */
    public void setType(String tp) {
        this.type = tp;

    }

    /**
     * @param value the value to set
     */
    public void setName(String value) {
        this.name = value;
    }

    private String writeIcon() {
        IconTag i = new IconTag();
        if (type.equals("central") || type.equals("global")) {
            i.setType("header-channel-configuration");
        }
        else if (type.equals("local_override")) {
            i.setType("header-system");
        }
        else {
            i.setType("header-sandbox");
        }
        String result = i.renderStartTag();
        i.release();
        return result;
    }

    /**
     * Returns the URL to view a config channel
     * This method may also be used with the el expression
     * ${cfg:channelUrl(ccid)}
     * This method is public static because
     * EL functions defined in a TLD file,
     * need to be public static methods..
     * @param ccId the id of the given Config Channel
     * @return the URL to view a config channel
     */
    public static String makeConfigChannelUrl(String ccId) {
        return CHANNEL_URL + "?ccid=" + ccId;
    }
}
