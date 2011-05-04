/**
 * Copyright (c) 2010, Daniel Bimschas and Dennis Pfisterer, Institute of Telematics, University of Luebeck
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 	- Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * 	  disclaimer.
 * 	- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * 	  following disclaimer in the documentation and/or other materials provided with the distribution.
 * 	- Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
 * 	  products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.uniluebeck.itm.netty.handlerstack.util;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.DownstreamMessageEvent;
import org.slf4j.LoggerFactory;

import de.uniluebeck.itm.tr.util.Triple;

public class StopAndWaitPacketSender<PacketType> {
    private final org.slf4j.Logger log;

    private final Deque<Triple<PacketType, ChannelHandlerContext, ChannelFuture>> queue =
            new LinkedList<Triple<PacketType, ChannelHandlerContext, ChannelFuture>>();

    private final ScheduledExecutorService executorService;

    private final int confirmTimeout;

    private final TimeUnit confirmTimeoutTimeUnit;

    private final ConfirmPendingOrTimeoutHelper confirmPending;

    private final Lock lock = new ReentrantLock();

    private ScheduledFuture<?> sendRunnableSchedule;

    private final Runnable sendRunnable = new Runnable() {

        @Override
        public void run() {
            lock.lock();
            try {
                // A new packet may be sent if no confirm is pending and the queue is not empty
                if (confirmPending.isConfirmPending()) {
                    int newTimeout = confirmTimeout / 4;
                    log.debug("Confirm pending, rescheduling new timeout after {}{}", newTimeout, confirmTimeoutTimeUnit);
                    rescheduleSendRunnable(newTimeout, confirmTimeoutTimeUnit);
                    return;

                } else if (queue.isEmpty()) {
                    log.trace("Queue empty, returning. No further task is scheduled");
                    return;
                }
                
                // Get the next packet to transmit
                Triple<PacketType, ChannelHandlerContext, ChannelFuture> first = queue.removeFirst();
                PacketType packet = first.getFirst();
                ChannelHandlerContext ctx = first.getSecond();
                ChannelFuture future = first.getThird();

                // Send it downstream
                DownstreamMessageEvent event =
                        new DownstreamMessageEvent(ctx.getChannel(), future, packet, ctx.getChannel()
                                .getRemoteAddress());

                log.trace("Sending message event downstream, packet: {}", packet);
                ctx.sendDownstream(event);

                // If more packets are in the queue, reschedule a timeout
                confirmPending.setConfirmPendingAndResetTimeout();

                if (!queue.isEmpty())
                    rescheduleSendRunnable(confirmTimeout, confirmTimeoutTimeUnit);

            } finally {
                lock.unlock();
            }
        }
    };

    public StopAndWaitPacketSender(ScheduledExecutorService executorService, int confirmTimeout,
            TimeUnit confirmTimeoutTimeUnit) {
        this(null, executorService, confirmTimeout, confirmTimeoutTimeUnit);
    }

    public StopAndWaitPacketSender(String instanceName, ScheduledExecutorService executorService, int confirmTimeout,
            TimeUnit confirmTimeoutTimeUnit) {
        log = LoggerFactory.getLogger(instanceName != null ? instanceName : StopAndWaitPacketSender.class.getName());
        this.executorService = executorService;
        this.confirmTimeout = confirmTimeout;
        this.confirmTimeoutTimeUnit = confirmTimeoutTimeUnit;
        confirmPending = new ConfirmPendingOrTimeoutHelper(confirmTimeout, confirmTimeoutTimeUnit);

    }

    public void enqeue(PacketType packet, ChannelHandlerContext ctx, ChannelFuture future) {
        lock.lock();
        try {
            // Add the packet to the queue
            queue.addLast(new Triple<PacketType, ChannelHandlerContext, ChannelFuture>(packet, ctx, future));
            log.trace("Enqueued packet (qlen={}): {}", queue.size(), packet);

            // Only schedule the runnable if we are the only packet in the queue.
            // Otherwise, there is already a scheduled timout.
            if (queue.size() == 1) {
                rescheduleSendRunnable(0, TimeUnit.MILLISECONDS);
            }

        } finally {
            lock.unlock();
        }
    }

    public void confirmReceived() {
        lock.lock();
        try {
            log.trace("Confirm received, enqueuing send runnable immediately");

            // Set confirm received to true so that the next packet can be sent immediately
            confirmPending.setConfirmReceived();

            // Schedule the timeout now
            rescheduleSendRunnable(0, TimeUnit.MILLISECONDS);

        } finally {
            lock.unlock();
        }
    }

    private void rescheduleSendRunnable(long initialDelay, TimeUnit timeUnit) {
        lock.lock();
        try {
            // Cancel an existing schedule
            if (sendRunnableSchedule != null)
                sendRunnableSchedule.cancel(false);

            // Reschedule
            sendRunnableSchedule = executorService.schedule(sendRunnable, initialDelay, timeUnit);

        } finally {
            lock.unlock();
        }
    }

}
