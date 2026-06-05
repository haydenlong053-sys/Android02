//package com.freddy.im;
//
//import com.freddy.im.crypto.Aes128CbcZeroIv;
//import com.freddy.im.crypto.ImCryptoKeys;
//import com.freddy.im.crypto.RsaPkcs1Util;
//import com.freddy.im.netty.SeekWeb4FrameDecoder;
//import com.freddy.im.netty.SeekWeb4Packet;
//import com.freddy.im.netty.SeekWeb4Protocol;
//import com.freddy.im.netty.SeekWeb4ServerOutboundFrame;
//import com.freddy.im.netty.SeekWeb4ServerResponseEncoder;
//import com.freddy.im.signal.ImSignalProtos;
//
//import java.security.PrivateKey;
//
//import io.netty.bootstrap.ServerBootstrap;
//import io.netty.channel.ChannelFuture;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.ChannelInitializer;
//import io.netty.channel.ChannelOption;
//import io.netty.channel.SimpleChannelInboundHandler;
//import io.netty.channel.nio.NioEventLoopGroup;
//import io.netty.channel.socket.SocketChannel;
//import io.netty.channel.socket.nio.NioServerSocketChannel;
//import io.netty.util.AttributeKey;
//
///**
// * 本地演示：SeekWeb4 包头 + CMD 0x01/0x02 + imsignal（与客户端逻辑配套）。
// */
//@Deprecated
//public class NettyServerDemo {
//
//    private static final AttributeKey<byte[]> ATTR_AES = AttributeKey.valueOf("seekweb4_aes");
//
//    public static void main(String[] args) throws Exception {
//        NioEventLoopGroup boss = new NioEventLoopGroup(1);
//        NioEventLoopGroup worker = new NioEventLoopGroup();
//        try {
//            ServerBootstrap b = new ServerBootstrap();
//            b.group(boss, worker)
//                    .channel(NioServerSocketChannel.class)
//                    .childHandler(new ChannelInitializer<SocketChannel>() {
//                        @Override
//                        protected void initChannel(SocketChannel ch) {
//                            ch.pipeline().addLast(new SeekWeb4FrameDecoder(false, SeekWeb4Protocol.PKG_BEGIN_VAL));
//                            ch.pipeline().addLast(new DemoSeekWeb4ServerHandler());
//                            ch.pipeline().addLast(new SeekWeb4ServerResponseEncoder());
//                        }
//                    })
//                    .option(ChannelOption.SO_BACKLOG, 128)
//                    .childOption(ChannelOption.SO_KEEPALIVE, true);
//
//            ChannelFuture f = b.bind(8855).sync();
//            System.out.println("SeekWeb4 demo server listen 8855, magic=0x" + Integer.toHexString(SeekWeb4Protocol.PKG_BEGIN_VAL));
//            f.channel().closeFuture().sync();
//        } finally {
//            boss.shutdownGracefully();
//            worker.shutdownGracefully();
//        }
//    }
//
//    private static final class DemoSeekWeb4ServerHandler extends SimpleChannelInboundHandler<SeekWeb4Packet> {
//
//        @Override
//        protected void channelRead0(ChannelHandlerContext ctx, SeekWeb4Packet p) throws Exception {
//            int cmd = p.getCmd();
//            byte[] body = p.getBody();
//
//            if (cmd == 0x01 && p.getFlags() == 0) {
//                PrivateKey pk = RsaPkcs1Util.parsePrivateKeyPkcs8Pem(ImCryptoKeys.demoPrivatePkcs8Pem());
//                byte[] aes = RsaPkcs1Util.decrypt(body, pk);
//                if (aes.length == 16) {
//                    ctx.channel().attr(ATTR_AES).set(aes);
//                }
//                ctx.writeAndFlush(new SeekWeb4ServerOutboundFrame((byte) 0x81, (byte) 0, (byte) 0, SeekWeb4Packet.EMPTY_BODY));
//                return;
//            }
//
//            if (cmd == 0x02 && p.getFlags() == 1) {
//                byte[] key = ctx.channel().attr(ATTR_AES).get();
//                if (key == null) {
//                    return;
//                }
//                byte[] plain = Aes128CbcZeroIv.decrypt(body, key);
//                ImSignalProtos.SignalRequest req = ImSignalProtos.SignalRequest.parseFrom(plain);
//                if (!req.hasHeader()) {
//                    return;
//                }
//                int biz = req.getHeader().getCmd();
//                byte[] replyPlain = handleBusiness(biz, req);
//                if (replyPlain == null) {
//                    return;
//                }
//                byte[] encReply = Aes128CbcZeroIv.encrypt(replyPlain, key);
//                ctx.writeAndFlush(new SeekWeb4ServerOutboundFrame((byte) 0x82, (byte) 1, (byte) 0, encReply));
//            }
//        }
//
//        private byte[] handleBusiness(int biz, ImSignalProtos.SignalRequest req) {
//            if (biz == ImSignalProtos.Cmd.BIND_USER_VALUE) {
//                ImSignalProtos.SignalResponse res = ImSignalProtos.SignalResponse.newBuilder()
//                        .setHeader(ImSignalProtos.ResponseHeader.newBuilder()
//                                .setVersion(1)
//                                .setCmd(ImSignalProtos.Cmd.BIND_USER_RES_VALUE)
//                                .setCode(0)
//                                .build())
//                        .build();
//                return res.toByteArray();
//            }
//            if (biz == ImSignalProtos.Cmd.SEND_MSG_VALUE && req.hasBody()) {
//                try {
//                    ImSignalProtos.SendMsgReq sm = ImSignalProtos.SendMsgReq.parseFrom(req.getBody().toByteArray());
//                    ImSignalProtos.SendMsgRes smr = ImSignalProtos.SendMsgRes.newBuilder()
//                            .setConversation(sm.getConversation())
//                            .setMessageId("srv-" + sm.getClientMsgId())
//                            .setClientMsgId(sm.getClientMsgId())
//                            .build();
//                    ImSignalProtos.SignalResponse res = ImSignalProtos.SignalResponse.newBuilder()
//                            .setHeader(ImSignalProtos.ResponseHeader.newBuilder()
//                                    .setVersion(1)
//                                    .setCmd(ImSignalProtos.Cmd.SEND_MSG_RES_VALUE)
//                                    .setCode(0)
//                                    .build())
//                            .setBody(smr.toByteString())
//                            .build();
//                    return res.toByteArray();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//            if (biz == ImSignalProtos.Cmd.PING_VALUE) {
//                ImSignalProtos.SignalResponse res = ImSignalProtos.SignalResponse.newBuilder()
//                        .setHeader(ImSignalProtos.ResponseHeader.newBuilder()
//                                .setVersion(1)
//                                .setCmd(ImSignalProtos.Cmd.PING_RES_VALUE)
//                                .setCode(0)
//                                .build())
//                        .build();
//                return res.toByteArray();
//            }
//            return null;
//        }
//    }
//}
