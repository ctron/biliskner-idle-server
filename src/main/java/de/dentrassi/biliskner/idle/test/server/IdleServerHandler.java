package de.dentrassi.biliskner.idle.test.server;

import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

public class IdleServerHandler extends ChannelDuplexHandler {

	private static final Logger logger = LoggerFactory.getLogger(IdleServerHandler.class);

	private final Random random = new Random();

	private int sequence = 0;

	@Override
	public void channelActive(final ChannelHandlerContext ctx) throws Exception {
		logger.info("New connection - {}", ctx);
		process(ctx);
	}

	@Override
	public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
		if (msg instanceof ByteBuf) {
			((ByteBuf) msg).release();
		}
	}

	@Override
	public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}

	private void process(final ChannelHandlerContext ctx) {
		final int idlePeriod = nextPeriod();
		final String data = String.format("%04d#%04d%n", this.sequence++, idlePeriod);

		if ( this.sequence > 9999) {
			// reset sequence
			this.sequence = 0;
		}

		final ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
		buffer.writeBytes(data.getBytes(StandardCharsets.UTF_8));
		ctx.writeAndFlush(buffer);

		ctx.executor().schedule(() -> process(ctx), idlePeriod, TimeUnit.SECONDS);
	}

	private int nextPeriod() {
		return this.random.nextInt(120);
	}
}
