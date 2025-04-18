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
package com.redhat.rhn.frontend.xmlrpc.org.trusts.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.access.AccessGroupFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.ChannelTreeNode;
import com.redhat.rhn.frontend.dto.OrgTrustOverview;
import com.redhat.rhn.frontend.dto.TrustedOrgDto;
import com.redhat.rhn.frontend.xmlrpc.org.OrgHandler;
import com.redhat.rhn.frontend.xmlrpc.org.trusts.OrgTrustHandler;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;
import com.redhat.rhn.manager.org.MigrationManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.webui.services.test.TestSaltApi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * OrgTrustHandlerTest
 */
public class OrgTrustHandlerTest extends BaseHandlerTestCase {

    private OrgTrustHandler handler = new OrgTrustHandler();
    private OrgHandler orgHandler = new OrgHandler(new MigrationManager(new ServerGroupManager(new TestSaltApi())));

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        admin.addPermanentRole(RoleFactory.SAT_ADMIN);
        TestUtils.saveAndFlush(admin);
    }

    @Test
    public void testOrgTrusts() throws Exception {
        Org org2 = createOrg();
        Org org3 = createOrg();

        handler.addTrust(
                admin,
                org2.getId().intValue(),
                org3.getId().intValue());
        assertTrue(isTrusted(org2, org3));
        handler.removeTrust(
                admin,
                org2.getId().intValue(),
                org3.getId().intValue());
        assertFalse(isTrusted(org2, org3));
    }

    @Test
    public void testListOrgs() throws Exception {
        // setup
        Channel channel = ChannelFactoryTest.createTestChannel(admin);
        admin.getOrg().addOwnedChannel(channel);

        Org org2 = createOrg();
        Org org3 = createOrg();

        org2.addTrust(admin.getOrg());
        org3.addTrust(admin.getOrg());

        channel.getTrustedOrgs().add(org3);

        OrgFactory.save(admin.getOrg());
        ChannelFactory.save(channel);
        flushAndEvict(channel);

        // execute
        Object[] result = handler.listOrgs(admin);

        // verify
        assertNotNull(result);
        assertTrue(result.length >= 2);

        boolean foundOrg2 = false, foundOrg3 = false;
        for (Object oIn : result) {
            TrustedOrgDto item = (TrustedOrgDto) oIn;
            if (item.getName().equals(org2.getName())) {
                assertNotNull(item.getSharedChannels());
                foundOrg2 = true;
            }
            if (item.getName().equals(org3.getName())) {
                assertNotNull(item.getSharedChannels());
                foundOrg3 = true;
            }
        }
        assertTrue(foundOrg2);
        assertTrue(foundOrg3);
    }

    @Test
    public void testListChannelsProvided() throws Exception {
        // setup
        Channel channel = ChannelFactoryTest.createTestChannel(admin);

        Org org2 = createOrg();
        org2.addOwnedChannel(channel);
        org2.addTrust(admin.getOrg());
        channel.getTrustedOrgs().add(admin.getOrg());
        channel.setAccess(Channel.PROTECTED);

        OrgFactory.save(admin.getOrg());
        ChannelFactory.save(channel);
        flushAndEvict(channel);



        // execute
        Object[] result = handler.listChannelsProvided(admin, org2.getId().intValue());

        // verify
        assertNotNull(result);
        assertTrue(result.length >= 1);

        boolean foundChannel = false;
        for (Object oIn : result) {
            ChannelTreeNode item = (ChannelTreeNode) oIn;
            if (item.getName().equals(channel.getName())) {
                foundChannel = true;
            }
        }
        assertTrue(foundChannel);
    }

    @Test
    public void testListChannelsConsumed() throws Exception {
        // setup
        Channel channel = ChannelFactoryTest.createTestChannel(admin);
        admin.getOrg().addOwnedChannel(channel);

        Org org2 = createOrg();
        org2.addTrust(admin.getOrg());
        channel.getTrustedOrgs().add(org2);
        channel.setAccess(Channel.PROTECTED);

        OrgFactory.save(admin.getOrg());
        ChannelFactory.save(channel);
        flushAndEvict(channel);

        // execute
        Object[] result = handler.listChannelsConsumed(admin, org2.getId().intValue());

        // verify
        assertNotNull(result);
        assertTrue(result.length >= 1);

        boolean foundChannel = false;
        for (Object oIn : result) {
            ChannelTreeNode item = (ChannelTreeNode) oIn;
            if (item.getName().equals(channel.getName())) {
                foundChannel = true;
            }
        }
        assertTrue(foundChannel);
    }

    @Test
    public void testGetDetails() throws Exception {
        // setup
        Channel channel = ChannelFactoryTest.createTestChannel(admin);
        admin.getOrg().addOwnedChannel(channel);

        Org org2 = createOrg();
        org2.addTrust(admin.getOrg());
        channel.getTrustedOrgs().add(org2);

        OrgFactory.save(admin.getOrg());
        ChannelFactory.save(channel);
        flushAndEvict(channel);

        // execute
        Map<String, Object> result = handler.getDetails(admin, org2.getId().intValue());

        // verify
        assertNotNull(result);
        assertTrue(result.containsKey("created"));
        assertTrue(result.containsKey("trusted_since"));
        assertTrue(result.containsKey("channels_provided"));
        assertTrue(result.containsKey("channels_consumed"));
        assertTrue(result.containsKey("systems_transferred_to"));
        assertTrue(result.containsKey("systems_transferred_from"));
    }

    private Org createOrg() {
        String random = TestUtils.randomString();
        String orgName = "EdwardNortonOrg" + random;
        String login = "edward" + random;
        String password = "redhat";
        String prefix = "Mr.";
        String first = "Edward";
        String last = "Norton";
        String email = "EddieNorton@redhat.com";
        Boolean usePam = Boolean.FALSE;

        orgHandler.create(admin, orgName, login, password, prefix, first,
                last, email, usePam);

        Org org =  OrgFactory.lookupByName(orgName);
        assertNotNull(org);
        return org;
    }

    @Test
    public void testBaseTrusts() throws Exception {
        Org org1 = createOrg();
        Org org2 = createOrg();
        handler.addTrust(
                admin,
                org1.getId().intValue(),
                org2.getId().intValue());
        assertTrue(isTrusted(org1, org2));
        handler.removeTrust(
                admin,
                org1.getId().intValue(),
                org2.getId().intValue());
        assertFalse(isTrusted(org1, org2));
    }

    @Test
    public void testListAffectedSystems() throws Exception {
        Channel c = ChannelFactoryTest.createTestChannel(admin);
        c.setAccess("public");
        Org orgA = c.getOrg();
        Org orgB = createOrg();
        User userB = UserTestUtils.createUser("Johnny Quest", orgB.getId());
        handler.addTrust(
                admin,
                orgA.getId().intValue(),
                orgB.getId().intValue());
        Server s = ServerFactoryTest.createTestServer(userB);
        SystemManager.subscribeServerToChannel(userB, s, c);
        flushAndEvict(c);
        flushAndEvict(s);
        addAccessGroup(admin, AccessGroupFactory.CHANNEL_ADMIN);
        Package pkg = PackageTest.createTestPackage(orgA);
        List<Long> packages = new ArrayList<>();
        packages.add(pkg.getId());
        List<Map<String, Object>> affected =
            handler.listSystemsAffected(
                    admin, orgA.getId().intValue(),
                    orgB.getId().intValue());
        boolean found = false;
        for (Map<String, Object> m : affected) {
            if (m.get("systemId").equals(s.getId())) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    private boolean isTrusted(Org org, Org trusted) {
        List<OrgTrustOverview> trusts =
                handler.listTrusts(admin, org.getId().intValue());
        for (OrgTrustOverview t : trusts) {
            if (t.getId().equals(trusted.getId()) && t.getTrusted()) {
                return true;
            }
        }
        return false;
    }
}
