package dev.ajaffie.dootr.doots.services;

import dev.ajaffie.dootr.doots.domain.Doot;
import dev.ajaffie.dootr.doots.domain.Migrations;
import dev.ajaffie.dootr.doots.domain.QueryDto;
import dev.ajaffie.dootr.doots.domain.User;
import io.vertx.axle.mysqlclient.MySQLPool;
import io.vertx.axle.sqlclient.Row;
import org.jooq.*;
import org.jooq.conf.ParamType;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

@ApplicationScoped
public class SearchServiceImpl implements SearchService {
    private MySQLPool client;
    private Logger logger;
    private DootService dootService;

    @Inject
    public SearchServiceImpl(MySQLPool pool, DootService dootService) {
        this.logger = LoggerFactory.getLogger(SearchServiceImpl.class);
        this.client = pool;
        this.dootService = dootService;
    }

    @PostConstruct
    public void setup() {
        Migrations.runMigrations(client, CassandraClusterFactory.getSession());
    }

    @Override
    public CompletionStage<Collection<Doot>> search(QueryDto query, User user) {
        if (query == null) {
            query = new QueryDto();
        }
        logger.info("Running query with ts {} following {} username {} q {}...", query.timestamp, query.following, query.username, query.query);
        Select builder = DSL.using(SQLDialect.MARIADB)
                .select()
                .from("Doots d");
        Condition condition = DSL.condition("d.`Timestamp` < ?", query.timestamp);
        if (query.username != null) {
            condition = condition.and("d.Username = ?", query.username);
        }
        if (query.following && user != null) {
            condition = condition.and("d.Username IN (SELECT FollowedName FROM Follows WHERE FollowerName = ?)", user.username);
        }
        if (query.hasMedia) {
            condition = condition.and("d.Id IN (SELECT DootId FROM Media)");
        }
        if (query.replies && query.parent != null) {
            condition = condition.and("d.Parent = ?", query.parent);
        }
        if (query.query != null) {
            condition = condition.and("MATCH(d.Content) AGAINST(?)", query.query);
        }
        builder = ((SelectFromStep) builder).where(condition);
        if (query.rank.equals("time")) {
            builder = ((SelectWhereStep) builder).orderBy(DSL.field("d.Timestamp").desc());
        } else {
            builder = ((SelectWhereStep) builder).orderBy(DSL.field("((SELECT COUNT(*) FROM Likes WHERE DootId = d.Id) + (SELECT COUNT(*) FROM Doots d2 WHERE d.Id = d2.Parent AND d2.ChildType = 'retweet'))").desc());
        }
        builder = ((SelectOrderByStep) builder).limit(query.limit);
        String sql = builder.getSQL(ParamType.INLINED);
        logger.info("Generated SQL for query: {}", sql);

        return client.query(sql)
                .thenApplyAsync(rs -> {
                    List<Doot> doots = new ArrayList<>(rs.size());

                    for (Row r : rs) {
                        doots.add((Doot.from(r)));
                    }

                    return doots.stream()
                            .map(dootService::addLikes)
                            .map(cs -> cs.thenCompose(dootService::addRetweets))
                            .map(cs -> cs.thenCompose(dootService::addMedia).toCompletableFuture())
                            .map(CompletableFuture::join)
                            .collect(Collectors.toList());
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
