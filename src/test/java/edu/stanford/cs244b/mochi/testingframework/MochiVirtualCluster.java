package edu.stanford.cs244b.mochi.testingframework;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;

import edu.stanford.cs244b.mochi.server.messaging.MochiServer;
import edu.stanford.cs244b.mochi.server.messaging.Server;

/* 
 Represents virual cluster which contains of different clients and servers, 
 all run on the same JVM. The primarily goal of that - ease of testing
 */
public class MochiVirtualCluster implements Closeable {
    private final int initialPort = 8001;
    private volatile int nextPort = initialPort;

    private final List<VirtualServer> servers;
    
    public MochiVirtualCluster(int initialNumberOfServers) {
        Assert.assertTrue(initialNumberOfServers > 0);
        servers = new ArrayList<VirtualServer>(initialNumberOfServers * 2);
        for (int i = 0 ; i < initialNumberOfServers; i++) {
            final MochiServer ms = newMochiServer(nextPort);
            nextPort += 1;
            servers.add(new VirtualServer(ms));
        }
    }

    private MochiServer newMochiServer(final int serverPort) {
        return new MochiServer(serverPort, new InMemoryDSMochiContextImpl());
    }

    public void startAllServers() {
        for (final VirtualServer vs : servers) {
            final MochiServer s = vs.getServer();
            s.start();
        }
    }

    public List<Server> getAllServers() {
        final List<Server> mServers = new ArrayList<Server>(servers.size());
        for (final VirtualServer vs : servers) {
            final MochiServer s = vs.getServer();
            mServers.add(s.toServer());
        }
        return mServers;
    }

    @Override
    public void close() {
        for (final VirtualServer vs : servers) {
            final MochiServer s = vs.getServer();
            s.close();
        }
    }
}
