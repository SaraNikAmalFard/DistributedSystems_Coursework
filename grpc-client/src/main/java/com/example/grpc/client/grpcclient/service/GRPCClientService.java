package com.example.grpc.client.grpcclient.service;

import com.grpc.MatrixMultiplicationServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.json.JSONArray;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.grpc.MatrixMultiplicationReply;
import com.grpc.MatrixMultiplicationRequest;
import com.grpc.Row;

@Service
public class GRPCClientService {

	public static int matrixA[][];
	public static int matrixB[][];

	public boolean isPowerOfTwo(int n) {
		return (int) (Math.ceil((Math.log(n) / Math.log(2))))
				== (int) (Math.floor(((Math.log(n) / Math.log(2)))));
	}

	public boolean checkFiles() {
		File directoryPath = new File("./uploads");
		String[] contents = directoryPath.list();
		String filePath = null;
		List<String> lines = new ArrayList();

		int commonLineSize = 0;

		if (contents != null) {
			filePath = "./uploads" + "/" + contents[0];
			FileInputStream fileInputStream = null;
			try {
				fileInputStream = new FileInputStream(filePath);
				Scanner scanner = new Scanner(fileInputStream);
				while (scanner.hasNextLine()) {
					lines.add(scanner.nextLine());
				}
				scanner.close();

				if (isPowerOfTwo(lines.size()) == true) {
					String[] elements;
					for (String line : lines) {
						elements = line.split(" ");
						if (elements.length != lines.size()) {
							return false;
						}
					}
				} else {
					return false;
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			filePath = "./uploads" + "/" + contents[1];
			try {
				commonLineSize = lines.size();
				lines.clear();
				fileInputStream = new FileInputStream(filePath);
				Scanner scanner = new Scanner(fileInputStream);
				while (scanner.hasNextLine()) {
					lines.add(scanner.nextLine());
				}
				scanner.close();

				if (lines.size() == commonLineSize) {
					String[] elements;
					for (String line : lines) {
						elements = line.split(" ");
						if (elements.length != commonLineSize) {
							return false;
						}
					}
				} else {
					return false;
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			return true;
		}
		return false;
	}

	public static void delete() {
		File directory = new File("./uploads");
		File[] files = directory.listFiles();
		if (files != null) {
			for (File file : files) {
				file.delete();
			}
		}
	}

	public void create2DArrayFromFile() {
		File directoryPath = new File("./uploads");
		String[] contents = directoryPath.list();
		String lineA;
		String lineB;
		int row = 0;
		int size = 0;
		try {
			BufferedReader bufferA = new BufferedReader(new FileReader("./uploads" + "/" + contents[0]));
			BufferedReader bufferB = new BufferedReader(new FileReader("./uploads" + "/" + contents[1]));
			while ((lineA = bufferA.readLine()) != null) {
				lineB = bufferB.readLine();
				String[] valsA = lineA.trim().split("\\s+");
				String[] valsB = lineB.trim().split("\\s+");
				if (matrixA == null) {
					size = valsA.length;
					matrixA = new int[size][size];
					matrixB = new int[size][size];
				}

				for (int col = 0; col < size; col++) {
					matrixA[row][col] = Integer.parseInt(valsA[col]);
					matrixB[row][col] = Integer.parseInt(valsB[col]);
				}
				row++;
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	public static MatrixMultiplicationReply replyBuilder(int C[][]) {
		MatrixMultiplicationReply.Builder reply = MatrixMultiplicationReply.newBuilder();
		int MAX = matrixA.length;
		for (int i = 0; i < MAX; i++) {
			Row.Builder row = Row.newBuilder();
			for (int j = 0; j < C.length; j++) {
				row.addColumn(C[i][j]);
				row.setColumn(j, C[i][j]);
			}
			reply.addMatrixC(i, row);
		}
		return reply.build();
	}

	public static MatrixMultiplicationRequest requestBuilder(int[][] matrixA, int[][] matrixB) {
		MatrixMultiplicationRequest.Builder request = MatrixMultiplicationRequest.newBuilder();
		int MAX = matrixA.length;

		for (int i = 0; i < MAX; i++) {
			Row.Builder row = Row.newBuilder();
			for (int col : matrixA[i]) {
				row.addColumn(col);

			}
			request.addMatrixA(row.build());
		}

		for (int i = 0; i < MAX; i++) {
			Row.Builder row = Row.newBuilder();
			for (int col : matrixB[i]) {
				row.addColumn(col);
			}
			request.addMatrixB(row.build());
		}
		return request.build();
	}

	public static int[][] arrayReplyBuilder(MatrixMultiplicationReply reply) {
		int MAX = matrixA.length;
		int[][] C = new int[MAX][MAX];
		for (int i = 0; i < MAX; i++) {
			for (int j = 0; j < C[i].length; j++) {
				C[i][j] = reply.getMatrixC(i).getColumn(j);
			}
		}
		return C;
	}

	public static MatrixMultiplicationRequest getRequestNumOf(int[][] matrixA, int[][] matrixB, int bs, int reqNum)
	{
		int size = matrixA.length / bs;
		int rowA = reqNum / (size * size);
		int colArowB = (reqNum % (size * size)) / size;
		int colB = (reqNum % (size * size)) % size;
		int[][] ma = new int[bs][bs];
		int[][] mb = new int[bs][bs];
		for (int ki = 0; ki < bs; ki++)
		{
			for (int kj = 0; kj < bs; kj++)
			{
				ma[ki][kj] = matrixA[rowA * bs + ki][colArowB * bs + kj];
				mb[ki][kj] = matrixB[colArowB * bs + ki][colB * bs + kj];
			}
		}
		return requestBuilder(ma, mb);
	}

	public static double getFootprint()
	{
        System.out.println("Entered footprint method");
		int[][] matrixOne = {{1,2} , {3,4}};
		int[][] matrixTwo = {{5,6} , {7,8}};
		long startTime = System.nanoTime();

		ManagedChannel channel = ManagedChannelBuilder.forAddress("172.31.74.179", 8081)
				.usePlaintext()
				.build();
		MatrixMultiplicationServiceGrpc.MatrixMultiplicationServiceBlockingStub stub
				= MatrixMultiplicationServiceGrpc.newBlockingStub(channel);

		stub.multiplyBlock(requestBuilder(matrixOne,matrixTwo));

		long endTime = System.nanoTime();
		long footPrint = endTime - startTime;
		double ftprnt = (double) footPrint / 1_000_000_000;
		return ftprnt;
	}

	public static int[][] multiplyMatrixBlock(/*int[][] matrixA, int[][] matrixB ,*/ int deadline)
    {
        System.out.println("Entered Multiply method");
        System.out.println("Deadline is = " + deadline);
		int footPrint = (int)getFootprint();
        System.out.println("Foot print is = " + footPrint);
		int bSize = 2;
		int newSize = matrixA.length / bSize;
		int multiplyNum = (newSize) * (newSize) * (newSize);
		int SERVER_NUM = (footPrint* multiplyNum)/deadline;
		if(SERVER_NUM > 2)
		    SERVER_NUM = 2;
        System.out.println("Number of servers needed is = " + SERVER_NUM);
		int workPerServer = multiplyNum / SERVER_NUM;
		ArrayList<StreamObserver<MatrixMultiplicationRequest>> requestObserverList = new ArrayList<>();
		ArrayList<int[][]> resultMatrixArrayList = new ArrayList<>(multiplyNum);
		ArrayList<CountDownLatch> latchList = new ArrayList<>(SERVER_NUM);
		int[] resNum = new int[SERVER_NUM];
		int[] reqNum = new int[SERVER_NUM];

		ManagedChannel channel1 = ManagedChannelBuilder.forAddress("172.31.74.179", 9090).usePlaintext().build();
		ManagedChannel channel2 = ManagedChannelBuilder.forAddress("172.31.74.213", 9090).usePlaintext().build();
		/*ManagedChannel channel3 = ManagedChannelBuilder.forAddress("172.31.76.162", 9090).usePlaintext().build();
		ManagedChannel channel4 = ManagedChannelBuilder.forAddress("172.31.77.230", 9090).usePlaintext().build();
		ManagedChannel channel5 = ManagedChannelBuilder.forAddress("172.31.70.55", 9090).usePlaintext().build();
		ManagedChannel channel6 = ManagedChannelBuilder.forAddress("172.31.67.0", 9090).usePlaintext().build();
		ManagedChannel channel7 = ManagedChannelBuilder.forAddress("172.31.76.213", 9090).usePlaintext().build();
		ManagedChannel channel8 = ManagedChannelBuilder.forAddress("172.31.68.246", 9090).usePlaintext().build();
*/
		MatrixMultiplicationServiceGrpc.MatrixMultiplicationServiceStub asyncStub1  =MatrixMultiplicationServiceGrpc.newStub(channel1);
		MatrixMultiplicationServiceGrpc.MatrixMultiplicationServiceStub asyncStub2  =MatrixMultiplicationServiceGrpc.newStub(channel2);
		/*MatrixMultiplicationServiceGrpc.MatrixMultiplicationServiceStub asyncStub3  =MatrixMultiplicationServiceGrpc.newStub(channel3);
		MatrixMultiplicationServiceGrpc.MatrixMultiplicationServiceStub asyncStub4  =MatrixMultiplicationServiceGrpc.newStub(channel4);
		MatrixMultiplicationServiceGrpc.MatrixMultiplicationServiceStub asyncStub5  =MatrixMultiplicationServiceGrpc.newStub(channel5);
		MatrixMultiplicationServiceGrpc.MatrixMultiplicationServiceStub asyncStub6  =MatrixMultiplicationServiceGrpc.newStub(channel6);
		MatrixMultiplicationServiceGrpc.MatrixMultiplicationServiceStub asyncStub7  =MatrixMultiplicationServiceGrpc.newStub(channel7);
		MatrixMultiplicationServiceGrpc.MatrixMultiplicationServiceStub asyncStub8  =MatrixMultiplicationServiceGrpc.newStub(channel8);*/

		ArrayList<MatrixMultiplicationServiceGrpc.MatrixMultiplicationServiceStub> asyncStubList = new ArrayList<>(SERVER_NUM);
		asyncStubList.add(asyncStub1);
		asyncStubList.add(asyncStub2);
		/*asyncStubList.add(asyncStub3);
		asyncStubList.add(asyncStub4);
		asyncStubList.add(asyncStub5);
		asyncStubList.add(asyncStub6);
		asyncStubList.add(asyncStub7);
		asyncStubList.add(asyncStub8);*/

		for (int i = 0; i < SERVER_NUM; i++)
		{
			latchList.set(i , new CountDownLatch(1));
			int finalI = i;

		StreamObserver<MatrixMultiplicationRequest> temp =	asyncStubList.get(i).multiplyStreamBlock(new StreamObserver<MatrixMultiplicationReply>() {
				@Override
				public void onNext(MatrixMultiplicationReply matrixResult)
				{//this is called to get Result of Mult / server calls this to give us result
					resultMatrixArrayList.toArray()[finalI * workPerServer + (resNum[finalI]++)] = arrayReplyBuilder(matrixResult);
					if (reqNum[finalI] < workPerServer)  // we call this on next to give server the next work load
						requestObserverList.get(finalI).onNext(getRequestNumOf(matrixA,matrixB, bSize ,finalI * workPerServer + (reqNum[finalI]++)));
					else
						requestObserverList.get(finalI).onCompleted();
				}

				@Override
				public void onError(Throwable t) {
					latchList.get(finalI).countDown(); }

				@Override
				public void onCompleted()
				{//info("Finished RecordRoute");
					latchList.get(finalI).countDown();
				}
			});
		    requestObserverList.set(i,temp);

			//initial call to start stream
			requestObserverList.get(i).onNext(getRequestNumOf(matrixA,matrixB, bSize ,i * workPerServer + (reqNum[i]++)));
		}


		for(int z=0 ;z<SERVER_NUM;z++)
		{
			try
			{
				latchList.get(z).await(1, TimeUnit.MINUTES);
			}catch (Exception e)
			{ }

		}

		//add method
		int[][] matrix;
		int[][] bMatRes;
		ArrayList<int[][]> matrixArrayToAdd = new ArrayList<>(newSize);
		for (int k = 0; k < matrixArrayToAdd.toArray().length; k++)
		{
            matrix = new int[matrixA.length][matrixB.length];
			matrixArrayToAdd.set(k, matrix);

			for (int colIndex = 0; colIndex < newSize; colIndex++)
			{
				for (int rowIndex = 0; rowIndex < newSize; rowIndex++)
				{
					bMatRes = resultMatrixArrayList.get(colIndex * (newSize * newSize) + k * newSize + rowIndex);
					for (int ki = 0; ki < bSize; ki++)
					{
						for (int kj = 0; kj < bSize; kj++)
						{
							matrix[rowIndex * bSize + ki][colIndex * bSize + kj] = bMatRes[ki][kj];
						}
					}
				}
			}
		}

		//client
		int[][] matrixResultFinal = new int[matrixA.length][matrixA.length];
		for (int r = 0; r < matrixResultFinal.length; r++)
		{
			for (int c = 0; c < matrixResultFinal[r].length; c++)
			{
				for (int[][] matrixx : matrixArrayToAdd)
					matrixResultFinal[r][c] += matrixx[r][c];
			}
		}
		return matrixResultFinal;
	}

	public JSONArray replyMatrixToJson(int[][] matrix)
	{
		JSONArray jsonArray = new JSONArray();
		for (int[] row : matrix)
		{
			JSONArray arr = new JSONArray();
			for (int element : row)
			{
				arr.put(String.valueOf(element));
			//	arr.add(Character.toString(el));
			}
			jsonArray.put(arr);
			//jsonArray.add(arr);
		}
      return jsonArray;
		//Another way
		/*for(int[] row: matrix)
			jsonArray.put(Arrays.toString(row));*/
	}
}