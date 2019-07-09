package com.presenso;

import java.util.StringJoiner;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class MatrixUtils {
    // This field represents an approximate size of matrix
    // which will be calculated per worker. Amend this value
    // to achieve best performance
    public static int LOAD_PER_WORKER = 2;

    /**
     * Performs multiplication of matrices.
     * All matrices represented by the two-dimension arrays like int[][] m1;
     * where m1[1][2] refers to the 1st row and the 2nd column in the matrix
     *
     * @param v1 matrix to be multiplied
     * @param v2 matrix to be multiplied
     * @return result of the multiplication
     */
    public static int[][] multiply(int[][] v1, int[][] v2) {
        if (v1 == null || v2 == null || v1.length == 0 || v2.length == 0) {
            throw new IllegalArgumentException("Malformed matrices");
        }
        if (v1[0].length != v2.length) {
            throw new IllegalArgumentException("Number of columns in v1 must equal to the number of rows in v2");
        }
        int[][] result = new int[v1.length][v2[0].length];

        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result[i].length; j++) {
                int sum = 0;
                for (int k = 0; k < v2.length; k++) {
                    sum += v1[i][k] * v2[k][j];
                }
                result[i][j] = sum;
            }
        }
        return result;
    }

    public static int[][] multiplyParallel(int[][] v1, int[][] v2) {
        ForkJoinPool pool = new ForkJoinPool();
        MatrixMultiplicationJob mmj = new MatrixMultiplicationJob(v1, v2);

        pool.invoke(mmj);
        return mmj.result;
    }

    public static String toString(int[][] matrix) {
        StringJoiner columns = new StringJoiner("\n");

        for (int i = 0; i < matrix.length; i++) {
            StringJoiner rows = new StringJoiner(", ");
            for (int j = 0; j < matrix[0].length; j++) {
                rows.add(String.valueOf(matrix[i][j]));
            }
            columns.add(rows.toString());
        }
        return columns.toString();
    }

    private static class MatrixMultiplicationJob extends RecursiveAction {
        private int[][] v1;
        private int[][] v2;
        private int[][] result;
        private int startRow;
        private int endRow;
        private int startColumn;
        private int endColumn;

        public MatrixMultiplicationJob(int[][] v1, int[][] v2) {
            if (v1 == null || v2 == null || v1.length == 0 || v2.length == 0) {
                throw new IllegalArgumentException("Malformed matrices");
            }
            if (v1[0].length != v2.length) {
                throw new IllegalArgumentException("Number of columns in v1 must equal to the number of rows in v2");
            }
            this.v1 = v1;
            this.v2 = v2;
            this.result = new int[v1.length][v2[0].length];
            this.endRow = result.length;
            this.endColumn = result[0].length;
        }

        private MatrixMultiplicationJob(int[][] v1, int[][] v2, int[][] result,
                                        int startRow, int endRow, int startColumn, int endColumn) {
            this.v1 = v1;
            this.v2 = v2;
            this.result = result;
            this.startRow = startRow;
            this.endRow = endRow;
            this.startColumn = startColumn;
            this.endColumn = endColumn;
        }

        @Override
        protected void compute() {
            if (endRow - startRow > endColumn - startColumn) {
                computeOrSplitByRows();
            } else {
                computeOrSplitByColumns();
            }
        }

        void computeOrSplitByRows() {
            if (endRow - startRow < LOAD_PER_WORKER) {
                multiply();
            } else {
                int split = (endRow - startRow) / 2 + startRow;
                invokeAll(clone(startRow, split, startColumn, endColumn),
                        clone(split, endRow, startColumn, endColumn));
            }
        }

        void computeOrSplitByColumns() {
            if (endColumn - startColumn < LOAD_PER_WORKER) {
                multiply();
            } else {
                int split = (endColumn - startColumn) / 2 + startColumn;
                invokeAll(clone(startRow, endRow, startColumn, split),
                        clone(startRow, endRow, split, endColumn));
            }
        }

        void multiply() {
            for (int i = startRow; i < endRow; i++) {
                for (int j = startColumn; j < endColumn; j++) {
                    int sum = 0;
                    for (int k = 0; k < v2.length; k++) {
                        sum += v1[i][k] * v2[k][j];
                    }
                    result[i][j] = sum;
                }
            }
        }

        private MatrixMultiplicationJob clone(int startRow, int endRow, int startColumn, int endColumn) {
            return new MatrixMultiplicationJob(v1, v2, result, startRow, endRow, startColumn, endColumn);
        }
    }
}
