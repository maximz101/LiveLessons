package edu.vandy.quoteservices.client;

import edu.vandy.quoteservices.common.Quote;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Flux;

import java.util.List;

import static edu.vandy.quoteservices.common.Constants.EndPoint.*;
import static edu.vandy.quoteservices.common.Constants.Service.ZIPPY;

/**
 * This interface provides the contract for the RESTful {@code
 * ZippyController} microservice used in conjunction with the {@code
 * GatewayApplication}.  It defines the HTTP GET and POST methods that
 * can be used to interact with the {@code ZippyController} API,
 * along with the expected request and response parameters for each
 * method.  However, since clients access the {@code ZippyController}
 * API via the {@code GatewayApplication} it's necessary to add a
 * {@code HANDEY} prefix to each URL mapping via the {@code
 * HttpExchange} annotation.
 * 
 * This interface uses Spring HTTP interface annotations that provide
 * metadata about the API, such as the type of HTTP request (i.e.,
 * {@code GET} or {@code POST}), the parameter types (which are
 * annotated with {@code GetExchange}, {@code PostExchange},
 * {@code @RequestPath}, {@code RequestBody}, or {@code RequestParam}
 * tags), and the expected response format.  The HTTP interface
 * framework uses these annotations and method signatures to generate
 * an implementation of the interface that the client uses to make
 * asynchronous HTTP requests to the API.
 */
@HttpExchange(url = ZIPPY + "/")
public interface ZippyQuoteAPI {
    /**
     * Get a {@link Flux} that emits the requested quotes.
     *
     * @return An {@link Flux} that emits all the {@link Quote}
     *         objects
     */
    @GetExchange(GET_ALL_QUOTES)
    Flux<Quote> getAllQuotes();

    /**
     * Get a {@link List} containing the requested quotes.
     *
     * @param quoteIds A {@link List} containing the given random
     *                 {@code quoteIds}
     * @return An {@link Flux} that emits the requested {@link Quote}
     *         objects
     */
    @PostExchange(POST_QUOTES)
    Flux<Quote> postQuotes(@RequestBody List<Integer> quoteIds);

    /**
     * Search for quotes containing any of the given {@link List} of
     * {@code queries}.
     *
     * @param queries The {@link List} of {@code queries} to search
     *                for
     * @return A {@link Flux} that emits {@link Quote} objects
     *         matching the queries
     */
    @PostExchange(POST_SEARCHES)
    Flux<Quote> search(@RequestBody List<String> queries);

    /**
     * Search the Zippy microservice for quotes containing all the
     * given {@link List} of {@code queries} using a custom SQL
     * method.
     *
     * @param queries The {@link List} of {@code queries} to search
     *                for
     * @return A {@link Flux} that emits {@link Quote} objects
     *         matching the queries
     */
    @PostExchange(POST_SEARCHES_EX)
    Flux<Quote> searchEx(@RequestBody List<String> queries);
}
