package com.ubjson.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.ubjson.io.UBJInputStream;
import org.ubjson.io.UBJOutputStream;

import com.ubjson.data.CouchDB4k;
import com.ubjson.data.CouchDB4kMarshaller;

public class CouchDB4kBenchmark {
	private static int len;
	private static byte[] data;

	private static UBJOutputStream out = new UBJOutputStream(
			new NullOutputStream());

	public static void main(String[] args) throws IOException {
		if(true) {
			FileOutputStream out = new FileOutputStream("CouchDB4k.ubj");
			CouchDB4kMarshaller.serialize(new CouchDB4k(), new UBJOutputStream(out));
			out.flush();
			out.close();
			
			System.exit(0);
		}
		
		
		// Write a single CouchDB4k out to byte[]
		RawByteArrayOutputStream rbaos = new RawByteArrayOutputStream(1024);
		UBJOutputStream stream = new UBJOutputStream(rbaos);
		CouchDB4kMarshaller.serialize(new CouchDB4k(), stream);

		stream.flush();
		stream.close();

		// Remember the example output
		len = rbaos.getBytesWritten();
		data = rbaos.getArray();

		benchmarkMS(1000000); // 1mil
		benchmarkMS(100000); // 100k
		benchmarkMS(10000); // 10k
		benchmarkNS(1000); // 1k
		benchmarkNS(100); // 100
		benchmarkNS(1); // 1
	}

	public static void benchmarkMS(int iters) throws IOException {
		System.out.println("Benchmark (milliseconds): " + iters
				+ " iterations.");

		long t = 0;
		long tt = 0;
		CouchDB4k db = new CouchDB4k();

		for (int i = 0; i < iters; i++) {
			tt = System.currentTimeMillis();
			CouchDB4kMarshaller.serialize(db, out);
			t += (System.currentTimeMillis() - tt);
		}

		printTime("Serialization", iters, t, false);
		t = 0;
		tt = 0;

		for (int i = 0; i < iters; i++) {
			UBJInputStream in = new UBJInputStream(new ByteArrayInputStream(
					data, 0, len));

			tt = System.currentTimeMillis();
			CouchDB4kMarshaller.deserialize(in);
			t += (System.currentTimeMillis() - tt);
		}

		printTime("Deserialization", iters, t, false);
	}

	public static void benchmarkNS(int iters) throws IOException {
		System.out
				.println("Benchmark (nanoseconds): " + iters + " iterations.");

		long t = 0;
		long tt = 0;
		CouchDB4k db = new CouchDB4k();

		for (int i = 0; i < iters; i++) {
			tt = System.nanoTime();
			CouchDB4kMarshaller.serialize(db, out);
			t += (System.nanoTime() - tt);
		}

		printTime("Serialization", iters, t, true);
		t = 0;
		tt = 0;

		for (int i = 0; i < iters; i++) {
			UBJInputStream in = new UBJInputStream(new ByteArrayInputStream(
					data, 0, len));

			tt = System.nanoTime();
			CouchDB4kMarshaller.deserialize(in);
			t += (System.nanoTime() - tt);
		}

		printTime("Deserialization", iters, t, true);
	}

	private static void printTime(String label, long iters, long et, boolean ns) {
		long nsPerOp = (ns ? et : (et * 1000000L)) / iters;
		double opsPerSec = (double) iters
				/ (double) ((double) et / 1000d / (ns ? 1000000d : 1d));

		System.out.printf("\t%s, Elapsed Time: %d %s [%d ns/op, %f ops/sec]\n",
				label, et, (ns ? "ns" : "ms"), nsPerOp, opsPerSec);
	}

	private static class RawByteArrayOutputStream extends ByteArrayOutputStream {
		public RawByteArrayOutputStream(int size) {
			super(size);
		}

		public int getBytesWritten() {
			return count;
		}

		public byte[] getArray() {
			return buf;
		}
	}

	private static class NullOutputStream extends OutputStream {
		@Override
		public void write(int b) throws IOException {
			// no-op
		}

		@Override
		public void write(byte[] b) throws IOException {
			// no-op
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			// no-op
		}
	}
}