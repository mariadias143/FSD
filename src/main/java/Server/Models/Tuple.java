package Server.Models;

public class Tuple<F,S> {
    private F first;
    private S second;

    public Tuple(F fst,S snd){
        this.first = fst;
        this.second = snd;
    }

    public F getFirst() {
        return this.first;
    }

    public S getSecond(){
        return this.second;
    }
}
