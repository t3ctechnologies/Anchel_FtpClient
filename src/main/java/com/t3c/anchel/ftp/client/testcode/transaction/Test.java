package com.t3c.anchel.ftp.client.testcode.transaction;

import java.io.IOException;

import com.t3c.anchel.ftp.client.testcode.FtpClient;

public class Test {

	public static void main(String[] args) {
		try {
			FtpClient.init(args);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
