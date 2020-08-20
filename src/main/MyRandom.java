package src.main;

public class MyRandom{
    int i;
    public MyRandom(long seed){
        i = (int) seed;
    }
    public int nextInt(){
        i++;
        return i;
    }
}