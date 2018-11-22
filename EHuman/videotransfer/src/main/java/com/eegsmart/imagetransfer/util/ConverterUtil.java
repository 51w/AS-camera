package com.eegsmart.imagetransfer.util;

public class ConverterUtil
	{

		// 在TCP发送时，高位地址先发送，地位地址后发送?
		// public static byte[] shorts2Bytes(short[] shorts)
		// {
		// byte[] bytes = new byte[shorts.length * 2];
		// for (int i = 1; i < shorts.length + 1; i++)
		// {
		// bytes[(i - 1) * 2] = (byte) ((shorts[i - 1] >> 8));
		// bytes[(i - 1) * 2 + 1] = (byte) (shorts[i - 1]);
		// }
		// return bytes;
		// }
//
//		public static byte[] shortToByteArray(short s)
//			{
//				byte[] targets = new byte[2];
//				for (int i = 0; i < 2; i++)
//					{
//						int offset = (targets.length - 1 - i) * 8;
//						targets[i] = (byte) ((s >>> offset) & 0xff);
//					}
//				return targets;
//			}

		public static byte[] shortsToByteArray(short[] s)
			{
				int length = s.length * 2;
				byte[] targets = new byte[length];

				for (int i = 0; i < s.length; i++)
					{
						for (int j = 0; j < 2; j++)
							{
								int offset = (2 - 1 - j) * 8;
								targets[i * 2 + j] = (byte) ((s[i] >>> offset) & 0xff);
							}
					}

				return targets;
			}
		/**
		 * Convert hex string to byte[]
		 * @param hexString the hex string
		 * @return byte[]
		 */
		public static byte[] hexStringToBytes(String hexString) {
			if (hexString == null || hexString.equals("")) {
				return null;
			}
			hexString = hexString.toUpperCase();
			int length = hexString.length() / 2;
			char[] hexChars = hexString.toCharArray();
			byte[] d = new byte[length];
			for (int i = 0; i < length; i++) {
				int pos = i * 2;
				d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
			}
			return d;
		}
		/**
		 * Convert char to byte
		 * @param c char
		 * @return byte
		 */
		private static byte charToByte(char c) {
			return (byte) "0123456789ABCDEF".indexOf(c);
		}

		public static String bytes2Hex(byte[] bytes)
			{
				StringBuilder sb = new StringBuilder();
				String tmp = null;
				for (byte b : bytes)
					{
						tmp = Integer.toHexString(0xFF & b);
						if (tmp.length() == 1)
							{
								tmp = "0" + tmp;
							}
						sb.append(tmp);
					}

				return sb.toString();

			}
		public static String bytes2Hex(byte[] bytes,int len)
		{
			StringBuilder sb = new StringBuilder();
			String tmp = null;
			byte b;
			for (int i=0;i<len;i++)
			{
				b=bytes[i];
				tmp = Integer.toHexString(0xFF & b);
				if (tmp.length() == 1)
				{
					tmp = "0" + tmp;
				}
				sb.append(tmp);
			}

			return sb.toString();

		}
		public static String byte2Hex(byte bt)
			{
				String temp = Integer.toHexString(0xFF & bt);
				if (temp.length() == 1)
					{
						temp = "0" + temp;
					}
				return temp;
			}

	}
