package com.coalesenses.isense.ishell.interpreter;

public class IShellInterpreterSetChannelMessage {

    private byte channel;

    public IShellInterpreterSetChannelMessage(byte channel) {
        this.channel = channel;
    }

    public byte getChannel() {
        return channel;
    }
}
