package com.t3c.anchel.ftp.client.testcode;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;

public class Base64_Testclass {
	/**
	 * Defines the length of bytes read from the input file in one iteration. It
	 * must be a multiple of 3 and 4. It affects the heap used. Current value is 12
	 * kB.
	 */
	private static final int LENGTH = 12288;

	private static final int MASK_ENCODE = 0x3F;

	private static final int MASK_DECODE = 0xFF;

	/** Alphabet of base64, used to encode bytes in base64. */
	private static final char[] ALPHABET = new char[64];

	static {
		for (int i = 0; i < 26; i++) {
			ALPHABET[i] = (char) ('A' + i);
			ALPHABET[i + 26] = (char) ('a' + i);
		}
		for (int i = 0; i < 10; i++) {
			ALPHABET[i + 52] = (char) ('0' + i);
		}
		ALPHABET[62] = '+';
		ALPHABET[63] = '/';
	}

	/** Array to decode base64 bytes array. */
	private static int[] FROM_BASE64 = new int[128];

	static {
		for (int i = 0; i < ALPHABET.length; i++) {
			FROM_BASE64[ALPHABET[i]] = i;
		}
	}

	/**
	 * Encode the bytes from an array into a base64 byte array.
	 * 
	 * @param input
	 *            The byte array to encode.
	 * 
	 * @return The base64 byte array of the input
	 */
	public static byte[] encode(final byte[] input) {
		return encode(input, input.length);
	}

	/**
	 * Encode the first bytes from an array into a base64 byte array.
	 * 
	 * @param input
	 *            The byte array to encode.
	 * @param size
	 *            The number of bytes to encode.
	 * 
	 * @return The base64 byte array of the input
	 */
	public static byte[] encode(final byte[] input, final int size) {
		final byte[] base64 = new byte[(size + 2) / 3 * 4];
		int a = 0;
		int i = 0;
		while (i < size) {
			final byte b0 = input[i++];
			final byte b1 = i < size ? input[i++] : 0;
			final byte b2 = i < size ? input[i++] : 0;

			base64[a++] = (byte) ALPHABET[b0 >> 2 & MASK_ENCODE];
			base64[a++] = (byte) ALPHABET[(b0 << 4 | (b1 & 0xFF) >> 4) & MASK_ENCODE];
			base64[a++] = (byte) ALPHABET[(b1 << 2 | (b2 & 0xFF) >> 6) & MASK_ENCODE];
			base64[a++] = (byte) ALPHABET[b2 & MASK_ENCODE];
		}
		switch (size % 3) {
		case 1:
			base64[--a] = '=';
		case 2:
			base64[--a] = '=';
		}
		return base64;
	}

	/**
	 * Decode the base64 input string into a byte array.
	 * 
	 * @param base64
	 *            The base64 input string to decode.
	 * 
	 * @return The decoded byte array.
	 */
	public static byte[] decode(final String base64) {
		final int delta = base64.endsWith("==") ? 2 : base64.endsWith("=") ? 1 : 0;
		final byte[] output = new byte[base64.length() * 3 / 4 - delta];
		int index = 0;
		for (int i = 0; i < base64.length(); i += 4) {
			final int c0 = FROM_BASE64[base64.charAt(i)];
			final int c1 = FROM_BASE64[base64.charAt(i + 1)];
			output[index++] = (byte) ((c0 << 2 | c1 >> 4) & MASK_DECODE);
			if (index >= output.length) {
				return output;
			}
			final int c2 = FROM_BASE64[base64.charAt(i + 2)];
			output[index++] = (byte) ((c1 << 4 | c2 >> 2) & MASK_DECODE);
			if (index >= output.length) {
				return output;
			}
			final int c3 = FROM_BASE64[base64.charAt(i + 3)];
			output[index++] = (byte) ((c2 << 6 | c3) & MASK_DECODE);
		}
		return output;
	}

	/**
	 * Converts a file from/to a base64 file.
	 * 
	 * @param inputFileName
	 *            The name of the file to convert.
	 * @param outputFileName
	 *            The name of the file to write the result of the conversion.
	 * @param isEncode
	 *            The way of conversion (true: encode ; false: decode).
	 * 
	 * @throws IOException
	 *             If an error occurs during reading or writing file.
	 */
	private static String convert(final String inputFileName, final String outputFileName, final boolean isEncode)
			throws IOException {
		final InputStream inputStream = new FileInputStream(inputFileName);
		final byte[] inputBytes = new byte[LENGTH];

		final OutputStream outputStream = new FileOutputStream(outputFileName);

		int bytesRead = LENGTH;
		while (bytesRead == LENGTH) {
			bytesRead = inputStream.read(inputBytes);
			byte[] outputBytes, outputBytes1, outputBytes2;
			if (isEncode) {
				outputBytes = Base64_Testclass.encode(inputBytes, bytesRead);
				//Base64.Encoder encoder = Base64.getEncoder();
				// outputBytes = encoder.encode(outputBytes1);
			} else {
				outputBytes = Base64_Testclass.decode(new String(inputBytes, 0, bytesRead));
				//Base64.Decoder decoder = Base64.getDecoder();
				// outputBytes = decoder.decode(outputBytes2);
			}
			outputStream.write(outputBytes);
		}

		inputStream.close();
		outputStream.close();
		return outputFileName;
	}

	/**
	 * Encode a file to a base64 file.
	 * 
	 * @param inputFileName
	 *            The name of the file to encode.
	 * @param outputFileName
	 *            The name of the file to write the result of the conversion.
	 * @return
	 * 
	 * @throws IOException
	 *             If an error occurs during reading or writing file.
	 */
	public static String encodeFile(final String inputFileName, final String outputFileName) throws IOException {
		return convert(inputFileName, outputFileName, true);
	}

	/**
	 * Decode a file to a base64 file.
	 * 
	 * @param inputFileName
	 *            The name of the file to decode.
	 * @param outputFileName
	 *            The name of the file to write the result of the conversion.
	 * 
	 * @throws IOException
	 *             If an error occurs during reading or writing file.
	 */
	public static String decodeFile(final String inputFileName, final String outputFileName) throws IOException {
		return convert(inputFileName, outputFileName, false);
	}

	/**
	 * Return the base64 alphabet.
	 * 
	 * @return The base64 alphabet.
	 */
	public static String getAlphabet() {
		return new String(ALPHABET);
	}
}
