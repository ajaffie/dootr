package dev.ajaffie.dootr.doots.controllers;

import dev.ajaffie.dootr.doots.domain.ErrorDto;
import dev.ajaffie.dootr.doots.domain.OkWithItemListDto;
import dev.ajaffie.dootr.doots.domain.QueryDto;
import dev.ajaffie.dootr.doots.services.SearchService;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.concurrent.CompletionStage;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@ApplicationScoped
public class SearchController {
    private SearchService searchService;

    @Inject
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @POST
    @Path("/search")
    public CompletionStage<Response> search(@RequestBody QueryDto query) {
        return searchService.search(query)
                .thenApply(doots -> {
                    if (doots == null) {
                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                .entity(new ErrorDto("There was an error searching. Try again later.")).build();
                    }
                    return Response.ok(OkWithItemListDto.fromDoots(doots)).build();
                });
    }
}
