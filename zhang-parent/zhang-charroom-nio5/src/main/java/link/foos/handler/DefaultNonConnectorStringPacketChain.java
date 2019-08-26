package link.foos.handler;


import link.packaging.impl.StringReceivePacket;

/**
 * 默认String接收节点，不做任何事情
 */
class DefaultNonConnectorStringPacketChain extends ConnectorStringPacketChain {
    @Override
    protected boolean consume(ConnectorHandler handler, StringReceivePacket packet) {
        return false;
    }
}
