import java.util.ArrayList;
import java.util.Arrays;

public class Methods {


    public static int[] firstTask(int[] arr) throws RuntimeException {
        int n = 0;
        ArrayList<Integer> arrayList = new ArrayList<>();
        for (int a: arr) {
            arrayList.add(a);
        }
        if (arrayList.indexOf(4) != -1) {
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] == 4) n = i;
            }
            int[] arr1 = new int[arr.length - n - 1];
            System.arraycopy(arr, n + 1, arr1, 0, arr1.length);
            return arr1;
        } else {
            throw new RuntimeException("4 не найдена");
        }
    }

    public boolean secondTask(int[] arr){
        boolean i1 = false, i4 = false;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == 1) i1 = true;
            else if (arr[i] == 4) i4 = true;
            else return false;
        }
        return i1 && i4;
    }
}
