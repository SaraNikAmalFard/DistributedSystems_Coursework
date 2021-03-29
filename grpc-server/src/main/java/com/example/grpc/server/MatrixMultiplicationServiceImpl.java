package com.example.grpc.server;

import com.grpc.MatrixMultiplicationReply;
import com.grpc.MatrixMultiplicationRequest;
import com.grpc.MatrixMultiplicationServiceGrpc;
import com.grpc.Row;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;


@GrpcService
public class MatrixMultiplicationServiceImpl extends MatrixMultiplicationServiceGrpc.MatrixMultiplicationServiceImplBase
{

    @Override
    public void addBlock(MatrixMultiplicationRequest request, StreamObserver<MatrixMultiplicationReply> responseObserver)
    {
        int MAX = request.getMatrixA(1).getColumnCount();
        int c[][] = new int[MAX][MAX];

        for (int i = 0; i < c.length; i++) {
            for (int j = 0; j < c.length; j++) {
                c[i][j] = request.getMatrixA(i).getColumn(j) + request.getMatrixB(i).getColumn(j);
            }
        }
        MatrixMultiplicationReply.Builder response = MatrixMultiplicationReply.newBuilder();

        for (int i = 0; i < c.length; i++) {
            Row.Builder row = Row.newBuilder();
            for (int j = 0; j < c[i].length; j++) {
                row.addColumn(c[i][j]);
            }
            response.addMatrixC(row.build());
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void multiplyBlock(MatrixMultiplicationRequest request, StreamObserver<MatrixMultiplicationReply> responseObserver)
    {
        // int MAX =  request.getMatrixA(1).getColumnCount();
        // int C[][] = new int[MAX][MAX];
        // C[0][0] = request.getMatrixA(0).getColumn(0) * request.getMatrixB(0).getColumn(0) + request.getMatrixA(0).getColumn(1) * request.getMatrixB(1).getColumn(0);
        // C[0][1] = request.getMatrixA(0).getColumn(0) * request.getMatrixB(0).getColumn(1) + request.getMatrixA(0).getColumn(1) * request.getMatrixB(1).getColumn(1);
        // C[1][0] = request.getMatrixA(1).getColumn(0) * request.getMatrixB(0).getColumn(0) + request.getMatrixA(1).getColumn(1) * request.getMatrixB(1).getColumn(0);
        // C[1][1] = request.getMatrixA(1).getColumn(0) * request.getMatrixB(0).getColumn(1) + request.getMatrixA(1).getColumn(1) * request.getMatrixB(1).getColumn(1);

        // MatrixMultiplicationReply.Builder response = MatrixMultiplicationReply.newBuilder();
        MatrixMultiplicationReply.Builder response = multiplyBlockInt(request);
        // for (int i = 0; i < C.length; i++) {
        // Row.Builder row = Row.newBuilder();
        // for (int j = 0; j < C[i].length; j++) {
        // row.addColumn(C[i][j]);
        // }
        // response.addMatrixC(row.build());
        // }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    // new stream bidirectional multiply
    @Override
    public StreamObserver<MatrixMultiplicationRequest> multiplyStreamBlock(StreamObserver<MatrixMultiplicationReply> responseObserver)
    {
        return new StreamObserver<MatrixMultiplicationRequest>() {
            @Override
            public void onNext(MatrixMultiplicationRequest request) {
                responseObserver.onNext(multiplyBlockInt(request).build());
            }

            @Override
            public void onError(Throwable t) {
                //logger.log(Level.WARNING, "cancelled");
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

    private MatrixMultiplicationReply.Builder multiplyBlockInt(MatrixMultiplicationRequest request)
    {
        int MAX =  request.getMatrixA(1).getColumnCount();
        int C[][] = new int[MAX][MAX];
        C[0][0] = request.getMatrixA(0).getColumn(0) * request.getMatrixB(0).getColumn(0) + request.getMatrixA(0).getColumn(1) * request.getMatrixB(1).getColumn(0);
        C[0][1] = request.getMatrixA(0).getColumn(0) * request.getMatrixB(0).getColumn(1) + request.getMatrixA(0).getColumn(1) * request.getMatrixB(1).getColumn(1);
        C[1][0] = request.getMatrixA(1).getColumn(0) * request.getMatrixB(0).getColumn(0) + request.getMatrixA(1).getColumn(1) * request.getMatrixB(1).getColumn(0);
        C[1][1] = request.getMatrixA(1).getColumn(0) * request.getMatrixB(0).getColumn(1) + request.getMatrixA(1).getColumn(1) * request.getMatrixB(1).getColumn(1);

        MatrixMultiplicationReply.Builder response = MatrixMultiplicationReply.newBuilder();

        for (int i = 0; i < C.length; i++) {
            Row.Builder row = Row.newBuilder();
            for (int j = 0; j < C[i].length; j++) {
                row.addColumn(C[i][j]);
            }
            response.addMatrixC(row.build());
        }
        return response;
    }
}
