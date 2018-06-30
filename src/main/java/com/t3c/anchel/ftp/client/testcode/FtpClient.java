/**
 *
 */
package com.t3c.anchel.ftp.client.testcode;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.waarp.common.logging.WaarpLogger;
import org.waarp.common.logging.WaarpLoggerFactory;

import com.t3c.anchel.ftp.client.WaarpFtp4jClient;
import com.t3c.anchel.ftp.client.testcode.transaction.Ftp4JClientTransactionTest;
import com.t3c.anchel.ftp.client.testcode.transaction.FtpClientThread;

/**
 * Simple test example using predefined scenario (Note: this uses the
 * configuration example for user shutdown command)
 * 
 * @author frederic
 * 
 */
public class FtpClient {
	public static AtomicLong numberOK = new AtomicLong(0);

	public static AtomicLong numberKO = new AtomicLong(0);

	public static void main(String[] args) throws NumberFormatException, IOException {
		System.out.println("Client started.");
		// init(args[0], Integer.parseInt(args[1]));
	}

	/**
	 * @param args
	 * @throws IOException
	 * 
	 *             index 0 : filename index 1 : mode index 2 : gatewayIp index 3
	 *             : username index 4 : password index 5 : accountName
	 */
	public static boolean init(String[] args) throws IOException {
		// WaarpLoggerFactory.setDefaultFactory(new
		// WaarpSlf4JLoggerFactory(null));
		final WaarpLogger logger = WaarpLoggerFactory.getLogger(WaarpFtp4jClient.class);
		
		System.setProperty("javax.net.debug", "false");
		String fileName = args[0];
		int mode = Integer.parseInt(args[1]);
		String server = null;
		int port = 21;
		String username = null;
		String passwd = null;
		String account = null;
		String localFilename = null;
		int numberThread = 1;
		int numberIteration = 1;
		if (fileName == null) {
			System.err.println("Usage: " + FtpClient.class.getSimpleName()
					+ " server port user pwd acct localfilename nbThread nbIter");
			System.exit(1);
		}

		server = InetAddress.getByName(args[2]).getHostAddress();
		port = 21;
		username = args[3];
		passwd = args[4];
		account = args[5];
		int type = mode;
		numberThread = Integer.parseInt("1");
		numberIteration = Integer.parseInt("1");

		int delay = 0;
		int isSSL = 0;
		boolean shutdown = false;
		Ftp4JClientTransactionTest client = new Ftp4JClientTransactionTest(server, port, username, passwd, account,
				isSSL);
		System.out.println("FTPClient to Gateway IPaddress :" +server);
		if (!client.connect()) {
			System.err.println("Cant connect");
			logger.error("FtpClient is not connected with GatewayFtp Server");
			FtpClient.numberKO.incrementAndGet();
			return false;
		}
		logger.info("FtpClient is get connected with GatewayFtp Server");
		if (type == 11 || type == -11) {
			File linsharefile = new File(fileName);
			String EncodedFile = linsharefile.getName();
			localFilename = Base64_Testclass.encodeFile(fileName, EncodedFile);
		} else {
			localFilename = fileName;
		}
		try {
			for (int i = 0; i < numberThread; i++) {
				client.makeDir("T" + i);
			}
			System.err.println("SITE: " + client.featureEnabled("SITE"));
			System.err.println("SITE CRC: " + client.featureEnabled("SITE XCRC"));
			System.err.println("CRC: " + client.featureEnabled("XCRC"));
			System.err.println("MD5: " + client.featureEnabled("XMD5"));
			System.err.println("SHA1: " + client.featureEnabled("XSHA1"));
		} finally {
			client.logout();
		}
		if (isSSL > 0) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		ExecutorService executorService = Executors.newCachedThreadPool();
		long date1 = System.currentTimeMillis();
		for (int i = 0; i < numberThread; i++) {
			executorService.execute(new FtpClientThread("T" + i, server, port, username, passwd, account, localFilename,
					numberIteration, type, delay, isSSL));
			if (delay > 0) {
				try {
					long newdel = ((delay / 3) / 10) * 10;
					if (newdel == 0)
						Thread.yield();
					else
						Thread.sleep(newdel);
				} catch (InterruptedException e) {
				}
			} else {
				Thread.yield();
			}
		}
		try {
			Thread.sleep(100);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
			executorService.shutdownNow();
			Thread.currentThread().interrupt();
		}
		executorService.shutdown();
		long date2 = 0;
		try {
			if (!executorService.awaitTermination(12000, TimeUnit.SECONDS)) {
				date2 = System.currentTimeMillis() - 120000 * 60;
				executorService.shutdownNow();
				if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
					System.err.println("Really not shutdown normally");
				}
			} else {
				date2 = System.currentTimeMillis();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			executorService.shutdownNow();
			date2 = System.currentTimeMillis();
			Thread.currentThread().interrupt();
		}

		System.out.println(localFilename + " " + numberThread + " " + numberIteration + " " + type + " Real: "
				+ (date2 - date1) + " OK: " + numberOK.get() + " KO: " + numberKO.get() + " Trf/s: "
				+ (numberOK.get() * 1000 / (date2 - date1)));
		if (shutdown) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
			client = new Ftp4JClientTransactionTest(server, port, "fredo", "fred1", "a", isSSL);
			if (!client.connect()) {
				System.err.println("Cant connect");
				FtpClient.numberKO.incrementAndGet();
				return false;
			}
			try {
				String[] results = client.executeSiteCommand("internalshutdown abcdef");
				System.err.print("SHUTDOWN: ");
				for (String string : results) {
					System.err.println(string);
				}
			} finally {
				client.disconnect();
			}
		}
		// TODO Deleting encoded file
		if (type == 11 || type == -11) {
			new File(localFilename).delete();
		}
		return true;
	}

}
