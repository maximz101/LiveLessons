package zippyisms.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import zippyisms.common.model.Subscription;
import zippyisms.common.model.Quote;

import java.time.Duration;
import java.util.UUID;

import static zippyisms.common.Constants.*;
import static zippyisms.utils.RandomUtils.getRandomIntegers;

/**
 * This class provides a client whose methods can be used to send
 * messages to endpoints provided by the ZippyApplication microservice
 * that demonstrates each of the four interaction models supported by
 * RSocket.
 * <p>
 * The {@code @Component} annotation allows Spring to automatically
 * detect custom beans, i.e., Spring will scan the application for
 * classes annotated with {@code @Component}, instantiate them, and
 * inject the specified dependencies into them without having to write
 * any explicit code.
 */
@Component
public class ZippyProxy {
    /**
     * This object connects to the Spring controller running the
     * RSocket server and its associated endpoints.
     *
     * The {@code @Autowired} annotation marks this field to be
     * initialized via Spring's dependency injection facilities, where
     * an object receives other objects that it depends on (in this
     * case, by creating a connected {@link RSocketRequester}).
     */
    @Autowired
    private Mono<RSocketRequester> mZippyQuoteRequester;

    /**
     * @return A {@link Mono} that emits the total number of Zippy
     *         th' Pinhead quotes
     */
    public Mono<Integer> getQuoteMax() {
        // Return an array of random indicates.
        return mZippyQuoteRequester
            // Initialize the request that will be sent to the server.
            .map(r -> r
                // Set the metadata to indicate the request is for
                // the server's GET_NUMBER_OF_QUOTES endpoint.
                .route(GET_NUMBER_OF_QUOTES))

            // Perform a two-way call using the metadata and then
            // convert the response to a Mono that emits the total
            // number of Zippyisms.
            .flatMap(r -> r
                .retrieveMono(Integer.class));
    }

    /**
     * This factory method returns an array of random indices that are
     * then used to generate random Zippy th' Pinhead quotes.
     *
     * @param numberOfIndices The number of random indices to generate
     * @return A {@link Mono} that emits an array of random indices
     *         within the range of the Zippy quotes
     */
    public Mono<Integer[]> makeRandomIndices(int numberOfIndices) {
        return this
            // Get the max number of Zippy quotes.
            .getQuoteMax()

            // Create an Integer array containing random indices.
            .map(numberOfZippyQuotes ->
                getRandomIntegers(numberOfIndices,
                    numberOfZippyQuotes));
    }

    /**
     * This method returns a Flux that emits random Zippy th' Pinhead
     * quotes once a second until the stream is complete.
     *
     * @param randomIndices A {@link Mono} that emits an array of
     *                      random indices used to request the associated Zippy
     *                      quote
     * @return A {@link Flux} that emits random Zippy quotes once a
     *         second until the stream is complete
     */
    @SuppressWarnings("ReactiveStreamsUnusedPublisher")
    public Flux<Quote> getRandomQuotes
        (Mono<Integer[]> randomIndices) {
        // Return a Flux that emits random Zippy quotes.
        return mZippyQuoteRequester
            // Wait for both Monos to emit one element and combine
            // these elements once into a Tuple2 object.
            .zipWith(randomIndices)

            // Initialize the request that will be sent to the server.
            .map(tuple ->
                // Set the metadata to indicate the request is for
                // the server's GET_QUOTE endpoint.
                tuple.getT1().route(GET_QUOTES)

                    // Create the param to pass to the GET_QUOTE
                    // endpoint.
                    .data(Flux
                        // Create a Flux that emits indices for random
                        // Zippy th' Pinhead quotes.
                        .fromArray(tuple.getT2())

                        // Emit the indices once every second.
                        .delayElements(Duration.ofSeconds(1))))

            // Perform a two-way call using the metadata and then
            // convert the Mono result to a Flux<ZippyQuote> that
            // emits ZippyQuote objects once every second until the
            // stream is complete.
            .flatMapMany(r -> r
                        .retrieveFlux(Quote.class));
    }

    /**
     * A factory method that creates and returns a confirmed {@link
     * Subscription}.
     *
     * @param uuid A unique ID to identify the subscription
     * @return A {@link Mono} that emits a confirmed
     *         {@link Subscription}
     */
    public Mono<Subscription> subscribe(UUID uuid) {
        return mZippyQuoteRequester
            // Initialize the request that will be sent to the server.
            .map(r -> r
                // Set the metadata to indicate the request is for
                // the server's SUBSCRIBE endpoint.
                .route(SUBSCRIBE)

                // Create a new Subscription with the given
                // subscription ID and pass it as the data param.
                .data(new Subscription(uuid)))

            // Perform a two-way call using the metadata and data and
            // then convert the response to a Mono that emits the
            // resulting Subscription.
            .flatMap(r -> r
                .retrieveMono(Subscription.class))

            // Convert this Mono into a hot source, which caches the
            // emitted signals for future subscribers.
            .cache();
    }

