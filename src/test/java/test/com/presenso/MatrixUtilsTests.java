package test.com.presenso;

import com.presenso.MatrixUtils;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class MatrixUtilsTests {
    private static final int[][] M1 = {{7, 8, 9, 0},
                                       {4, 5, 3, 1},
                                       {2, 5, 8, 9}};
    private static final int[][] M2 = {{7, 8, 9},
                                       {4, 5, 3},
                                       {2, 5, 8},
                                       {4, 5, 6}};
    private static final int[][] EXPECTED_RESULT = {{99, 141, 159},
                                                    {58, 77, 81},
                                                    {86, 126, 151}};

    @Test(expected = IllegalArgumentException.class)
    public void multiply_illegalSizedMatrices_exceptionThrown() {
        MatrixUtils.multiply(new int[1][2], new int[1][2]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void multiply_emptyArray_exceptionThrown() {
        MatrixUtils.multiply(new int[0][0], new int[0][0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void multiply_malformedArray_exceptionThrown() {
        MatrixUtils.multiply(new int[2][0], new int[0][2]);
    }

    @Test
    public void multiply_twoMatrices_resultReturned() {
        int[][] result = MatrixUtils.multiply(M1, M2);

        assertArraysAreEqual(result, EXPECTED_RESULT);
    }

    @Test
    public void multiplyParallel_twoMatrices_resultReturned() {
        int[][] result = MatrixUtils.multiplyParallel(M1, M2);

        assertArraysAreEqual(result, EXPECTED_RESULT);
    }

    private void assertArraysAreEqual(int[][] result, int[][] expectedResult) {
        assertEquals(result.length, expectedResult.length);

        for (int i = 0; i < result.length; i++) {
            assertEquals(result[i].length, expectedResult[i].length);
        }
        for (int i = 0; i < expectedResult.length; i++) {
            for (int j = 0; j < expectedResult[i].length; j++) {
                assertThat(result[i][j], is(expectedResult[i][j]));
            }
        }
    }
}
