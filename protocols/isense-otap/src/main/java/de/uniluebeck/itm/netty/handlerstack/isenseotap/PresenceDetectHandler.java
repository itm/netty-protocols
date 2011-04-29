package de.uniluebeck.itm.netty.handlerstack.isenseotap;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uniluebeck.itm.netty.handlerstack.isenseotap.generatedmessages.PresenceDetectReply;
import de.uniluebeck.itm.netty.handlerstack.isenseotap.generatedmessages.PresenceDetectRequest;
import de.uniluebeck.itm.tr.util.TimedCache;
import de.uniluebeck.itm.wsn.devicedrivers.generic.ChipType;

public class PresenceDetectHandler extends SimpleChannelHandler {
    private static final Logger log = LoggerFactory.getLogger(PresenceDetectHandler.class);

    private final ScheduledExecutorService executorService;

    private final int presenceDetectInterval;

    private final TimeUnit presenceDetectIntervalTimeunit;

    private Channel channel = null;

    private ScheduledFuture<?> sendPresenceDetectRunnableSchedule;

    private final TimedCache<Integer, OtapDevice> detectedDevices;

    private final Runnable sendPresenceDetectRunnable = new Runnable() {
        @Override
        public void run() {
            if (channel != null) {
                channel.write(new PresenceDetectRequest());
                log.trace("Sent Presence Detect Request");
            }
        }
    };

    
    public PresenceDetectHandler(final ScheduledExecutorService executorService, final int presenceDetectInterval,
            int deviceTimeout, final TimeUnit timeunit) {
        this.executorService = executorService;
        this.presenceDetectIntervalTimeunit = timeunit;
        this.presenceDetectInterval = presenceDetectInterval;

        detectedDevices = new TimedCache<Integer, OtapDevice>(deviceTimeout, timeunit);
    }

    public void startPresenceDetect() {
        stopPresenceDetect();
        sendPresenceDetectRunnableSchedule =
                executorService.scheduleWithFixedDelay(sendPresenceDetectRunnable, 0, presenceDetectInterval,
                        presenceDetectIntervalTimeunit);

    }

    public void stopPresenceDetect() {
        sendPresenceDetectRunnableSchedule.cancel(false);
    }

    public Collection<OtapDevice> getDetectedDevices() {
        return detectedDevices.values();
    }
    
    @Override
    public void channelDisconnected(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
        stopPresenceDetect();
        channel = null;
        super.channelDisconnected(ctx, e);
    }

    @Override
    public void channelConnected(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
        assert channel == null;
        channel = e.getChannel();
        super.channelConnected(ctx, e);
    }

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
        Object message = e.getMessage();

        if (!(message instanceof PresenceDetectReply)) {
            super.messageReceived(ctx, e);
            return;
        }

        PresenceDetectReply reply = (PresenceDetectReply) message;
        log.debug("Received presence detect reply: {}", reply);

        OtapDevice d = getOrCreateDevice(reply.device_id);

        d.setApplicationID(reply.application_id);
        d.setSoftwareRevision(reply.revision_no);
        d.setChipType(ChipType.getChipType(reply.chip_type));
        d.setProtocolVersion(reply.protocol_version);
        d.getLastReception().touch();
        
        if( log.isDebugEnabled())
            log.debug("Detected {} devices with ids: {}", detectedDevices.size(), Arrays.toString(detectedDevices.keySet().toArray()));
    }

    private OtapDevice getOrCreateDevice(int deviceId) {
        OtapDevice device = detectedDevices.get(deviceId);
        if (device == null)
            device = new OtapDevice();

        return device;
    }
}