    /**
     * Perform a confirmed cancellation on a {@link Subscription}.
     *
     * @param subscriptionRequest A {@link Subscription} object that
     *                            should be valid
     * @return A {@link Mono} that emits the {@link Subscription}
     *         object with the status of the cancellation, which is
     *         either CANCELLED if there was a matching subscription
     *         or ERROR if there was no matching subscription
     */
    public Mono<Subscription> cancelConfirmed
    (Mono<Subscription> subscriptionRequest) {
        return mZippyQuoteRequester
            // Wait for both Monos to emit one element and combine
            // these elements once into a Tuple2 object.
            .zipWith(subscriptionRequest)

            // Initialize the request that will be sent to the server.
            .map(tuple -> tuple
                // Set the metadata to indicate the request is for
                // the server's CANCEL_CONFIRMED endpoint.
                .getT1().route(CANCEL_CONFIRMED)

                // Set the subscriptionRequest as the data param.
                .data(tuple.getT2()))

            // Perform a two-way call using the metadata and data and
            // then convert the response to a Mono that emits the
            // resulting Subscription.
            .flatMap(r -> r
                .retrieveMono(Subscription.class));
    }

    /**
     * Perform a confirmed cancellation on a {@link Subscription}.
     *
     * @param uuid A unique ID that should identify a previous
     *             subscription
     * @return A {@link Mono} that emits a {@link Subscription} with
     *         CANCELLED if there was a matching subscription or ERROR
     *         if there was no matching subscription
     */
    public Mono<Subscription> cancelConfirmed(UUID uuid) {
        return mZippyQuoteRequester
            // Initialize the request that will be sent to the server.
            .map(r -> r
                // Set the metadata to indicate the request is for
                // the server's CANCEL_CONFIRMED endpoint.
                .route(CANCEL_CONFIRMED)

                // Create a new Subscription and pass it as the data
                // param.
                .data(new Subscription(uuid)))

            // Perform a two-way call using the metadata and data and
            // then convert the response to a Mono that emits the
            // resulting Subscription.
            .flatMap(r -> r
                .retrieveMono(Subscription.class));
    }

    /**
     * Perform an unconfirmed cancellation on a {@link Subscription},
     * i.e., no value is returned indicating whether the {@link
     * Subscription} was cancelled.  Only use this method if the
     * {@link Subscription} is known to be valid.
     *
     * @param subscriptionRequest A {@link Subscription} object that
     *                            should be valid
     * @return A {@link Mono} that emits a {@link Void} object
     */
    public Mono<Void> cancelUnconfirmed
        (Mono<Subscription> subscriptionRequest) {
        // Perform an unconfirmed cancellation on the subscription,
        // which returns no confirmation.
        return mZippyQuoteRequester
            // Wait for both Monos to emit one element and combine
            // these elements once into a Tuple2 object.
            .zipWith(subscriptionRequest)

            // Initialize the request that will be sent to the server.
            .map(r -> r.getT1()
                // Set the metadata to indicate the request is for
                // the server's CANCEL_UNCONFIRMED endpoint.
                .route(CANCEL_UNCONFIRMED)

                // Set the subscriptionRequest as the data param.
                .data(r.getT2()))

            // Perform a fire-and-forget call using the metadata and
            // data and return a Mono<Void>.
            .flatMap(RSocketRequester.RetrieveSpec::send);
    }

    /**
     * This method returns a {@link Flux} that emits all Zippy th'
     * Pinhead quotes once every second until complete if the {@code
     * subscriptionRequest} is valid, otherwise it returns an {@link
     * Flux} that emits an {@link IllegalAccessException}.
     *
     * @param subscriptionRequest A {@link Subscription} object
     * @return A {@link Flux} that emits all Zippy th' Pinhead quotes
     *         once every second until complete if the {@code
     *         subscriptionRequest} is valid, otherwise it returns an
     *         {@link Flux} that emits an {@link IllegalAccessException}
     */
    public Flux<Quote> getAllQuotes
    (Mono<Subscription> subscriptionRequest) {
        return mZippyQuoteRequester
            // Wait for both Monos to emit one element and combine
            // these elements once into a Tuple2 object.
            .zipWith(subscriptionRequest)

            // Initialize the request that will be sent to the server.
            .map(tuple -> tuple.getT1()
                // Set the metadata to indicate the request is for
                // the server's GET_QUOTE endpoint.
                .route(GET_ALL_QUOTES)

                // Set the subscriptionRequest as the data param.
                .data(tuple.getT2()))

            // Perform a two-way call using the metadata and data and
            // then convert the response to a Flux that emits a stream
            // of ZippyQuote objects once a second until complete.
            .flatMapMany(r -> r
                .retrieveFlux(Quote.class))

            // Return an error exception if the subscription was
            // cancelled.
            .switchIfEmpty(Flux
                .error(new IllegalAccessException("Subscription cancelled")));
    }

    /**
     *
     */
    public void closeConnection() {
        mZippyQuoteRequester.block().rsocket().dispose();
    }
}
