package dev.ajaffie.dootr.doots.services;


import com.datastax.oss.driver.api.core.CqlSession;

import javax.inject.Singleton;
import java.net.InetSocketAddress;

@Singleton
public class CassandraClusterFactory {
    private static CqlSession session;

    public static CqlSession getSession() {
        if (session == null) {
            session = CqlSession.builder()
                    .addContactPoint(new InetSocketAddress("cassandra-0.cassandra.default.svc.cluster.local", 9042))
                    .addContactPoint(new InetSocketAddress("cassandra-1.cassandra.default.svc.cluster.local", 9042))
                    .withLocalDatacenter("datacenter1")
                    .build();
        }
        return session;
    }
}
