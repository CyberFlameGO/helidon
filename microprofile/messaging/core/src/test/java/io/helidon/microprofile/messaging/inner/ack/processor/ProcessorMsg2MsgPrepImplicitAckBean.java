/*
 * Copyright (c) 2020 Oracle and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.helidon.microprofile.messaging.inner.ack.processor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;

import io.helidon.microprofile.messaging.AssertableTestBean;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.reactivestreams.Publisher;

/**
 * This test is modified version of official tck test in version 1.0
 * https://github.com/eclipse/microprofile-reactive-messaging
 */
@ApplicationScoped
public class ProcessorMsg2MsgPrepImplicitAckBean implements AssertableTestBean {

    private final CompletableFuture<Void> ackFuture = new CompletableFuture<>();
    private final AtomicBoolean completedBeforeProcessor = new AtomicBoolean(false);

    @Outgoing("inner-processor")
    public Publisher<Message<String>> produceMessage() {
        return ReactiveStreams.of(Message.of("test-data", () -> {
            ackFuture.complete(null);
            return CompletableFuture.completedFuture(null);
        })).buildRs();
    }

    @Incoming("inner-processor")
    @Outgoing("inner-consumer")
    public Message<String> process(Message<String> msg) {
        completedBeforeProcessor.set(ackFuture.isDone());
        return Message.of(msg.getPayload());
    }

    @Incoming("inner-consumer")
    @Acknowledgment(Acknowledgment.Strategy.NONE)
    public CompletionStage<Void> receiveMessage(Message<String> msg) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void assertValid() {
        await("Message not acked!", ackFuture);
        assertWithOrigin("Should be acked in pre-process!", completedBeforeProcessor.get());
    }
}
