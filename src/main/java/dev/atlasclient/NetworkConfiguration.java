package dev.atlasclient;

import net.minecraft.network.*;

/**
 * NetworkConfiguration - Configuration class for storing network connection parameters.
 * 
 * This class holds the essential information about the current network state including
 * connection phase, communication side, and protocol version compatibility.
 */
public class NetworkConfiguration {
    /** The current phase of the network connection (HANDSHAKING, PLAY, STATUS, LOGIN) */
    public NetworkPhase phase;
    
    /** The side of the network connection (CLIENTBOUND or SERVERBOUND) */
    public NetworkSide side;
    
    /** The Minecraft protocol version number for network compatibility */
    public int protocolVersion;
    public long lastPacket;

    /**
     * Sets all network configuration parameters at once.
     * Useful for atomic updates to the network state.
     *
     * @param phase The network phase to set
     * @param side The network side to set  
     * @param protocolVersion The protocol version to set
     */
    public void set(NetworkPhase phase, NetworkSide side, int protocolVersion) {
        this.phase = phase;
        this.side = side;
        this.protocolVersion = protocolVersion;
    }
}
