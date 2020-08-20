package src.main;

public class MyRandom{
    int i;
    public MyRandom(long seed){
        System.out.println("seed: " + seed);
        i = (int) seed;
    }
    public int nextInt(){
        i++;
        System.out.println(i);
        return i;
    }
}