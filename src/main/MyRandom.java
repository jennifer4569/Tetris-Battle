package src.main;

public class MyRandom{
    int i;
    public MyRandom(long seed){
        i = (int) seed;
    }
    public int nextInt(){
        i = Integer.hashCode(i);
        return i;
    }
}