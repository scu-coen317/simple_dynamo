package edu.scu.coen317.client;

import edu.scu.coen317.common.message.MessageType;
import edu.scu.coen317.common.message.client.ClientRequest;
import edu.scu.coen317.common.message.client.codec.ClientRequestEncoder;
import edu.scu.coen317.common.message.client.codec.ClientResponseDecoder;
import edu.scu.coen317.common.model.Node;
import edu.scu.coen317.common.util.NodeGlobalView;
import edu.scu.coen317.common.util.NodeLocator;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamoClient {
	private static final Logger LOG = LoggerFactory.getLogger(DynamoClient.class);

    public static void main(String[] args) throws Exception {
        put("key_1", "val_1");
        get("key_1");
    }
    
    public static void put(String key, String val) {
    	ClientRequest req = new ClientRequest(MessageType.PUT, key, val);
    	try {
    		LOG.info("Sending PUT request...Key: {}, Value: {}", key, val);
			boolean res = execute(req);
			LOG.info("PUT request {}", res ? "succeeded" : "failed");
		} catch (Exception e) {
			LOG.warn("PUT request got interrupted...");
		}
    }
    
    public static void get(String key) {
    	ClientRequest req = new ClientRequest(MessageType.GET, key);
    	try {
    		LOG.info("Sending GET request...Key: {}", key);
			boolean res = execute(req);
			LOG.info("GET request {}", res ? "succeeded" : "failed");
		} catch (Exception e) {
			LOG.warn("GET request got interrupted...");
		}
    }

    private static boolean execute(ClientRequest req) throws Exception {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup)
             .channel(NioSocketChannel.class)
             .handler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 protected void initChannel(SocketChannel sc) throws Exception {
                     ChannelPipeline cp = sc.pipeline();
                     cp.addLast(new ClientResponseDecoder());
                     cp.addLast(new ClientRequestEncoder());
                     cp.addLast(new RequestHandler(req));
                 }
             });

            Set<Node> targets = NodeLocator.getNodes(NodeGlobalView.readAll(), req.getKey(), "");
            for (Node node : targets) {
                ChannelFuture future = b.connect(node.getIp(), node.getPort()).await();
                future.channel().closeFuture().sync();
                if (future.isSuccess()) {
                    return true;
                }
            }
        } finally {
            workerGroup.shutdownGracefully();
        }
        return false;
    }
}
