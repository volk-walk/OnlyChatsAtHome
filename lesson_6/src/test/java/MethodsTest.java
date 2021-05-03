import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MethodsTest {
    private Methods methods;

    @BeforeEach
    public void init(){
        methods = new Methods();
    }

    @Test
    public void testFirsTask1(){
        int[] arr1 = {1, 7};
        int[] arr2 = {1, 2, 4, 4, 2, 3, 4, 1, 7};
        Assertions.assertArrayEquals(arr1,methods.firstTask(arr2));
    }

    @Test
    public void testFirsTask2(){
        int[] arr1 = {};
        int[] arr2 = {1, 2, 4, 3, 4};
        Assertions.assertArrayEquals(arr1,methods.firstTask(arr2));
    }

    @Test
    public void testFirsTask3(){
        int[] arr = {1, 2, 44, 24, 3, 7};
        Assertions.assertThrows(RuntimeException.class, ()-> methods.firstTask(arr));
    }

    @Test
    public void testSecondTask1(){
        int[] arr = {1,1,1,4,4,1,4,4};
        Assertions.assertEquals(true, methods.secondTask(arr));
    }
    @Test
    public void testSecondTask2(){
        int[] arr = {1,1,1,1,1,1};
        Assertions.assertEquals(false, methods.secondTask(arr));
    }
    @Test
    public void testSecondTask3(){
        int[] arr = {4,4,4,4,4,4};
        Assertions.assertEquals(false, methods.secondTask(arr));
    }
    @Test
    public void testSecondTask4(){
        int[] arr = {1,4,1,1,1,4,4,3};
        Assertions.assertEquals(false, methods.secondTask(arr));
    }
}
