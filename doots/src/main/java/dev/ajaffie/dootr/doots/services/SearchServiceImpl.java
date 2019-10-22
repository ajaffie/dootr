package dev.ajaffie.dootr.doots.services;

import dev.ajaffie.dootr.doots.domain.Doot;
import dev.ajaffie.dootr.doots.domain.Migrations;
import dev.ajaffie.dootr.doots.domain.QueryDto;
import io.vertx.axle.mysqlclient.MySQLPool;
import io.vertx.axle.sqlclient.Row;
import io.vertx.axle.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class SearchServiceImpl implements SearchService {
    private MySQLPool client;
    private Logger logger;

    @Inject
    public SearchServiceImpl(MySQLPool pool) {
        this.logger = LoggerFactory.getLogger(SearchServiceImpl.class);
        this.client = pool;
    }

    @PostConstruct
    public void setup() {
        Migrations.runMigrations(client);
    }

    @Override
    public CompletionStage<Collection<Doot>> search(QueryDto query) {
        return client.preparedQuery(
                "SELECT Id, Username, UserId, Likes, Retweeted, Content, `Timestamp`\n" +
                        "FROM Doots\n" +
                        "WHERE `Timestamp` < ?\n" +
                        "ORDER BY `Timestamp` DESC\n" +
                        "LIMIT ?",
                Tuple.of(query.timestamp, query.limit)
        )
                .thenApply(rs -> {
                    List<Doot> doots = new ArrayList<>(rs.size());
                    for (Row r : rs) {
                        doots.add(Doot.from(r));
                    }
                    return doots;
                })
                .handle((doots, err) -> {
                    if (err != null) {
                        logger.error("There was an error searching for doots: {}", err.getMessage());
                        return null;
                    }
                    return doots;
                });
    }
}
