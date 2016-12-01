package de.dentrassi.biliskner.idle.test.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;

public class IdleServer implements AutoCloseable {

	private final NioEventLoopGroup bossGroup;
	private final NioEventLoopGroup workerGroup;
	private final ChannelFuture closeFuture;

	public IdleServer(final int port) throws InterruptedException {
		final ServerBootstrap bootstrap = new ServerBootstrap();

		this.bossGroup = new NioEventLoopGroup();
		this.workerGroup = new NioEventLoopGroup();

		try  {
			bootstrap.group(this.bossGroup, this.workerGroup);
			bootstrap.channel(NioServerSocketChannel.class);

			bootstrap.option(ChannelOption.SO_REUSEADDR, true);
			bootstrap.option(ChannelOption.SO_BACKLOG, 128);
			bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);

			bootstrap.childHandler(new ChannelInitializer<SocketChannel>() { // (4)
				@Override
				public void initChannel(final SocketChannel ch) throws Exception {
					ch.pipeline().addLast(new IdleServerHandler());
				}
			});

			final ChannelFuture f = bootstrap.bind(port).sync();
			this.closeFuture = f.channel().closeFuture();
		}
		catch ( final Exception e ) {
			// terminate early
			this.bossGroup.shutdownGracefully();
			this.workerGroup.shutdownGracefully();
			throw e;
		}
	}

	@Override
	public void close() throws Exception {
		final Future<?> f1 = this.bossGroup.shutdownGracefully();
		final Future<?> f2 = this.workerGroup.shutdownGracefully();
		f1.sync();
		f2.sync();
	}

	public void sleep() throws InterruptedException {
		if ( this.closeFuture != null)  {
			this.closeFuture.await();
		}
	}

}
