package edu.vandy.quoteservices.microservice;

import edu.vandy.quoteservices.common.Quote;
import edu.vandy.quoteservices.repository.JPAQuoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

/**
 * This class defines implementation methods that are called by the
 * {@link ZippyController}, which serves as the main "front-end" app
 * gateway entry point for remote clients that want to receive Zippy
 * quotes.
 * <p>
 * This class is annotated as a Spring {@code @Service}, which enables
 * the automatic detection and wiring of dependent implementation
 * classes via classpath scanning. It also includes its name in the
 * {@code @Service} annotation below so that it can be identified as a
 * service.
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Service
public class ZippyService {
    /**
     * Spring-injected repository that contains all quotes.
     */
    @Autowired
    private JPAQuoteRepository mRepository;

    /**
     * @return A {@link Flux} that emits all {@link Quote} objects
     */
    public Flux<Quote> getAllQuotes() {
        return Flux
            // Convert List to a Flux.
            .fromIterable(mRepository
                // Forward to the repository.
                .findAll());
    }

    /**
     * Get a {@link List} that contains the requested quotes.
     *
     * @param quoteIds A {@link List} containing the given random
     *                 {@code quoteIds}
     * @return A {@link Flux} that emits all requested {@link Quote} objects
     */
    public Flux<Quote> postQuotes(List<Integer> quoteIds) {
        return Flux
            // Convert List to a Flux.
            .fromIterable(mRepository
                // Forward to the repository.
                .findAllById(quoteIds));
    }

    /**
     * Search for quotes containing any of the given {@link List} of
     * {@code queries} and return a {@link Flux} that emits matching
     * {@link Quote} objects.
     *
     * @param queries The search queries
     * @return A {@code Flux} that emits {@link Quote} objects
     * matching the given {@code queries}
     */
    @SuppressWarnings("BlockingMethodInNonBlockingContext")
    public Flux<Quote> search(List<String> queries) {
        // Use a Project Reactor ParallelFlux and the JPA to
        // locate all quotes whose 'quote' field matches the List
        // of 'queries' and return them as a Flux of Quote objects.
        System.out.println("search(List<String> queries");

        return Flux
            // Convert List to a Flux.
            .fromIterable(queries)

            // Convert Flux to a ParallelFlux.
            .parallel()

            // Run the computations in the BoundedElastic thread pool.
            .runOn(Schedulers.boundedElastic())

            // Flatten the Flux of Fluxes into a Flux.
            .flatMap(query -> Flux
                     // Convert the List to a Flux.
                     .fromIterable(mRepository
                                   // Find all Quote rows in the
                                   // database that match the 'query'.
                                   .findByQuoteContainingIgnoreCase(query)))

            // Convert ParallelFlux to Flux.
            .sequential()

            // Ensure duplicate Zippy quotes aren't returned.
            .distinct();
    }

    /**
     * Search for quotes containing all the given {@link String} and
     * return a {@link Flux} that emits the matching {@link Quote}
     * objects.
     *
     * @param queries The search queries
     * @return A {@code Flux} that emits {@link Quote} objects
     * containing the given {@code queries}
     */
    public Flux<Quote> searchEx(List<String> queries) {
        // Use the JPA to locate all quotes whose 'quote' field matches
        // the List of 'queries' and return them as a Flux of Quote
        // objects.
        return Flux
            // Convert the List to a Stream.
            .fromIterable(mRepository
                // Forward to the repository.
                .findAllByQuoteContainingIgnoreCaseAllIn(queries));
    }
}
